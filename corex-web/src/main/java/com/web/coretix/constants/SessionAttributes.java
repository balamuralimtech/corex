/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: `app.name`
 */
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
    APPLICATION_NOTIFICATION_GROWL("notificationGrowl"),
    APPLICATION_NOTIFICATION_MESSAGES("notificationMessages"),
    APPLICATION_NOTIFICATION_UNREAD_COUNT("notificationUnreadCount"),
    ROLE_UPDATE_LOGOUT_NOTIFICATION("roleUpdateLogoutNotification"),
    SESSION_AUDIT_COMPLETED("sessionAuditCompleted"),
    SESSION_TERMINATION_REASON("sessionTerminationReason"),
    LAST_ACTIVITY_AT("lastActivityAt");

    private String name;

    private SessionAttributes(String var3) {
        this.name = var3;
    }

    public String getName() {
        return this.name;
    }
}




