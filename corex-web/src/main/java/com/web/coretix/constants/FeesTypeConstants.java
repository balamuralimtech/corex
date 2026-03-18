package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum FeesTypeConstants {

    POL(1,"Port Of Loading"),
    POD(2,"Port Of Destination"),
    FPOD(3,"Final Place Of Delivery");


    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    private final int id;
    private final String value;

    FeesTypeConstants(int id, String value) {
        this.id = id;
        this.value = value;

    }

    public static FeesTypeConstants getById(int id) {
        for (FeesTypeConstants module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    public static FeesTypeConstants getByValue(String value) {
        for (FeesTypeConstants module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }
    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (FeesTypeConstants module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}

