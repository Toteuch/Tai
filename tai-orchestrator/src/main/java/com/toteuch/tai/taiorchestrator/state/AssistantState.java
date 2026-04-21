package com.toteuch.tai.taiorchestrator.state;

public class AssistantState {

    private ListeningState listeningState = ListeningState.IDLE;
    private ThinkingState thinkingState = ThinkingState.IDLE;
    private SpeakingState speakingState = SpeakingState.SILENT;

    private String currentSessionId;
    private String currentUserText;
    private String currentAssistantText;

    private boolean ttsEnabled = true;
    private boolean obscenityFilterEnabled = false;

    public ListeningState getListeningState() {
        return listeningState;
    }

    public void setListeningState(ListeningState listeningState) {
        this.listeningState = listeningState;
    }

    public ThinkingState getThinkingState() {
        return thinkingState;
    }

    public void setThinkingState(ThinkingState thinkingState) {
        this.thinkingState = thinkingState;
    }

    public SpeakingState getSpeakingState() {
        return speakingState;
    }

    public void setSpeakingState(SpeakingState speakingState) {
        this.speakingState = speakingState;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    public void setCurrentSessionId(String currentSessionId) {
        this.currentSessionId = currentSessionId;
    }

    public String getCurrentUserText() {
        return currentUserText;
    }

    public void setCurrentUserText(String currentUserText) {
        this.currentUserText = currentUserText;
    }

    public String getCurrentAssistantText() {
        return currentAssistantText;
    }

    public void setCurrentAssistantText(String currentAssistantText) {
        this.currentAssistantText = currentAssistantText;
    }

    public boolean isTtsEnabled() {
        return ttsEnabled;
    }

    public void setTtsEnabled(boolean ttsEnabled) {
        this.ttsEnabled = ttsEnabled;
    }

    public boolean isObscenityFilterEnabled() {
        return obscenityFilterEnabled;
    }

    public void setObscenityFilterEnabled(boolean obscenityFilterEnabled) {
        this.obscenityFilterEnabled = obscenityFilterEnabled;
    }
}
