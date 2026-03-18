package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum ClientManagementModule {
    CLIENT_ADMINISTRATION(1, "Client Administration"),
    CLIENT_TYPE(2, "Client Type"),
    CLIENT_HISTORY(3, "Client History");

    private final int id;
    private final String value;

    // Constructor
    ClientManagementModule(int id, String value) {
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
    public static ClientManagementModule getById(int id) {
        for (ClientManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static ClientManagementModule getByValue(String value) {
        for (ClientManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (ClientManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
