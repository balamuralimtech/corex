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
public enum RolePrivilegeConstants {
    VIEW(1, "View"),
    ADD(2, "Add"),
    EDIT(3, "Edit"),
    DELETE(4, "Delete"),
    EXPORT(5, "Export"),
    PRINT(6, "Print"),
    EMAIL(7, "Email"),
    PREPARE(8, "Prepare"),
    APPROVE(9, "Approve"),
    REJECT(10, "Reject"),
    APP_NOTIFY(11,"APP_NOTIFY");


    private final int id;
    private final String value;

    // Constructor
    RolePrivilegeConstants(int id, String value) {
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
    public static RolePrivilegeConstants getById(int id) {
        for (RolePrivilegeConstants module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid UserManagementModule id: " + id);
    }

    // Method to get enum by value
    public static RolePrivilegeConstants getByValue(String value) {
        for (RolePrivilegeConstants module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid UserManagementModule value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (RolePrivilegeConstants module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}



