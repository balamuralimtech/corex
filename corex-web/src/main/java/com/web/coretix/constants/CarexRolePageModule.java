package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum CarexRolePageModule {
    DASHBOARD(1, "Clinic Management - Dashboard"),
    ADMISSION(2, "Clinic Management - Admission"),
    CONSULTATION(3, "Clinic Management - Consultation"),
    MANAGE_DOCTOR(4, "Clinic Management - Manage Doctor"),
    MANAGE_PATIENT(5, "Clinic Management - Manage Patient"),
    MANAGE_MEDICINE(6, "Clinic Management - Manage Medicine"),
    PATIENT_HISTORY_REPORT(7, "Reports - Patient History"),
    PRESCRIPTION_HISTORY_REPORT(8, "Reports - Prescription History"),
    REVENUE_REPORT(9, "Reports - Revenue Report"),
    PATIENT_INCOMING_REPORT(10, "Reports - Patient Incoming Report"),
    CLINIC_REPORT(11, "Reports - Clinic Report"),
    INVOICE_HISTORY_REPORT(12, "Reports - Invoice History"),
    MEDICINE_REPORT(13, "Reports - Medicine Report"),
    CLINIC_SETTINGS(14, "Settings - Clinic Settings"),
    PRESCRIPTION_SETTINGS(15, "Settings - Prescription Settings"),
    INVOICE_SETTINGS(16, "Settings - Invoice Settings"),
    MEDICAL_CERTIFICATE_SETTINGS(17, "Settings - Medical Certificate Settings");

    private final int id;
    private final String value;

    CarexRolePageModule(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static CarexRolePageModule getById(int id) {
        for (CarexRolePageModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid CareX role page id: " + id);
    }

    public static CarexRolePageModule getByValue(String value) {
        for (CarexRolePageModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid CareX role page value: " + value);
    }

    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (CarexRolePageModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
