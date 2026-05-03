// SPDX-License-Identifier: GPL-3.0-only
import { useQuery } from '@tanstack/react-query';
import { getModuleDetails } from '@/features/ui-state/api/taiUiApi';
import { TaiModule } from '@/features/ui-state/types';

export function useModuleDetails(module: TaiModule | null) {
  return useQuery({
    queryKey: ['module-details', module],
    queryFn: ({ signal }) => getModuleDetails(module!, signal),
    enabled: module !== null,
  });
}
