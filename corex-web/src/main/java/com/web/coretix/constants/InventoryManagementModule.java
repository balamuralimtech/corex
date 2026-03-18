package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum InventoryManagementModule {
    MANAGE_CONTAINER(1, "Manage Container"),
    MANAGE_YARD(2, "Manage Yard"),
    MANAGE_PORT(3, "Manage Port"),
    MANAGE_VESSEL(4, "Manage Vessel"),
    MANAGE_VESSEL_TYPE(5, "Manage Vessel Type"),
    MANAGE_CONTAINER_TYPE(6,"Manage Container Type"),
    MANAGE_CONTAINER_SIZE(7,"Manage Container Size"),
    VESSEL_HISTORY(8,"Vessel History"),
    CONTAINER_HISTORY(9,"Container History");

    private final int id;
    private final String value;

    // Constructor
    InventoryManagementModule(int id, String value) {
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
    public static InventoryManagementModule getById(int id) {
        for (InventoryManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static InventoryManagementModule getByValue(String value) {
        for (InventoryManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (InventoryManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
