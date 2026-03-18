package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum QuoteManagementModule {
    QUOTATION_DASHBOARD(1, "Quotation Dashboard"),
    MANAGE_QUOTATION(2, "Quotation Administration"),
    FEES_TYPE_CONSTANTS(3, "Fees Type Constants"),
    TERMS_AND_CONDITIONS(4, "Terms and Conditions"),
    QUOTATION_HISTORY(5, "Quotation History");

    private final int id;
    private final String value;

    // Constructor
    QuoteManagementModule(int id, String value) {
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
    public static QuoteManagementModule getById(int id) {
        for (QuoteManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static QuoteManagementModule getByValue(String value) {
        for (QuoteManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (QuoteManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
