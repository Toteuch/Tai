// SPDX-License-Identifier: GPL-3.0-only
import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { createTaiUiEventSource, getTaiUiState } from '@/features/ui-state/api/taiUiApi';
import { TaiUiStateSchema } from '@/features/ui-state/types';
import { taiUiStateQueryKey } from '@/features/ui-state/hooks/useTaiUiState';
import { useLocalUiStore } from '@/store/localUiStore';

export function useTaiUiStateStream() {
  const queryClient = useQueryClient();
  const setStreamStatus = useLocalUiStore((state) => state.setStreamStatus);

  useEffect(() => {
    setStreamStatus('connecting');
    const source = createTaiUiEventSource();

    source.onopen = () => {
      setStreamStatus('connected');
    };

    source.onerror = () => {
      setStreamStatus(source.readyState === EventSource.CLOSED ? 'disconnected' : 'reconnecting');
    };

    const handleSnapshot = (event: MessageEvent<string>) => {
      try {
        const parsed = TaiUiStateSchema.parse(JSON.parse(event.data));
        queryClient.setQueryData(taiUiStateQueryKey, parsed);
        setStreamStatus('connected');
      } catch (error) {
        console.error('Invalid tai-ui-state SSE payload.', error);
      }
    };

    source.addEventListener('tai-ui-state', handleSnapshot as EventListener);

    return () => {
      source.removeEventListener('tai-ui-state', handleSnapshot as EventListener);
      source.close();
      setStreamStatus('disconnected');
    };
  }, [queryClient, setStreamStatus]);

  useEffect(() => {
    const unsubscribe = useLocalUiStore.subscribe((state, previousState) => {
      if (previousState.streamStatus === 'reconnecting' && state.streamStatus === 'connected') {
        void queryClient.fetchQuery({
          queryKey: taiUiStateQueryKey,
          queryFn: ({ signal }) => getTaiUiState(signal),
        });
      }
    });

    return unsubscribe;
  }, [queryClient]);
}
