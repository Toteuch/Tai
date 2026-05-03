// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.history;

import com.toteuch.tai.orchestrator.session.ConversationTurn;
import com.toteuch.tai.orchestrator.session.SessionContext;
import com.toteuch.tai.orchestrator.session.SessionStore;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryItem;
import com.toteuch.tai.orchestrator.ui.model.history.ConversationHistoryPage;
import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationHistoryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final SessionStore sessionStore;
    private final ConversationHistoryMapper mapper;
    private final Clock clock;

    public ConversationHistoryService(
            SessionStore sessionStore, ConversationHistoryMapper mapper, Clock clock) {
        this.sessionStore = sessionStore;
        this.mapper = mapper;
        this.clock = clock;
    }

    public ConversationHistoryPage getHistory(Integer requestedLimit, String cursor) {
        int limit = normalizeLimit(requestedLimit);
        List<ConversationTurn> turns = completedTurnsSnapshot();

        if (turns.isEmpty()) {
            return emptyPage(limit);
        }

        int exclusiveEndIndex = resolveExclusiveEndIndex(turns, cursor);

        if (exclusiveEndIndex <= 0) {
            return emptyPage(limit);
        }

        int fromIndexInclusive = Math.max(0, exclusiveEndIndex - limit);

        List<ConversationTurn> pageTurns = turns.subList(fromIndexInclusive, exclusiveEndIndex);

        List<ConversationHistoryItem> items =
                pageTurns.reversed().stream().map(mapper::toHistoryItem).toList();

        String nextCursor = resolveNextCursor(items, fromIndexInclusive);

        return new ConversationHistoryPage(clock.instant(), limit, nextCursor, items);
    }

    private List<ConversationTurn> completedTurnsSnapshot() {
        SessionContext sessionContext = sessionStore.get();

        /*
         * À adapter au vrai nom de l’accesseur.
         *
         * L’important :
         * - retourner uniquement les turns historisés / complétés
         * - dans l’ordre naturel : plus ancien → plus récent
         * - retourner une copie stable si la collection interne est mutable
         */
        return List.copyOf(sessionContext.getTurns());
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit < 1) {
            return DEFAULT_LIMIT;
        }

        return Math.min(requestedLimit, MAX_LIMIT);
    }

    private int resolveExclusiveEndIndex(List<ConversationTurn> turns, String cursor) {
        if (cursor == null || cursor.isBlank()) {
            /*
             * Initial request:
             * exclude latest completed turn because it is already represented
             * by the live overview.
             */
            return turns.size() - 1;
        }

        for (int index = 0; index < turns.size(); index++) {
            if (cursor.equals(turns.get(index).getCorrelationId())) {
                /*
                 * Cursor is exclusive:
                 * cursor=T80 returns T79 and older.
                 */
                return index;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Unknown conversation history cursor: " + cursor);
    }

    private String resolveNextCursor(List<ConversationHistoryItem> items, int fromIndexInclusive) {
        if (items.isEmpty() || fromIndexInclusive == 0) {
            return null;
        }

        return items.getLast().correlationId();
    }

    private ConversationHistoryPage emptyPage(int limit) {
        return new ConversationHistoryPage(clock.instant(), limit, null, List.of());
    }
}
