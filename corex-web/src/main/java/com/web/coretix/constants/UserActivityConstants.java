package com.web.coretix.constants;

public enum UserActivityConstants {

    L0GIN(1, "Login"),
    LOGOUT(2, "Logout"),
    ADD(3, "Add"),
    UPDATE(4, "Update"),
    DELETE(5, "Delete");

    private final int id;
    private final String value;

    UserActivityConstants(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
