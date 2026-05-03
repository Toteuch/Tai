// SPDX-License-Identifier: GPL-3.0-only
import { useQuery } from '@tanstack/react-query';
import { getTaiUiState } from '@/features/ui-state/api/taiUiApi';
import { fallbackTaiUiState } from '@/features/ui-state/fallbackState';

export const taiUiStateQueryKey = ['tai-ui-state'] as const;

export function useTaiUiState() {
  return useQuery({
    queryKey: taiUiStateQueryKey,
    queryFn: ({ signal }) => getTaiUiState(signal),
    initialData: fallbackTaiUiState,
    refetchInterval: false,
  });
}
