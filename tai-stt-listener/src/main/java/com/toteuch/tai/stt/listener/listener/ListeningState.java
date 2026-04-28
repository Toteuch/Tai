// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.listener;

public enum ListeningState {
    STOPPED,
    STARTING,
    WAITING_FOR_SPEECH,
    CAPTURING,
    PROCESSING,
    ERROR
}
