package com.web.carex.menu;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.web.coretix.menu.AppMenuGroup;
import com.web.coretix.menu.AppMenuItem;
import com.web.coretix.menu.MenuContributor;

@ApplicationScoped
public class CarexMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup clinicManagement = new AppMenuGroup("carex_clinic_management", "Clinic Management", "pi pi-fw pi-briefcase",
                100, "true")
                .addItem(new AppMenuItem("carex_dashboard", "Dashboard", "pi pi-fw pi-home",
                        "/pages/carex/clinic-management/dashboard.xhtml", 10, "true"))
                .addItem(new AppMenuItem("carex_consultation", "Consultation", "pi pi-fw pi-comments",
                        "/pages/carex/clinic-management/consultation.xhtml", 20, "true"))
                .addItem(new AppMenuItem("carex_manage_doctor", "Manage Doctor", "pi pi-fw pi-user-edit",
                        "/pages/carex/clinic-management/manage-doctor.xhtml", 30, "true"))
                .addItem(new AppMenuItem("carex_manage_patient", "Manage Patient", "pi pi-fw pi-users",
                        "/pages/carex/clinic-management/manage-patient.xhtml", 40, "true"))
                .addItem(new AppMenuItem("carex_manage_medicine", "Manage Medicine", "pi pi-fw pi-box",
                        "/pages/carex/clinic-management/manage-medicine.xhtml", 50, "true"))
                .addItem(new AppMenuItem("carex_reports", "Reports", "pi pi-fw pi-chart-bar",
                        null, 60, "true")
                        .addItem(new AppMenuItem("carex_report_patient_history", "Patient History", "pi pi-fw pi-book",
                                "/pages/carex/reports/patient-history.xhtml", 10, "true"))
                        .addItem(new AppMenuItem("carex_report_prescription_history", "Prescription History", "pi pi-fw pi-file",
                                "/pages/carex/reports/prescription-history.xhtml", 20, "true"))
                        .addItem(new AppMenuItem("carex_report_revenue", "Revenue Report", "pi pi-fw pi-dollar",
                                "/pages/carex/reports/revenue-report.xhtml", 30, "true"))
                        .addItem(new AppMenuItem("carex_report_patient_incoming", "Patient Incoming Report", "pi pi-fw pi-clock",
                                "/pages/carex/reports/patient-incoming-report.xhtml", 40, "true"))
                        .addItem(new AppMenuItem("carex_report_clinic", "Clinic Report", "pi pi-fw pi-chart-bar",
                                "/pages/carex/reports/clinic-report.xhtml", 50, "true"))
                        .addItem(new AppMenuItem("carex_report_invoice_history", "Invoice History", "pi pi-fw pi-receipt",
                                "/pages/carex/reports/invoice-history.xhtml", 60, "true"))
                        .addItem(new AppMenuItem("carex_report_medicine", "Medicine Report", "pi pi-fw pi-table",
                                "/pages/carex/reports/medicine-report.xhtml", 70, "true")))
                .addItem(new AppMenuItem("carex_settings", "Settings", "pi pi-fw pi-cog",
                        null, 70, "true")
                        .addItem(new AppMenuItem("carex_settings_clinic", "Clinic Settings", "pi pi-fw pi-cog",
                                "/pages/carex/settings/clinic-settings.xhtml", 10, "true"))
                        .addItem(new AppMenuItem("carex_settings_prescription", "Prescription Settings", "pi pi-fw pi-file-edit",
                                "/pages/carex/settings/prescription-settings.xhtml", 20, "true"))
                        .addItem(new AppMenuItem("carex_settings_invoice", "Invoice Settings", "pi pi-fw pi-file-o",
                                "/pages/carex/settings/invoice-settings.xhtml", 30, "true"))
                        .addItem(new AppMenuItem("carex_settings_medical_certificate", "Medical Certificate Settings", "pi pi-fw pi-id-card",
                                "/pages/carex/settings/medical-certificate-settings.xhtml", 40, "true")));

        return Arrays.asList(clinicManagement);
    }
}
