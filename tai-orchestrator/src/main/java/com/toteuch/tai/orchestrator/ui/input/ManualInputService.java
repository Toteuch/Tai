// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.orchestrator.ui.input;

import com.toteuch.tai.events.EventSource;
import com.toteuch.tai.events.ui.UiManualTextInputReceivedEvent;
import com.toteuch.tai.orchestrator.core.publisher.TaiEventPublisher;
import com.toteuch.tai.orchestrator.ui.model.input.ManualInputRequest;
import com.toteuch.tai.orchestrator.ui.model.input.ManualInputResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ManualInputService {

    private final TaiEventPublisher eventPublisher;
    private final ManualInputProperties properties;
    private final Clock clock;

    public ManualInputService(
            TaiEventPublisher eventPublisher, ManualInputProperties properties, Clock clock) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
    }

    public ManualInputResponse submit(ManualInputRequest request) {
        String text = normalizeText(request);

        validateText(text);

        Instant now = clock.instant();
        String correlationId = UUID.randomUUID().toString();

        eventPublisher.publish(
                new UiManualTextInputReceivedEvent(
                        UUID.randomUUID().toString(), now, correlationId, EventSource.UI, text));

        return new ManualInputResponse(true, correlationId, now);
    }

    private String normalizeText(ManualInputRequest request) {
        if (request == null || request.text() == null) {
            return "";
        }

        return request.text().trim();
    }

    private void validateText(String text) {
        if (text.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Manual input text must not be blank");
        }

        if (text.length() > properties.getMaxLength()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Manual input text must not exceed "
                            + properties.getMaxLength()
                            + " characters");
        }
    }
}
