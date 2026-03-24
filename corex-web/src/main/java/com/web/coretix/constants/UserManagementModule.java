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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author balamurali
 */
public enum UserManagementModule {
    USER_PROFILE(1, "User Profile"),
    USER_ACTIVITY(2, "User Activity"),
    ROLE_ADMINISTRATION(3, "Role Administration"),
    CHANGE_PASSWORD(4, "Change Password"),
    USER_ADMINISTRATION(5, "User Administration");

    private final int id;
    private final String value;

    // Constructor
    UserManagementModule(int id, String value) {
        this.id = id;
        this.value = value;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Getter for value
    public String getValue() {
        return value;
    }

    // Method to get enum by id
    public static UserManagementModule getById(int id) {
        for (UserManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid UserManagementModule id: " + id);
    }

    // Method to get enum by value
    public static UserManagementModule getByValue(String value) {
        for (UserManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid UserManagementModule value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (UserManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}



