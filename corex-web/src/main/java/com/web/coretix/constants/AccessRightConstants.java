/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
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
