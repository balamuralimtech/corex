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

/**
 *
 * @author balamurali
 */
public enum NotificationTypeConstant {

    APPLICATION(1, "Application Notification"),
    ORGANIZATION(2, "Organization Notification"),
    USER(3, "User Notification");

    private int id;
    private String description;

    // Constructor
    NotificationTypeConstant(int id, String description) {
        this.id = id;
        this.description = description;
    }

    // Getters for id and description
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    // Static method to get enum by id
    public static NotificationTypeConstant fromId(int id) {
        for (NotificationTypeConstant type : NotificationTypeConstant.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with id " + id);
    }

    // Static method to get enum by description (optional)
    public static NotificationTypeConstant fromDescription(String description) {
        for (NotificationTypeConstant type : NotificationTypeConstant.values()) {
            if (type.getDescription().equalsIgnoreCase(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with description " + description);
    }
}




