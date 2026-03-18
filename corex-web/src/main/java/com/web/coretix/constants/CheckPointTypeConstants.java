package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum CheckPointTypeConstants {
    TWO_POINTS(1,"2 Points"),
    THREE_POINTS(2,"3 points"),;

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    private final int id;
    private final String value;

    CheckPointTypeConstants(int id, String value) {
        this.id = id;
        this.value = value;

    }

    public static CheckPointTypeConstants getById(int id) {
        for (CheckPointTypeConstants module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    public static CheckPointTypeConstants getByValue(String value) {
        for (CheckPointTypeConstants module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }
    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (CheckPointTypeConstants module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
