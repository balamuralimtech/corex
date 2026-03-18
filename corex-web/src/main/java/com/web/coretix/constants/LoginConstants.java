package com.web.coretix.constants;

public enum LoginConstants {
    SUCCESSFUL_LOGIN(1, "Successful Login"),
    FAILED_LOGIN(2, "Failed Login"),
    NEVER_LOGIN_BEFORE(3, "Never Login Before"),
    PASSWORD_EXPIRED(4, "Password Expired"),
    SESSION_TIMEOUT(5, "Session Timeout"),
    LOGOUT_SUCCESSFUL(6, "Logout Successful"),
    LOGOUT_FAILED(7, "Logout Failed"),
    LICENSE_EXPIRED(8, "License Expired");

    private final int id;
    private final String value;

    LoginConstants(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    // Method to get value by id
    public static String getValueById(int id) {
        for (LoginConstants constant : LoginConstants.values()) {
            if (constant.getId() == id) {
                return constant.getValue();
            }
        }
        return "Unknown"; // Return a default value if not found
    }
}
