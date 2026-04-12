package com.web.carex.reports;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.carex.appgeneral.CarexManagedBean;
import org.springframework.context.annotation.Scope;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Named("patientIncomingReportBean")
@Scope("session")
public class PatientIncomingReportBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter HOUR_LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:00", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    @Inject
    private transient IOrganizationService organizationService;

    @Inject
    private transient IPatientService patientService;

    @Inject
    private transient IConsultationService consultationService;

    @Inject
    private transient IClinicSettingsService clinicSettingsService;

    private boolean initialized;
    private Integer selectedOrganizationId;
    private Integer selectedPatientId;
    private String patientScope = "ALL";
    private String periodScope = "DAILY";
    private Date reportReferenceDate = new Date();

    private List<Organizations> organizationList = new ArrayList<>();
    private List<Patient> patientList = new ArrayList<>();
    private List<Consultation> reportConsultations = new ArrayList<>();
    private ClinicSettings clinicSettings = new ClinicSettings();

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        selectedOrganizationId = resolveDefaultOrganizationId(organizationList, selectedOrganizationId);
        onOrganizationChange();
        initialized = true;
    }

    public void onOrganizationChange() {
        selectedOrganizationId = resolveAccessibleOrganizationId(selectedOrganizationId);
        patientList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
        clinicSettings = selectedOrganizationId == null
                ? new ClinicSettings()
                : fallbackClinicSettings(clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId));
        if (!"SPECIFIC".equals(patientScope)) {
            selectedPatientId = null;
        }
        reportConsultations = new ArrayList<>();
    }

    public void onPatientScopeChange() {
        if (!"SPECIFIC".equals(patientScope)) {
            selectedPatientId = null;
        }
    }

    public void fetchReport() {
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization to fetch patient incoming data.");
            reportConsultations = new ArrayList<>();
            return;
        }
        if ("SPECIFIC".equals(patientScope) && selectedPatientId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient required", "Choose a patient for a specific incoming report.");
            reportConsultations = new ArrayList<>();
            return;
        }

        LocalDate referenceDate = resolveReferenceDate();
        reportConsultations = new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId)).stream()
                .filter(consultation -> consultation.getConsultationDate() != null)
                .filter(this::matchesPatientScope)
                .filter(consultation -> matchesPeriodScope(consultation, referenceDate))
                .sorted(Comparator.comparing(Consultation::getConsultationDate).reversed())
                .collect(Collectors.toList());

        if (reportConsultations.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_INFO, "No data", "No patient incoming records matched the selected criteria.");
        }
    }

    public int getIncomingCount() {
        return reportConsultations.size();
    }

    public int getUniquePatientCount() {
        return (int) reportConsultations.stream()
                .map(consultation -> consultation.getPatient() == null ? null : consultation.getPatient().getId())
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    public int getPeakHourCount() {
        return getHourlyCounts().values().stream().max(Integer::compareTo).orElse(0);
    }

    public String getPeakHourLabel() {
        Map<Integer, Integer> hourlyCounts = getHourlyCounts();
        if (hourlyCounts.isEmpty()) {
            return "-";
        }
        Integer peakHour = hourlyCounts.entrySet().stream()
                .max(Map.Entry.<Integer, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .orElse(null);
        return peakHour == null ? "-" : String.format(Locale.ENGLISH, "%02d:00 - %02d:59", peakHour, peakHour);
    }

    public double getAverageIncomingPerHour() {
        if (reportConsultations.isEmpty()) {
            return 0d;
        }
        long occupiedHours = getHourlyCounts().values().stream().filter(count -> count > 0).count();
        if (occupiedHours == 0) {
            return 0d;
        }
        return (double) reportConsultations.size() / (double) occupiedHours;
    }

    public String getAverageIncomingPerHourLabel() {
        return String.format(Locale.ENGLISH, "%.2f", getAverageIncomingPerHour());
    }

    public String getBusiestDayLabel() {
        Map<String, Integer> dailyCounts = getDailyCounts();
        if (dailyCounts.isEmpty()) {
            return "-";
        }
        return dailyCounts.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("-");
    }

    public String getSelectedPatientName() {
        if (!"SPECIFIC".equals(patientScope) || selectedPatientId == null) {
            return "All Patients";
        }
        return patientList.stream()
                .filter(patient -> patient.getId() != null && patient.getId().equals(selectedPatientId))
                .map(patient -> safeText(patient.getPatientName(), "Patient"))
                .findFirst()
                .orElse("Patient");
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

    public String getHourlyIncomingJson() {
        Map<Integer, Integer> hourlyCounts = getHourlyCounts();
        if (hourlyCounts.isEmpty()) {
            return "[]";
        }
        return hourlyCounts.entrySet().stream()
                .map(entry -> "['" + String.format(Locale.ENGLISH, "%02d:00", entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getDailyIncomingJson() {
        Map<String, Integer> dailyCounts = getDailyCounts();
        if (dailyCounts.isEmpty()) {
            return "[]";
        }
        return dailyCounts.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getWeekdayIncomingJson() {
        LinkedHashMap<String, Integer> weekdayCounts = new LinkedHashMap<>();
        weekdayCounts.put("Mon", 0);
        weekdayCounts.put("Tue", 0);
        weekdayCounts.put("Wed", 0);
        weekdayCounts.put("Thu", 0);
        weekdayCounts.put("Fri", 0);
        weekdayCounts.put("Sat", 0);
        weekdayCounts.put("Sun", 0);

        for (Consultation consultation : reportConsultations) {
            DayOfWeek dayOfWeek = consultation.getConsultationDate().toLocalDateTime().getDayOfWeek();
            String key = dayOfWeek.name().substring(0, 1) + dayOfWeek.name().substring(1, 3).toLowerCase(Locale.ENGLISH);
            weekdayCounts.put(key, weekdayCounts.get(key) + 1);
        }

        return weekdayCounts.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getTopPatientsJson() {
        Map<String, Long> patientMix = reportConsultations.stream()
                .filter(consultation -> consultation.getPatient() != null)
                .collect(Collectors.groupingBy(consultation -> safeText(consultation.getPatient().getPatientName(), "Unknown Patient"), Collectors.counting()));
        if (patientMix.isEmpty()) {
            return "[]";
        }
        return patientMix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getConsultationDateLabel(Consultation consultation) {
        if (consultation == null || consultation.getConsultationDate() == null) {
            return "-";
        }
        LocalDateTime dateTime = consultation.getConsultationDate().toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH));
    }

    public String getArrivalHourLabel(Consultation consultation) {
        if (consultation == null || consultation.getConsultationDate() == null) {
            return "-";
        }
        return consultation.getConsultationDate().toLocalDateTime().format(HOUR_LABEL_FORMATTER);
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public List<Patient> getPatientList() {
        return patientList;
    }

    public List<Consultation> getReportConsultations() {
        return reportConsultations;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = selectedOrganizationId;
    }

    public Integer getSelectedPatientId() {
        return selectedPatientId;
    }

    public void setSelectedPatientId(Integer selectedPatientId) {
        this.selectedPatientId = selectedPatientId;
    }

    public String getPatientScope() {
        return patientScope;
    }

    public void setPatientScope(String patientScope) {
        this.patientScope = patientScope;
    }

    public String getPeriodScope() {
        return periodScope;
    }

    public void setPeriodScope(String periodScope) {
        this.periodScope = periodScope;
    }

    public Date getReportReferenceDate() {
        return reportReferenceDate;
    }

    public void setReportReferenceDate(Date reportReferenceDate) {
        this.reportReferenceDate = reportReferenceDate;
    }

    private LinkedHashMap<Integer, Integer> getHourlyCounts() {
        LinkedHashMap<Integer, Integer> hourlyCounts = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            hourlyCounts.put(hour, 0);
        }
        for (Consultation consultation : reportConsultations) {
            int hour = consultation.getConsultationDate().toLocalDateTime().getHour();
            hourlyCounts.put(hour, hourlyCounts.get(hour) + 1);
        }
        return hourlyCounts;
    }

    private LinkedHashMap<String, Integer> getDailyCounts() {
        LinkedHashMap<String, Integer> dailyCounts = new LinkedHashMap<>();
        List<Consultation> ascendingConsultations = reportConsultations.stream()
                .sorted(Comparator.comparing(Consultation::getConsultationDate))
                .collect(Collectors.toList());
        for (Consultation consultation : ascendingConsultations) {
            String label = consultation.getConsultationDate().toLocalDateTime().toLocalDate().format(DAY_LABEL_FORMATTER);
            dailyCounts.put(label, dailyCounts.getOrDefault(label, 0) + 1);
        }
        return dailyCounts;
    }

    private boolean matchesPatientScope(Consultation consultation) {
        if (!"SPECIFIC".equals(patientScope)) {
            return true;
        }
        return consultation.getPatient() != null
                && consultation.getPatient().getId() != null
                && consultation.getPatient().getId().equals(selectedPatientId);
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
        Date source = reportReferenceDate == null ? new Date() : reportReferenceDate;
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
}
