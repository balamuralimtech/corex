/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
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
    MODULES_ROLE_DEFINITION(3, "Modules Role Definition"),
    ROLE_ADMINISTRATION(4, "Role Administration"),
    USER_ACCESS_RIGHT(5, "User Access Right"),
    USER_MANAGEMENT(6, "User Management");

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
