/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.List;

public enum ApplicationManagementModule {
    DEMO_REQUESTS(1, "Demo Requests"),
    APPLICATION_PRICING(2, "Application Pricing"),
    REFERRAL_MANAGEMENT(3, "Referral Management"),
    REFERRAL_DASHBOARD(4, "Referral Dashboard");

    private final int id;
    private final String value;

    ApplicationManagementModule(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static ApplicationManagementModule getById(int id) {
        for (ApplicationManagementModule module : values()) {
            if (module.getId() == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module id: " + id);
    }

    public static ApplicationManagementModule getByValue(String value) {
        for (ApplicationManagementModule module : values()) {
            if (module.getValue().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid module value: " + value);
    }

    public static List<String> getAllValues() {
        List<String> valuesList = new ArrayList<>();
        for (ApplicationManagementModule module : values()) {
            valuesList.add(module.getValue());
        }
        return valuesList;
    }
}
