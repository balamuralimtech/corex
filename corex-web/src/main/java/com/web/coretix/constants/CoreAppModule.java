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


import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author balamurali
 */
public enum CoreAppModule {
    USER_MANAGEMENT(1, "User Management"),
    SYSTEM_MANAGEMENT(2, "System Management"),
    LICENCE(3, "License"),
    SERVER_AND_DB(4, "Server and DB");


    private final int id;
    private final String value;

    // Constructor
    CoreAppModule(int id, String value) {
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
    public static CoreAppModule getById(int id) {
        for (CoreAppModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static CoreAppModule getByValue(String value) {
        for (CoreAppModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (CoreAppModule module : getAllModules()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }

    // Method to get all enum entries as a strongly typed list
    public static List<CoreAppModule> getAllModules() {
        List<CoreAppModule> modules = new ArrayList<>();
        for (CoreAppModule module : values()) {
            modules.add(module);
        }
        return modules;
    }
}



