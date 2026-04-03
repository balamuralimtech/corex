package com.web.shipx.constants;

import java.util.ArrayList;
import java.util.List;

public enum QuotationStatusConstants {
    DRAFT(1,"Draft"),
    NEW(2,"New"),
    RENEW(3,"Renew"),
    WAITING_FOR_ACCEPTANCE(4,"Waiting For Acceptance"),
    UNDER_NEGOTIATION(5,"Under Negotiation"),
    EXPIRED(6,"Expired"),
    ACCEPTED(7,"Accepted"),
    MOVED_TO_SHIPPING(8,"Moved To Shipping"),
    REJECTED_BY_CLIENT(9,"Rejected By Client"),
    REJECTED_BY_COMPANY(10,"Rejected By Company");


    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    private final int id;
    private final String value;

    QuotationStatusConstants(int id, String value) {
        this.id = id;
        this.value = value;

    }

    public static QuotationStatusConstants getById(int id) {
        for (QuotationStatusConstants module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    public static QuotationStatusConstants getByValue(String value) {
        for (QuotationStatusConstants module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }
    // Method to get all values as a list
    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (QuotationStatusConstants module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
