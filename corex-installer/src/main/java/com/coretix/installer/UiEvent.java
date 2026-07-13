package com.coretix.installer;

final class UiEvent {
    enum Type {
        START,
        STEP_STARTED,
        PROGRESS,
        MESSAGE,
        COMPLETE
    }

    private final Type type;
    private final int completedSteps;
    private final int totalSteps;
    private final InstallStep step;
    private final String message;

    private UiEvent(Type type, int completedSteps, int totalSteps, InstallStep step, String message) {
        this.type = type;
        this.completedSteps = completedSteps;
        this.totalSteps = totalSteps;
        this.step = step;
        this.message = message;
    }

    static UiEvent start(int totalSteps) {
        return new UiEvent(Type.START, 0, totalSteps, null, null);
    }

    static UiEvent progress(int completedSteps, int totalSteps, InstallStep step) {
        return new UiEvent(Type.PROGRESS, completedSteps, totalSteps, step, null);
    }

    static UiEvent stepStarted(int stepNumber, int totalSteps, InstallStep step) {
        return new UiEvent(Type.STEP_STARTED, stepNumber, totalSteps, step, null);
    }

    static UiEvent message(String message) {
        return new UiEvent(Type.MESSAGE, 0, 0, null, message);
    }

    static UiEvent complete() {
        return new UiEvent(Type.COMPLETE, 0, 0, null, null);
    }

    Type type() {
        return type;
    }

    int completedSteps() {
        return completedSteps;
    }

    int totalSteps() {
        return totalSteps;
    }

    InstallStep step() {
        return step;
    }

    String message() {
        return message;
    }
}
