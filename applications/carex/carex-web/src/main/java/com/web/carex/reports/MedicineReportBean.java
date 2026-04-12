package com.web.carex.reports;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.ConsultationMedicine;
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

@Named("medicineReportBean")
@Scope("session")
public class MedicineReportBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

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
    private List<MedicineReportRow> medicineRows = new ArrayList<>();
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
        medicineRows = new ArrayList<>();
    }

    public void onPatientScopeChange() {
        if (!"SPECIFIC".equals(patientScope)) {
            selectedPatientId = null;
        }
    }

    public void fetchReport() {
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization to fetch medicine history.");
            medicineRows = new ArrayList<>();
            return;
        }
        if ("SPECIFIC".equals(patientScope) && selectedPatientId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient required", "Choose a patient for specific medicine history.");
            medicineRows = new ArrayList<>();
            return;
        }

        LocalDate referenceDate = resolveReferenceDate();
        List<Consultation> consultations = new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId));
        List<MedicineReportRow> rows = new ArrayList<>();
        for (Consultation consultation : consultations) {
            if (consultation.getConsultationDate() == null || !matchesPatientScope(consultation) || !matchesPeriodScope(consultation, referenceDate)) {
                continue;
            }
            if (consultation.getConsultationMedicines() == null || consultation.getConsultationMedicines().isEmpty()) {
                continue;
            }
            for (ConsultationMedicine medicine : consultation.getConsultationMedicines()) {
                MedicineReportRow row = new MedicineReportRow();
                row.setConsultation(consultation);
                row.setConsultationMedicine(medicine);
                rows.add(row);
            }
        }
        rows.sort(Comparator.comparing((MedicineReportRow row) -> row.getConsultation().getConsultationDate()).reversed()
                .thenComparing(row -> row.getConsultationMedicine().getLineNumber(), Comparator.nullsLast(Comparator.naturalOrder())));
        medicineRows = rows;
        if (medicineRows.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_INFO, "No data", "No medicine activity matched the selected criteria.");
        }
    }

    public int getMedicineLineCount() {
        return medicineRows.size();
    }

    public int getConsultationCount() {
        return (int) medicineRows.stream().map(row -> row.getConsultation().getId()).distinct().count();
    }

    public int getUniquePatientCount() {
        return (int) medicineRows.stream()
                .map(row -> row.getConsultation().getPatient() == null ? null : row.getConsultation().getPatient().getId())
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    public int getUniqueMedicineCount() {
        return (int) medicineRows.stream()
                .map(row -> row.getConsultationMedicine().getMedicine() == null ? null : row.getConsultationMedicine().getMedicine().getId())
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    public BigDecimal getTotalQuantity() {
        BigDecimal total = BigDecimal.ZERO;
        for (MedicineReportRow row : medicineRows) {
            total = total.add(row.getConsultationMedicine().getQuantity() == null ? BigDecimal.ZERO : row.getConsultationMedicine().getQuantity());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalLineValue() {
        BigDecimal total = BigDecimal.ZERO;
        for (MedicineReportRow row : medicineRows) {
            total = total.add(row.getConsultationMedicine().getLineTotal() == null ? BigDecimal.ZERO : row.getConsultationMedicine().getLineTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAverageLineValue() {
        if (medicineRows.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return getTotalLineValue().divide(BigDecimal.valueOf(medicineRows.size()), 2, RoundingMode.HALF_UP);
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
            return referenceDate.getMonth().name().substring(0, 1) + referenceDate.getMonth().name().substring(1).toLowerCase(Locale.ENGLISH)
                    + " " + referenceDate.getYear();
        }
        if ("YEARLY".equals(periodScope)) {
            return String.valueOf(referenceDate.getYear());
        }
        return referenceDate.format(DATE_LABEL_FORMATTER);
    }

    public String formatAmount(BigDecimal amount) {
        BigDecimal scaled = amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
        String symbol = safeText(clinicSettings.getBaseCurrencySymbol(), "");
        return symbol.isEmpty() ? scaled.toPlainString() : symbol + " " + scaled.toPlainString();
    }

    public String getMedicineMixJson() {
        Map<String, Long> mix = medicineRows.stream()
                .collect(Collectors.groupingBy(row -> resolveMedicineName(row.getConsultationMedicine()), Collectors.counting()));
        if (mix.isEmpty()) {
            return "[]";
        }
        return mix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getCategoryMixJson() {
        Map<String, Long> mix = medicineRows.stream()
                .collect(Collectors.groupingBy(row -> {
                    if (row.getConsultationMedicine().getMedicine() == null) {
                        return "Uncategorized";
                    }
                    return safeText(row.getConsultationMedicine().getMedicine().getCategory(), "Uncategorized");
                }, Collectors.counting()));
        if (mix.isEmpty()) {
            return "[]";
        }
        return mix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getDailyMedicineJson() {
        LinkedHashMap<String, Integer> volumeMap = new LinkedHashMap<>();
        for (MedicineReportRow row : medicineRows) {
            String label = row.getConsultation().getConsultationDate().toLocalDateTime().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
            volumeMap.put(label, volumeMap.getOrDefault(label, 0) + 1);
        }
        if (volumeMap.isEmpty()) {
            return "[]";
        }
        return volumeMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getMedicineValueJson() {
        LinkedHashMap<String, BigDecimal> valueMap = new LinkedHashMap<>();
        for (MedicineReportRow row : medicineRows) {
            String label = row.getConsultation().getConsultationDate().toLocalDateTime().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
            BigDecimal current = valueMap.getOrDefault(label, BigDecimal.ZERO);
            valueMap.put(label, current.add(row.getConsultationMedicine().getLineTotal() == null ? BigDecimal.ZERO : row.getConsultationMedicine().getLineTotal()));
        }
        if (valueMap.isEmpty()) {
            return "[]";
        }
        return valueMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + entry.getValue().setScale(2, RoundingMode.HALF_UP).toPlainString() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getTopPatientsJson() {
        Map<String, Long> mix = medicineRows.stream()
                .filter(row -> row.getConsultation().getPatient() != null)
                .collect(Collectors.groupingBy(row -> safeText(row.getConsultation().getPatient().getPatientName(), "Unknown Patient"), Collectors.counting()));
        if (mix.isEmpty()) {
            return "[]";
        }
        return mix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public List<Patient> getPatientList() {
        return patientList;
    }

    public List<MedicineReportRow> getMedicineRows() {
        return medicineRows;
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

    private String resolveMedicineName(ConsultationMedicine medicine) {
        if (medicine.getMedicine() != null) {
            return safeText(medicine.getMedicine().getMedicineName(), "Unknown Medicine");
        }
        return safeText(medicine.getDescriptionText(), "Unknown Medicine");
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

    public static class MedicineReportRow implements Serializable {
        private Consultation consultation;
        private ConsultationMedicine consultationMedicine;

        public Consultation getConsultation() {
            return consultation;
        }

        public void setConsultation(Consultation consultation) {
            this.consultation = consultation;
        }

        public ConsultationMedicine getConsultationMedicine() {
            return consultationMedicine;
        }

        public void setConsultationMedicine(ConsultationMedicine consultationMedicine) {
            this.consultationMedicine = consultationMedicine;
        }
    }
}
