// SPDX-License-Identifier: GPL-3.0-only
import { useEffect, useState } from 'react';

export function useCurrentTime() {
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1_000);
    return () => window.clearInterval(timer);
  }, []);

  return now;
}
