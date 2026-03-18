package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum InvoiceManagementModule {
    CREATE_INVOICE(1, "Create Invoice"),
    INVOICE_ADMINISTRATION(2, "Invoice Administration");

    private final int id;
    private final String value;

    // Constructor
    InvoiceManagementModule(int id, String value) {
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
    public static InvoiceManagementModule getById(int id) {
        for (InvoiceManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    // Method to get enum by value
    public static InvoiceManagementModule getByValue(String value) {
        for (InvoiceManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (InvoiceManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
