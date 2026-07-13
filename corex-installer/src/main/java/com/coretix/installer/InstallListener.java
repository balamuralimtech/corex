package com.coretix.installer;

public interface InstallListener {
    void onStart(int totalSteps);

    void onStepStarted(int stepNumber, int totalSteps, InstallStep step);

    void onProgress(int completedSteps, int totalSteps, InstallStep currentStep);

    void onMessage(String message);

    void onComplete();
}
