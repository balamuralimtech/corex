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

import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author balamurali
 */
public enum AccessRightConstants {
    Organization(1, "Organization Access Right"),
    Branch(2, "Branch Access Right");
    
    private final int id;
    private final String value;
    
    // Constructor
    AccessRightConstants(int id, String value) {
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
    public static AccessRightConstants getById(int id) {
        for (AccessRightConstants module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }
    // Method to get enum by value
    public static AccessRightConstants getByValue(String value) {
        for (AccessRightConstants module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }
    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (AccessRightConstants module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}



