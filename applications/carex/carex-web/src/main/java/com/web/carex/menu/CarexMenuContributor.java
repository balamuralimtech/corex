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
        AppMenuGroup patientCare = new AppMenuGroup("carex_patient_care", "Patient Care", "pi pi-fw pi-heart",
                100, "true")
                .addItem(new AppMenuItem("carex_patient_dashboard", "Patient Dashboard", "pi pi-fw pi-home",
                        "/pages/carex/patient-care/patient-dashboard.xhtml", 10, "true"))
                .addItem(new AppMenuItem("carex_patient_intake", "Patient Intake", "pi pi-fw pi-user-plus",
                        "/pages/carex/patient-care/patient-intake.xhtml", 20, "true"))
                .addItem(new AppMenuItem("carex_clinical_notes", "Clinical Notes", "pi pi-fw pi-file-edit",
                        "/pages/carex/patient-care/clinical-notes.xhtml", 30, "true"));

        AppMenuGroup appointments = new AppMenuGroup("carex_appointments", "Appointments", "pi pi-fw pi-calendar",
                110, "true")
                .addItem(new AppMenuItem("carex_schedule_board", "Schedule Board", "pi pi-fw pi-calendar-plus",
                        "/pages/carex/appointments/schedule-board.xhtml", 10, "true"))
                .addItem(new AppMenuItem("carex_doctor_queue", "Doctor Queue", "pi pi-fw pi-clock",
                        "/pages/carex/appointments/doctor-queue.xhtml", 20, "true"))
                .addItem(new AppMenuItem("carex_followup_tracker", "Follow-up Tracker", "pi pi-fw pi-refresh",
                        "/pages/carex/appointments/followup-tracker.xhtml", 30, "true"));

        AppMenuGroup pharmacyDesk = new AppMenuGroup("carex_pharmacy_desk", "Pharmacy Desk",
                "pi pi-fw pi-shopping-bag", 120, "true")
                .addItem(new AppMenuItem("carex_prescription_queue", "Prescription Queue", "pi pi-fw pi-list",
                        "/pages/carex/pharmacy-desk/prescription-queue.xhtml", 10, "true"))
                .addItem(new AppMenuItem("carex_dispense_board", "Dispense Board", "pi pi-fw pi-send",
                        "/pages/carex/pharmacy-desk/dispense-board.xhtml", 20, "true"))
                .addItem(new AppMenuItem("carex_stock_alerts", "Stock Alerts", "pi pi-fw pi-bell",
                        "/pages/carex/pharmacy-desk/stock-alerts.xhtml", 30, "true"));

        return Arrays.asList(patientCare, appointments, pharmacyDesk);
    }
}
