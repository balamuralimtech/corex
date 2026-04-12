package com.web.carex.menu;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import com.web.coretix.menu.AppMenuGroup;
import com.web.coretix.menu.AppMenuItem;
import com.web.coretix.menu.MenuContributor;

@Named
public class CarexMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup clinicManagement = new AppMenuGroup(
                "carex_clinic_management",
                "#{i18n.app('clinicManagementLabel')}",
                "pi pi-fw pi-briefcase",
                100,
                "true")
                .addItem(new AppMenuItem(
                        "carex_dashboard",
                        "#{i18n.app('dashboardLabel')}",
                        "pi pi-fw pi-home",
                        "/pages/carex/clinic-management/dashboard.xhtml",
                        10,
                        "true"))
                .addItem(new AppMenuItem(
                        "carex_admission",
                        "Admission",
                        "pi pi-fw pi-ticket",
                        "/pages/carex/clinic-management/admission.xhtml",
                        20,
                        "true"))
                .addItem(new AppMenuItem(
                        "carex_consultation",
                        "#{i18n.app('consultationTitle')}",
                        "pi pi-fw pi-comments",
                        "/pages/carex/clinic-management/consultation.xhtml",
                        30,
                        "true"))
                .addItem(new AppMenuItem(
                        "carex_manage_doctor",
                        "#{i18n.app('manageDoctorTitle')}",
                        "pi pi-fw pi-user-edit",
                        "/pages/carex/clinic-management/manage-doctor.xhtml",
                        40,
                        "true"))
                .addItem(new AppMenuItem(
                        "carex_manage_patient",
                        "#{i18n.app('managePatientTitle')}",
                        "pi pi-fw pi-users",
                        "/pages/carex/clinic-management/manage-patient.xhtml",
                        50,
                        "true"))
                .addItem(new AppMenuItem(
                        "carex_manage_medicine",
                        "#{i18n.app('manageMedicineTitle')}",
                        "pi pi-fw pi-box",
                        "/pages/carex/clinic-management/manage-medicine.xhtml",
                        60,
                        "true"))
                .addItem(new AppMenuItem(
                        "carex_reports",
                        "#{i18n.app('reportsLabel')}",
                        "pi pi-fw pi-chart-bar",
                        null,
                        70,
                        "true")
                        .addItem(new AppMenuItem(
                                "carex_report_patient_history",
                                "#{i18n.app('patientHistoryTitle')}",
                                "pi pi-fw pi-book",
                                "/pages/carex/reports/patient-history.xhtml",
                                10,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_report_prescription_history",
                                "#{i18n.app('prescriptionHistoryTitle')}",
                                "pi pi-fw pi-file",
                                "/pages/carex/reports/prescription-history.xhtml",
                                20,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_report_revenue",
                                "#{i18n.app('revenueReportTitle')}",
                                "pi pi-fw pi-dollar",
                                "/pages/carex/reports/revenue-report.xhtml",
                                30,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_report_patient_incoming",
                                "#{i18n.app('patientIncomingReportTitle')}",
                                "pi pi-fw pi-clock",
                                "/pages/carex/reports/patient-incoming-report.xhtml",
                                40,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_report_clinic",
                                "#{i18n.app('clinicReportTitle')}",
                                "pi pi-fw pi-chart-bar",
                                "/pages/carex/reports/clinic-report.xhtml",
                                50,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_report_invoice_history",
                                "#{i18n.app('invoiceHistoryTitle')}",
                                "pi pi-fw pi-receipt",
                                "/pages/carex/reports/invoice-history.xhtml",
                                60,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_report_medicine",
                                "#{i18n.app('medicineReportTitle')}",
                                "pi pi-fw pi-table",
                                "/pages/carex/reports/medicine-report.xhtml",
                                70,
                                "true")))
                .addItem(new AppMenuItem(
                        "carex_settings",
                        "#{i18n.app('settingsLabel')}",
                        "pi pi-fw pi-cog",
                        null,
                        80,
                        "true")
                        .addItem(new AppMenuItem(
                                "carex_settings_clinic",
                                "#{i18n.app('clinicSettingsPageTitle')}",
                                "pi pi-fw pi-cog",
                                "/pages/carex/settings/clinic-settings.xhtml",
                                10,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_settings_prescription",
                                "#{i18n.app('prescriptionSettingsPageTitle')}",
                                "pi pi-fw pi-file-edit",
                                "/pages/carex/settings/prescription-settings.xhtml",
                                20,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_settings_invoice",
                                "#{i18n.app('invoiceSettingsPageTitle')}",
                                "pi pi-fw pi-file-o",
                                "/pages/carex/settings/invoice-settings.xhtml",
                                30,
                                "true"))
                        .addItem(new AppMenuItem(
                                "carex_settings_medical_certificate",
                                "#{i18n.app('medicalCertificateSettingsPageTitle')}",
                                "pi pi-fw pi-id-card",
                                "/pages/carex/settings/medical-certificate-settings.xhtml",
                                40,
                                "true")));

        return Arrays.asList(clinicManagement);
    }
}
