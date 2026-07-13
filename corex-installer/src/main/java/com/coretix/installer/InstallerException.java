package com.coretix.installer;

public class InstallerException extends Exception {
    public InstallerException(String message) {
        super(message);
    }

    public InstallerException(String message, Throwable cause) {
        super(message, cause);
    }
}
