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
public enum SystemManagementModule {
    ORGANIZATION(1, "Organization"),
    BRANCH(2, "Branch"),
    DEPARTMENT(3, "Department"),
    DESIGNATION (4, "Designation"),
    COUNTRY(5, "Country"),
    STATE(6, "State"),
    CITY(7, "City"),
    REGION(8, "Region"),
    SUBREGION(9, "Subregion"),
    CURRENCY(10, "Currency"),
    BANK_DETAILS(11, "Bank Details"),
    NOTIFICATION_SETTING(12, "Notification Setting");
    

    private final int id;
    private final String value;

    // Constructor
    SystemManagementModule(int id, String value) {
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
    public static SystemManagementModule getById(int id) {
        for (SystemManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static SystemManagementModule getByValue(String value) {
        for (SystemManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (SystemManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
