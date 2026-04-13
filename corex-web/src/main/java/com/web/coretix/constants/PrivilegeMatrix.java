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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PrivilegeMatrix {

    private static final EnumSet<RolePrivilegeConstants> ALL_PRIVILEGES =
            EnumSet.allOf(RolePrivilegeConstants.class);

    public static final Map<String, EnumSet<RolePrivilegeConstants>> PAGE_PRIVILEGES =
            buildPagePrivileges();

    private PrivilegeMatrix() {
    }

    public static List<String> getAllowedPrivilegeValues(String subModule) {
        EnumSet<RolePrivilegeConstants> allowedPrivileges = PAGE_PRIVILEGES.getOrDefault(
                subModule,
                ALL_PRIVILEGES);

        List<String> privilegeValues = new ArrayList<>();
        for (RolePrivilegeConstants privilege : allowedPrivileges) {
            privilegeValues.add(privilege.getValue());
        }
        return privilegeValues;
    }

    public static boolean isPrivilegeAllowedForPage(String subModule, RolePrivilegeConstants privilege) {
        return PAGE_PRIVILEGES.getOrDefault(subModule, ALL_PRIVILEGES).contains(privilege);
    }

    private static Map<String, EnumSet<RolePrivilegeConstants>> buildPagePrivileges() {
        Map<String, EnumSet<RolePrivilegeConstants>> pagePrivileges = new LinkedHashMap<>();

        //User Management entries
        pagePrivileges.put(UserManagementModule.USER_PROFILE.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW));
        pagePrivileges.put(UserManagementModule.USER_ACTIVITY.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(UserManagementModule.ROLE_ADMINISTRATION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
       pagePrivileges.put(UserManagementModule.CHANGE_PASSWORD.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EDIT));
        pagePrivileges.put(UserManagementModule.USER_ADMINISTRATION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.EMAIL, RolePrivilegeConstants.APP_NOTIFY));

        //System Management entries
        pagePrivileges.put(SystemManagementModule.ORGANIZATION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.BRANCH.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.DEPARTMENT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.DESIGNATION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.COUNTRY.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.STATE.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.CITY.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.REGION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.SUBREGION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.CURRENCY.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.BANK_DETAILS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));
        pagePrivileges.put(SystemManagementModule.NOTIFICATION_SETTING.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.EMAIL, RolePrivilegeConstants.APP_NOTIFY));

        //Licence entries
        pagePrivileges.put(LicenseManagementModule.LICENSE.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.APP_NOTIFY));

        //Server and DB entries
        pagePrivileges.put(ServerAndDBModule.SERVER_LOGS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT, RolePrivilegeConstants.EMAIL));
        pagePrivileges.put(ServerAndDBModule.DATABASE.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW));

        //Application Privileges can be added here below
        pagePrivileges.put(ApplicationManagementModule.DEMO_REQUESTS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(ApplicationManagementModule.APPLICATION_PRICING.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(ApplicationManagementModule.REFERRAL_MANAGEMENT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(ApplicationManagementModule.REFERRAL_DASHBOARD.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));

        //CareX entries
        pagePrivileges.put(CarexRolePageModule.DASHBOARD.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.ADMISSION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT));
        pagePrivileges.put(CarexRolePageModule.CONSULTATION.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT));
        pagePrivileges.put(CarexRolePageModule.MANAGE_DOCTOR.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.MANAGE_PATIENT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.MANAGE_MEDICINE.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.ADD, RolePrivilegeConstants.EDIT,
                        RolePrivilegeConstants.DELETE, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.PATIENT_HISTORY_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.PRESCRIPTION_HISTORY_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.REVENUE_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.PATIENT_INCOMING_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.CLINIC_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.INVOICE_HISTORY_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.MEDICINE_REPORT.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EXPORT));
        pagePrivileges.put(CarexRolePageModule.CLINIC_SETTINGS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EDIT));
        pagePrivileges.put(CarexRolePageModule.PRESCRIPTION_SETTINGS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EDIT));
        pagePrivileges.put(CarexRolePageModule.INVOICE_SETTINGS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EDIT));
        pagePrivileges.put(CarexRolePageModule.MEDICAL_CERTIFICATE_SETTINGS.getValue(),
                EnumSet.of(RolePrivilegeConstants.VIEW, RolePrivilegeConstants.EDIT));

        return pagePrivileges;
    }

    private static void addDefaultEntries(Map<String, EnumSet<RolePrivilegeConstants>> pagePrivileges,
            List<String> subModules) {
        for (String subModule : subModules) {
            pagePrivileges.putIfAbsent(subModule, ALL_PRIVILEGES);
        }
    }
}

