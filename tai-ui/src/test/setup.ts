// SPDX-License-Identifier: GPL-3.0-only
import '@testing-library/jest-dom/vitest';

class MockEventSource {
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSED = 2;

  readonly url: string;
  readyState = MockEventSource.CONNECTING;
  onopen: (() => void) | null = null;
  onerror: (() => void) | null = null;
  private listeners = new Map<string, EventListener>();

  constructor(url: string) {
    this.url = url;
  }

  addEventListener(type: string, listener: EventListener) {
    this.listeners.set(type, listener);
  }

  removeEventListener(type: string) {
    this.listeners.delete(type);
  }

  close() {
    this.readyState = MockEventSource.CLOSED;
  }
}

Object.defineProperty(window, 'EventSource', {
  writable: true,
  value: MockEventSource,
});
