/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
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
 * Project: app.name
 */
package com.web.coretix.constants;

public enum UserTypeConstants {
    APPLICATION_ADMIN("APPLICATION_ADMIN"),
    GENERAL_USER("GENERAL_USER");

    private final String value;

    UserTypeConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserTypeConstants fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return GENERAL_USER;
        }

        for (UserTypeConstants userType : values()) {
            if (userType.getValue().equalsIgnoreCase(value.trim())) {
                return userType;
            }
        }

        return GENERAL_USER;
    }
}
