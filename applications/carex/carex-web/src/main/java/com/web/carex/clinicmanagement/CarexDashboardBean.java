package com.web.carex.clinicmanagement;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IDoctorService;
import com.module.carex.clinicmanagement.IMedicineService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.Doctor;
import com.persist.carex.clinicmanagement.Medicine;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.carex.appgeneral.CarexManagedBean;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Named("carexDashboardBean")
@Scope("session")
public class CarexDashboardBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IDoctorService doctorService;

    @Inject
    private IPatientService patientService;

    @Inject
    private IMedicineService medicineService;

    @Inject
    private IConsultationService consultationService;

    @Inject
    private IClinicSettingsService clinicSettingsService;

    private boolean initialized;
    private boolean timerEnabled = true;
    private Integer selectedOrganizationId;
    private List<Organizations> organizationList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private List<Patient> patientList = new ArrayList<>();
    private List<Medicine> medicineList = new ArrayList<>();
    private List<Consultation> consultationList = new ArrayList<>();
    private List<Consultation> recentConsultations = new ArrayList<>();
    private ClinicSettings clinicSettings = new ClinicSettings();

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        selectedOrganizationId = resolveDefaultOrganizationId(organizationList, selectedOrganizationId);
        refreshDashboard();
        initialized = true;
    }

    public void refreshDashboard() {
        if (selectedOrganizationId == null) {
            doctorList = new ArrayList<>();
            patientList = new ArrayList<>();
            medicineList = new ArrayList<>();
            consultationList = new ArrayList<>();
            recentConsultations = new ArrayList<>();
            clinicSettings = new ClinicSettings();
            return;
        }
        doctorList = new ArrayList<>(doctorService.getDoctorsByOrganizationId(selectedOrganizationId));
        patientList = new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
        medicineList = new ArrayList<>(medicineService.getMedicinesByOrganizationId(selectedOrganizationId));
        consultationList = new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId));
        consultationList.sort(Comparator.comparing(Consultation::getConsultationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        recentConsultations = consultationList.stream().limit(8).collect(Collectors.toList());
        clinicSettings = fallbackClinicSettings(clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId));
    }

    public String getDashboardInsights() {
        if (selectedOrganizationId == null) {
            return "Select an organization to surface clinic performance, patient flow, document volume, and stock signals.";
        }
        return String.format(Locale.US,
                "%s is currently tracking %d consultations, %d active doctors, and %d active patients. %s revenue conversion sits at %s with %d low-stock medicines already requiring attention.",
                getSelectedOrganizationName(),
                getTotalConsultations(),
                getActiveDoctors(),
                getActivePatients(),
                getPreviewCurrencySymbol().isEmpty() ? "Clinic" : "Clinic",
                formatCurrency(getTotalRevenue()),
                getLowStockMedicines());
    }

    public String getSelectedOrganizationName() {
        if (selectedOrganizationId == null) {
            return "Clinic";
        }
        for (Organizations organization : organizationList) {
            if (organization.getId() == selectedOrganizationId) {
                return safeText(organization.getOrganizationName(), "Clinic");
            }
        }
        return "Clinic";
    }

    public String getPreviewCurrencySymbol() {
        return safeText(clinicSettings.getBaseCurrencySymbol(), "");
    }

    public int getTotalDoctors() {
        return doctorList.size();
    }

    public int getActiveDoctors() {
        return (int) doctorList.stream().filter(Doctor::isActive).count();
    }

    public int getLinkedDoctors() {
        return (int) doctorList.stream().filter(doctor -> doctor.getUserDetail() != null).count();
    }

    public int getTotalPatients() {
        return patientList.size();
    }

    public int getActivePatients() {
        return (int) patientList.stream().filter(Patient::isActive).count();
    }

    public int getTotalMedicines() {
        return medicineList.size();
    }

    public int getLowStockMedicines() {
        return (int) medicineList.stream()
                .filter(medicine -> medicine.getStockQuantity() != null && medicine.getReorderLevel() != null)
                .filter(medicine -> medicine.getStockQuantity() <= medicine.getReorderLevel())
                .count();
    }

    public int getTotalConsultations() {
        return consultationList.size();
    }

    public int getTodayConsultations() {
        LocalDate today = LocalDate.now();
        return (int) consultationList.stream()
                .filter(consultation -> consultation.getConsultationDate() != null)
                .filter(consultation -> consultation.getConsultationDate().toLocalDateTime().toLocalDate().equals(today))
                .count();
    }

    public int getConsultationsWithInvoice() {
        return (int) consultationList.stream().filter(Consultation::isIssueInvoice).count();
    }

    public int getConsultationsWithMedicalCertificate() {
        return (int) consultationList.stream().filter(Consultation::isIssueMedicalCertificate).count();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Consultation consultation : consultationList) {
            total = total.add(consultation.getInvoiceTotal() == null ? BigDecimal.ZERO : consultation.getInvoiceTotal());
        }
        return scaleAmount(total);
    }

    public BigDecimal getAverageRevenuePerConsultation() {
        if (consultationList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return scaleAmount(getTotalRevenue().divide(BigDecimal.valueOf(consultationList.size()), 2, RoundingMode.HALF_UP));
    }

    public BigDecimal getAverageMedicineValue() {
        if (consultationList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Consultation consultation : consultationList) {
            total = total.add(consultation.getMedicineTotal() == null ? BigDecimal.ZERO : consultation.getMedicineTotal());
        }
        return scaleAmount(total.divide(BigDecimal.valueOf(consultationList.size()), 2, RoundingMode.HALF_UP));
    }

    public double getInvoiceCoverageRate() {
        return percentage(getConsultationsWithInvoice(), getTotalConsultations());
    }

    public double getMedicalCertificateRate() {
        return percentage(getConsultationsWithMedicalCertificate(), getTotalConsultations());
    }

    public double getDoctorUtilizationRate() {
        return percentage(getActiveDoctors(), getTotalDoctors());
    }

    public double getPatientActivationRate() {
        return percentage(getActivePatients(), getTotalPatients());
    }

    public String getQuickHighlightsJson() {
        return String.format(Locale.US,
                "[%.2f, %.2f, %.2f, %.2f]",
                getDoctorUtilizationRate(),
                getPatientActivationRate(),
                getInvoiceCoverageRate(),
                getMedicalCertificateRate());
    }

    public String getClinicMixJson() {
        return String.format(Locale.US,
                "[['Doctors', %d], ['Patients', %d], ['Medicines', %d], ['Consultations', %d]]",
                getTotalDoctors(),
                getTotalPatients(),
                getTotalMedicines(),
                getTotalConsultations());
    }

    public String getDocumentMixJson() {
        return String.format(Locale.US,
                "[['Prescription', %d], ['Invoice', %d], ['Medical Certificate', %d]]",
                getTotalConsultations(),
                getConsultationsWithInvoice(),
                getConsultationsWithMedicalCertificate());
    }

    public String getPatientGenderJson() {
        Map<String, Long> genderCounts = patientList.stream()
                .collect(Collectors.groupingBy(patient -> {
                    String gender = safeText(patient.getGender(), "Unspecified");
                    return gender.isEmpty() ? "Unspecified" : gender;
                }, LinkedHashMap::new, Collectors.counting()));
        if (genderCounts.isEmpty()) {
            return "[]";
        }
        return genderCounts.entrySet().stream()
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getStockStatusJson() {
        int healthy = 0;
        int low = 0;
        int critical = 0;
        for (Medicine medicine : medicineList) {
            int stock = medicine.getStockQuantity() == null ? 0 : medicine.getStockQuantity();
            int reorder = medicine.getReorderLevel() == null ? 0 : medicine.getReorderLevel();
            if (stock <= 0) {
                critical++;
            } else if (stock <= reorder) {
                low++;
            } else {
                healthy++;
            }
        }
        return String.format(Locale.US,
                "[['Healthy', %d], ['Low', %d], ['Critical', %d]]",
                healthy, low, critical);
    }

    public String getWeeklyRevenueJson() {
        Map<LocalDate, BigDecimal> revenueMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            revenueMap.put(today.minusDays(dayOffset), BigDecimal.ZERO);
        }
        for (Consultation consultation : consultationList) {
            if (consultation.getConsultationDate() == null) {
                continue;
            }
            LocalDate date = consultation.getConsultationDate().toLocalDateTime().toLocalDate();
            if (!revenueMap.containsKey(date)) {
                continue;
            }
            BigDecimal current = revenueMap.get(date);
            revenueMap.put(date, current.add(consultation.getInvoiceTotal() == null ? BigDecimal.ZERO : consultation.getInvoiceTotal()));
        }
        return revenueMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)) + "', " + scaleAmount(entry.getValue()) + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getRecentConsultationVolumeJson() {
        Map<LocalDate, Integer> countMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            countMap.put(today.minusDays(dayOffset), 0);
        }
        for (Consultation consultation : consultationList) {
            if (consultation.getConsultationDate() == null) {
                continue;
            }
            LocalDate date = consultation.getConsultationDate().toLocalDateTime().toLocalDate();
            if (!countMap.containsKey(date)) {
                continue;
            }
            countMap.put(date, countMap.get(date) + 1);
        }
        return countMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getDoctorSpecializationJson() {
        Map<String, Long> specializationCounts = doctorList.stream()
                .collect(Collectors.groupingBy(doctor -> {
                    String specialization = safeText(doctor.getSpecialization(), "General Practice");
                    return specialization.isEmpty() ? "General Practice" : specialization;
                }, Collectors.counting()));
        if (specializationCounts.isEmpty()) {
            return "[]";
        }
        return specializationCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getPatientAgeGroupJson() {
        Map<String, Integer> ageGroups = new LinkedHashMap<>();
        ageGroups.put("0-12", 0);
        ageGroups.put("13-25", 0);
        ageGroups.put("26-40", 0);
        ageGroups.put("41-60", 0);
        ageGroups.put("60+", 0);
        ageGroups.put("Unknown", 0);
        LocalDate today = LocalDate.now();
        for (Patient patient : patientList) {
            if (patient.getDateOfBirth() == null) {
                ageGroups.put("Unknown", ageGroups.get("Unknown") + 1);
                continue;
            }
            int age = today.getYear() - patient.getDateOfBirth().toLocalDate().getYear();
            if (age <= 12) {
                ageGroups.put("0-12", ageGroups.get("0-12") + 1);
            } else if (age <= 25) {
                ageGroups.put("13-25", ageGroups.get("13-25") + 1);
            } else if (age <= 40) {
                ageGroups.put("26-40", ageGroups.get("26-40") + 1);
            } else if (age <= 60) {
                ageGroups.put("41-60", ageGroups.get("41-60") + 1);
            } else {
                ageGroups.put("60+", ageGroups.get("60+") + 1);
            }
        }
        return ageGroups.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getOpenSignals() {
        return String.format(Locale.US,
                "%d low-stock items, %d consultations requiring invoices, %d consultations requiring medical certificates, and %d doctors already linked to user accounts.",
                getLowStockMedicines(),
                getConsultationsWithInvoice(),
                getConsultationsWithMedicalCertificate(),
                getLinkedDoctors());
    }

    public String formatCurrency(BigDecimal amount) {
        String value = scaleAmount(amount).toPlainString();
        return (getPreviewCurrencySymbol().isEmpty() ? "" : getPreviewCurrencySymbol() + " ") + value;
    }

    public String getRecentConsultationDate(Consultation consultation) {
        if (consultation == null || consultation.getConsultationDate() == null) {
            return "-";
        }
        return consultation.getConsultationDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = resolveAccessibleOrganizationId(selectedOrganizationId);
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public List<Consultation> getRecentConsultations() {
        return recentConsultations;
    }

    private ClinicSettings fallbackClinicSettings(ClinicSettings settings) {
        return settings == null ? new ClinicSettings() : settings;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private BigDecimal scaleAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private double percentage(double part, double total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round((part / total) * 10000.0) / 100.0;
    }

    private String escapeJs(String value) {
        return safeText(value, "").replace("\\", "\\\\").replace("'", "\\'");
    }
}
