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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
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

@Named("patientHistoryReportBean")
@Scope("session")
public class PatientHistoryReportBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

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
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization to fetch patient history.");
            reportConsultations = new ArrayList<>();
            return;
        }
        if ("SPECIFIC".equals(patientScope) && selectedPatientId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient required", "Choose a specific patient for this report scope.");
            reportConsultations = new ArrayList<>();
            return;
        }

        List<Consultation> consultations = new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId));
        LocalDate referenceDate = resolveReferenceDate();
        consultations = consultations.stream()
                .filter(consultation -> consultation.getConsultationDate() != null)
                .filter(consultation -> matchesPatientScope(consultation))
                .filter(consultation -> matchesPeriodScope(consultation, referenceDate))
                .sorted(Comparator.comparing(Consultation::getConsultationDate).reversed())
                .collect(Collectors.toList());

        reportConsultations = consultations;
        if (reportConsultations.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_INFO, "No data", "No consultations matched the selected patient history criteria.");
        }
    }

    public String getPreviewCurrencySymbol() {
        return safeText(clinicSettings.getBaseCurrencySymbol(), "");
    }

    public String formatAmount(BigDecimal amount) {
        BigDecimal scaled = amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
        return (getPreviewCurrencySymbol().isEmpty() ? "" : getPreviewCurrencySymbol() + " ") + scaled.toPlainString();
    }

    public int getConsultationCount() {
        return reportConsultations.size();
    }

    public int getUniquePatientCount() {
        return (int) reportConsultations.stream()
                .map(consultation -> consultation.getPatient() == null ? null : consultation.getPatient().getId())
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    public int getInvoiceCount() {
        return (int) reportConsultations.stream().filter(Consultation::isIssueInvoice).count();
    }

    public int getMedicalCertificateCount() {
        return (int) reportConsultations.stream().filter(Consultation::isIssueMedicalCertificate).count();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Consultation consultation : reportConsultations) {
            total = total.add(consultation.getInvoiceTotal() == null ? BigDecimal.ZERO : consultation.getInvoiceTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAverageRevenue() {
        if (reportConsultations.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return getTotalRevenue().divide(BigDecimal.valueOf(reportConsultations.size()), 2, RoundingMode.HALF_UP);
    }

    public String getReportWindowLabel() {
        LocalDate referenceDate = resolveReferenceDate();
        if ("WEEKLY".equals(periodScope)) {
            LocalDate start = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate end = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            return start.format(DATE_LABEL_FORMATTER) + " - " + end.format(DATE_LABEL_FORMATTER);
        }
        if ("MONTHLY".equals(periodScope)) {
            return referenceDate.getMonth().name().substring(0, 1) + referenceDate.getMonth().name().substring(1).toLowerCase(Locale.ENGLISH)
                    + " " + referenceDate.getYear();
        }
        if ("YEARLY".equals(periodScope)) {
            return String.valueOf(referenceDate.getYear());
        }
        return referenceDate.format(DATE_LABEL_FORMATTER);
    }

    public String getPatientMixJson() {
        Map<String, Long> mix = reportConsultations.stream()
                .filter(consultation -> consultation.getPatient() != null)
                .collect(Collectors.groupingBy(consultation -> safeText(consultation.getPatient().getPatientName(), "Unknown"), Collectors.counting()));
        if (mix.isEmpty()) {
            return "[]";
        }
        return mix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getDailyVolumeJson() {
        LinkedHashMap<String, Integer> volumeMap = new LinkedHashMap<>();
        for (Consultation consultation : reportConsultations) {
            String label = consultation.getConsultationDate().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
            volumeMap.put(label, volumeMap.getOrDefault(label, 0) + 1);
        }
        if (volumeMap.isEmpty()) {
            return "[]";
        }
        return volumeMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getRevenueTrendJson() {
        LinkedHashMap<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        for (Consultation consultation : reportConsultations) {
            String label = consultation.getConsultationDate().toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
            BigDecimal current = revenueMap.getOrDefault(label, BigDecimal.ZERO);
            revenueMap.put(label, current.add(consultation.getInvoiceTotal() == null ? BigDecimal.ZERO : consultation.getInvoiceTotal()));
        }
        if (revenueMap.isEmpty()) {
            return "[]";
        }
        return revenueMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue().setScale(2, RoundingMode.HALF_UP).toPlainString() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
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
