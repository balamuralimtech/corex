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
public enum LicenseManagementModule {
    LICENSE(1, "License");
    

    private final int id;
    private final String value;

    // Constructor
    LicenseManagementModule(int id, String value) {
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
    public static LicenseManagementModule getById(int id) {
        for (LicenseManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static LicenseManagementModule getByValue(String value) {
        for (LicenseManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (LicenseManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
