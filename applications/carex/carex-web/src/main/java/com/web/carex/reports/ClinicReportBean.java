package com.web.carex.reports;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.springframework.context.annotation.Scope;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Named("clinicReportBean")
@Scope("session")
public class ClinicReportBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_SHORT_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IPatientService patientService;

    @Inject
    private IConsultationService consultationService;

    @Inject
    private IClinicSettingsService clinicSettingsService;

    private boolean initialized;
    private Integer selectedOrganizationId;
    private String periodScope = "MONTHLY";
    private java.util.Date reportReferenceDate = new java.util.Date();

    private List<Organizations> organizationList = new ArrayList<>();
    private List<Patient> patientList = new ArrayList<>();
    private List<Consultation> reportConsultations = new ArrayList<>();
    private List<ClinicPatientSummaryRow> patientSummaryRows = new ArrayList<>();
    private ClinicSettings clinicSettings = new ClinicSettings();

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        organizationList = new ArrayList<>(organizationService.getOrganizationsList());
        if (selectedOrganizationId == null) {
            selectedOrganizationId = resolveCurrentOrganizationId();
        }
        onOrganizationChange();
        initialized = true;
    }

    public void onOrganizationChange() {
        patientList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
        clinicSettings = selectedOrganizationId == null
                ? new ClinicSettings()
                : fallbackClinicSettings(clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId));
        reportConsultations = new ArrayList<>();
        patientSummaryRows = new ArrayList<>();
    }

    public void fetchReport() {
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization to fetch clinic analytics.");
            reportConsultations = new ArrayList<>();
            patientSummaryRows = new ArrayList<>();
            return;
        }

        LocalDate referenceDate = resolveReferenceDate();
        reportConsultations = new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId)).stream()
                .filter(consultation -> consultation.getConsultationDate() != null)
                .filter(consultation -> matchesPeriodScope(consultation, referenceDate))
                .sorted(Comparator.comparing(Consultation::getConsultationDate).reversed())
                .collect(Collectors.toList());

        patientSummaryRows = buildPatientSummaryRows(reportConsultations);
        if (reportConsultations.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_INFO, "No data", "No clinic consultation activity matched the selected criteria.");
        }
    }

    public int getTotalPatients() {
        return patientList.size();
    }

    public int getActivePatients() {
        return (int) patientList.stream().filter(Patient::isActive).count();
    }

    public int getConsultationCount() {
        return reportConsultations.size();
    }

    public int getPatientsVisitedCount() {
        return patientSummaryRows.size();
    }

    public int getReturningPatientsCount() {
        return (int) patientSummaryRows.stream().filter(row -> row.getConsultationCount() > 1).count();
    }

    public String getAverageVisitsPerPatientLabel() {
        if (patientSummaryRows.isEmpty()) {
            return "0.00";
        }
        double average = (double) reportConsultations.size() / (double) patientSummaryRows.size();
        return String.format(Locale.ENGLISH, "%.2f", average);
    }

    public String getMostCommonAgeGroupLabel() {
        return topLabelFromMap(getAgeGroupCounts());
    }

    public String getMostCommonGenderLabel() {
        return topLabelFromMap(getGenderCounts());
    }

    public String getReportWindowLabel() {
        LocalDate referenceDate = resolveReferenceDate();
        if ("WEEKLY".equals(periodScope)) {
            LocalDate start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate end = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            return start.format(DATE_LABEL_FORMATTER) + " - " + end.format(DATE_LABEL_FORMATTER);
        }
        if ("MONTHLY".equals(periodScope)) {
            return referenceDate.getMonth().name().substring(0, 1)
                    + referenceDate.getMonth().name().substring(1).toLowerCase(Locale.ENGLISH)
                    + " " + referenceDate.getYear();
        }
        if ("YEARLY".equals(periodScope)) {
            return String.valueOf(referenceDate.getYear());
        }
        return referenceDate.format(DATE_LABEL_FORMATTER);
    }

    public String getAgeGroupJson() {
        return toJson(getAgeGroupCounts());
    }

    public String getGenderMixJson() {
        return toJson(getGenderCounts());
    }

    public String getVisitFrequencyJson() {
        LinkedHashMap<String, Integer> frequency = new LinkedHashMap<>();
        frequency.put("1 Visit", 0);
        frequency.put("2-3 Visits", 0);
        frequency.put("4-6 Visits", 0);
        frequency.put("7+ Visits", 0);

        for (ClinicPatientSummaryRow row : patientSummaryRows) {
            int count = row.getConsultationCount();
            if (count <= 1) {
                frequency.put("1 Visit", frequency.get("1 Visit") + 1);
            } else if (count <= 3) {
                frequency.put("2-3 Visits", frequency.get("2-3 Visits") + 1);
            } else if (count <= 6) {
                frequency.put("4-6 Visits", frequency.get("4-6 Visits") + 1);
            } else {
                frequency.put("7+ Visits", frequency.get("7+ Visits") + 1);
            }
        }
        return toJson(frequency);
    }

    public String getConsultationTrendJson() {
        LinkedHashMap<String, Integer> countMap = new LinkedHashMap<>();
        List<Consultation> ascending = reportConsultations.stream()
                .sorted(Comparator.comparing(Consultation::getConsultationDate))
                .collect(Collectors.toList());
        for (Consultation consultation : ascending) {
            String label = consultation.getConsultationDate().toLocalDateTime().toLocalDate().format(DAY_SHORT_FORMATTER);
            countMap.put(label, countMap.getOrDefault(label, 0) + 1);
        }
        return toJson(countMap);
    }

    public String getTopPatientVisitsJson() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (ClinicPatientSummaryRow row : patientSummaryRows.stream()
                .sorted(Comparator.comparingInt(ClinicPatientSummaryRow::getConsultationCount).reversed())
                .limit(8)
                .collect(Collectors.toList())) {
            counts.put(safeText(row.getPatientName(), "Patient"), row.getConsultationCount());
        }
        return toJson(counts);
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = selectedOrganizationId;
    }

    public String getPeriodScope() {
        return periodScope;
    }

    public void setPeriodScope(String periodScope) {
        this.periodScope = periodScope;
    }

    public java.util.Date getReportReferenceDate() {
        return reportReferenceDate;
    }

    public void setReportReferenceDate(java.util.Date reportReferenceDate) {
        this.reportReferenceDate = reportReferenceDate;
    }

    public List<ClinicPatientSummaryRow> getPatientSummaryRows() {
        return patientSummaryRows;
    }

    private List<ClinicPatientSummaryRow> buildPatientSummaryRows(List<Consultation> consultations) {
        Map<Integer, List<Consultation>> grouped = consultations.stream()
                .filter(consultation -> consultation.getPatient() != null && consultation.getPatient().getId() != null)
                .collect(Collectors.groupingBy(consultation -> consultation.getPatient().getId()));

        List<ClinicPatientSummaryRow> rows = new ArrayList<>();
        for (Map.Entry<Integer, List<Consultation>> entry : grouped.entrySet()) {
            List<Consultation> patientConsultations = entry.getValue().stream()
                    .sorted(Comparator.comparing(Consultation::getConsultationDate))
                    .collect(Collectors.toList());
            Patient patient = patientConsultations.get(0).getPatient();
            ClinicPatientSummaryRow row = new ClinicPatientSummaryRow();
            row.setPatientCode(patient.getPatientCode());
            row.setPatientName(patient.getPatientName());
            row.setGender(safeText(patient.getGender(), "Unspecified"));
            row.setAgeLabel(resolveAgeLabel(patient.getDateOfBirth()));
            row.setAgeGroup(resolveAgeGroup(patient));
            row.setConsultationCount(patientConsultations.size());
            row.setFirstVisit(patientConsultations.get(0).getConsultationDate());
            row.setLastVisit(patientConsultations.get(patientConsultations.size() - 1).getConsultationDate());
            rows.add(row);
        }

        rows.sort(Comparator.comparingInt(ClinicPatientSummaryRow::getConsultationCount).reversed()
                .thenComparing(ClinicPatientSummaryRow::getPatientName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return rows;
    }

    private LinkedHashMap<String, Integer> getAgeGroupCounts() {
        LinkedHashMap<String, Integer> ageGroups = new LinkedHashMap<>();
        ageGroups.put("0-12", 0);
        ageGroups.put("13-25", 0);
        ageGroups.put("26-40", 0);
        ageGroups.put("41-60", 0);
        ageGroups.put("60+", 0);
        ageGroups.put("Unknown", 0);

        for (Patient patient : patientList) {
            String ageGroup = resolveAgeGroup(patient);
            ageGroups.put(ageGroup, ageGroups.get(ageGroup) + 1);
        }
        return ageGroups;
    }

    private LinkedHashMap<String, Integer> getGenderCounts() {
        LinkedHashMap<String, Integer> genderCounts = new LinkedHashMap<>();
        for (Patient patient : patientList) {
            String gender = safeText(patient.getGender(), "Unspecified");
            genderCounts.put(gender, genderCounts.getOrDefault(gender, 0) + 1);
        }
        return genderCounts;
    }

    private String resolveAgeGroup(Patient patient) {
        if (patient == null || patient.getDateOfBirth() == null) {
            return "Unknown";
        }
        int age = resolveAge(patient.getDateOfBirth());
        if (age <= 12) {
            return "0-12";
        }
        if (age <= 25) {
            return "13-25";
        }
        if (age <= 40) {
            return "26-40";
        }
        if (age <= 60) {
            return "41-60";
        }
        return "60+";
    }

    private String resolveAgeLabel(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return "-";
        }
        return String.valueOf(resolveAge(dateOfBirth));
    }

    private int resolveAge(Date dateOfBirth) {
        LocalDate dob = dateOfBirth.toLocalDate();
        LocalDate today = LocalDate.now();
        int age = today.getYear() - dob.getYear();
        if (today.getDayOfYear() < dob.getDayOfYear()) {
            age--;
        }
        return Math.max(age, 0);
    }

    private String toJson(Map<String, ? extends Number> source) {
        if (source.isEmpty()) {
            return "[]";
        }
        return source.entrySet().stream()
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String topLabelFromMap(Map<String, Integer> source) {
        if (source.isEmpty()) {
            return "-";
        }
        return source.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("-");
    }

    private boolean matchesPeriodScope(Consultation consultation, LocalDate referenceDate) {
        LocalDate consultationDate = consultation.getConsultationDate().toLocalDateTime().toLocalDate();
        switch (periodScope) {
            case "WEEKLY":
                LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                return !consultationDate.isBefore(weekStart) && !consultationDate.isAfter(weekEnd);
            case "MONTHLY":
                return consultationDate.getYear() == referenceDate.getYear()
                        && consultationDate.getMonth() == referenceDate.getMonth();
            case "YEARLY":
                return consultationDate.getYear() == referenceDate.getYear();
            case "DAILY":
            default:
                return consultationDate.equals(referenceDate);
        }
    }

    private LocalDate resolveReferenceDate() {
        java.util.Date source = reportReferenceDate == null ? new java.util.Date() : reportReferenceDate;
        return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        Object organizationId = facesContext.getExternalContext().getSessionMap().get("organizationId");
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private ClinicSettings fallbackClinicSettings(ClinicSettings settings) {
        return settings == null ? new ClinicSettings() : settings;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String escapeJs(String value) {
        return safeText(value, "").replace("\\", "\\\\").replace("'", "\\'");
    }

    public static class ClinicPatientSummaryRow implements Serializable {
        private String patientCode;
        private String patientName;
        private String gender;
        private String ageLabel;
        private String ageGroup;
        private int consultationCount;
        private Timestamp firstVisit;
        private Timestamp lastVisit;

        public String getPatientCode() {
            return patientCode;
        }

        public void setPatientCode(String patientCode) {
            this.patientCode = patientCode;
        }

        public String getPatientName() {
            return patientName;
        }

        public void setPatientName(String patientName) {
            this.patientName = patientName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getAgeLabel() {
            return ageLabel;
        }

        public void setAgeLabel(String ageLabel) {
            this.ageLabel = ageLabel;
        }

        public String getAgeGroup() {
            return ageGroup;
        }

        public void setAgeGroup(String ageGroup) {
            this.ageGroup = ageGroup;
        }

        public int getConsultationCount() {
            return consultationCount;
        }

        public void setConsultationCount(int consultationCount) {
            this.consultationCount = consultationCount;
        }

        public Timestamp getFirstVisit() {
            return firstVisit;
        }

        public void setFirstVisit(Timestamp firstVisit) {
            this.firstVisit = firstVisit;
        }

        public Timestamp getLastVisit() {
            return lastVisit;
        }

        public void setLastVisit(Timestamp lastVisit) {
            this.lastVisit = lastVisit;
        }
    }
}
