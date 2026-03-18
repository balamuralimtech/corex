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
public enum AppModule {
    USER_MANAGEMENT(1, "User Management"),
    SYSTEM_MANAGEMENT(2, "System Management"),
    LICENCE(3, "License"),
    SERVER_AND_DB(4, "Server and DB"),
    CLIENT_MANAGEMENT(5, "Client Management"),
    INVENTORY_MANAGEMENT(6, "Inventory Management"),
    QUOTE_MANAGEMENT(7, "Quote Management"),
    SHIPMENT_MANAGEMENT(8, "Shipment Management"),
    CLINIC_MANAGEMENT(9, "Clinic Management");

    private final int id;
    private final String value;

    // Constructor
    AppModule(int id, String value) {
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
    public static AppModule getById(int id) {
        for (AppModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static AppModule getByValue(String value) {
        for (AppModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (AppModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
