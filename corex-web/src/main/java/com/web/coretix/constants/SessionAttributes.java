package com.web.coretix.constants;

public enum SessionAttributes {
    USERNAME("username"),
    ROLE("role"),
    LOGIN_TIME("loginTime"),
    LOGIN_STATUS("Login Status"),
    NETWORK_ADDRESS("networkAddress"),
    ORGANIZATION_ID("organizationId"),
    ORGANIZATION_NAME("organizationName"),
    COUNTRY_ID("countryId"),
    USER_ACCOUNT_ID("userAccountId"),
    BROWSER_CLIENT_INFO("browserClientInfo"),
    MACHINE_NAME("Machine Name"),
    MACHINE_IP("Machine IP"),
    ACCESS_RIGHT("accessRight"),
    ACCESS_RIGHT_ID("accessRightId"),
    ROLE_ID("roleId"),
    LANGUAGE("language"),
    APPLICATION_NOTIFICATION_GROWL("notificationGrowl");

    private String name;

    private SessionAttributes(String var3) {
        this.name = var3;
    }

    public String getName() {
        return this.name;
    }
}
