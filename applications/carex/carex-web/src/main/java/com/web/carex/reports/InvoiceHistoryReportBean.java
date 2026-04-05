package com.web.carex.reports;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.carex.settings.IInvoiceSettingsService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.ConsultationMedicine;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.settings.ClinicSettings;
import com.persist.carex.settings.InvoiceSettings;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.context.annotation.Scope;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Named("invoiceHistoryReportBean")
@Scope("session")
public class InvoiceHistoryReportBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IPatientService patientService;

    @Inject
    private IConsultationService consultationService;

    @Inject
    private IClinicSettingsService clinicSettingsService;

    @Inject
    private IInvoiceSettingsService invoiceSettingsService;

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
    private InvoiceSettings invoiceSettings = new InvoiceSettings();
    private Organizations selectedOrganization;
    private Integer selectedInvoiceConsultationId;
    private Consultation selectedInvoiceConsultation;
    private StreamedContent consultationInvoicePdf;

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
        selectedOrganization = selectedOrganizationId == null ? null : organizationService.getOrganizationById(selectedOrganizationId);
        clinicSettings = selectedOrganizationId == null
                ? new ClinicSettings()
                : fallbackClinicSettings(clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId));
        invoiceSettings = selectedOrganizationId == null
                ? createDefaultInvoiceSettings()
                : fallbackInvoiceSettings(invoiceSettingsService.getInvoiceSettingsByOrganizationId(selectedOrganizationId));
        if (!"SPECIFIC".equals(patientScope)) {
            selectedPatientId = null;
        }
        reportConsultations = new ArrayList<>();
        selectedInvoiceConsultation = null;
    }

    public void onPatientScopeChange() {
        if (!"SPECIFIC".equals(patientScope)) {
            selectedPatientId = null;
        }
    }

    public void fetchReport() {
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization to fetch invoice history.");
            reportConsultations = new ArrayList<>();
            return;
        }
        if ("SPECIFIC".equals(patientScope) && selectedPatientId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient required", "Choose a patient for specific invoice history.");
            reportConsultations = new ArrayList<>();
            return;
        }

        LocalDate referenceDate = resolveReferenceDate();
        reportConsultations = new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId)).stream()
                .filter(Consultation::isIssueInvoice)
                .filter(consultation -> consultation.getConsultationDate() != null)
                .filter(this::matchesPatientScope)
                .filter(consultation -> matchesPeriodScope(consultation, referenceDate))
                .sorted(Comparator.comparing(Consultation::getConsultationDate).reversed())
                .collect(Collectors.toList());

        if (reportConsultations.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_INFO, "No data", "No invoices matched the selected criteria.");
        }
    }

    public void openInvoicePreview() {
        if (selectedInvoiceConsultationId == null) {
            return;
        }
        selectedInvoiceConsultation = consultationService.getConsultationById(selectedInvoiceConsultationId);
        PrimeFaces.current().executeScript("PF('invoiceHistoryPreviewDialog').show();");
    }

    public void prepareInvoiceDownload() {
        if (selectedInvoiceConsultation == null) {
            consultationInvoicePdf = null;
            addMessage(FacesMessage.SEVERITY_WARN, "Invoice required", "Select an invoice before downloading.");
            return;
        }
        try {
            String fileName = safeFileName(selectedInvoiceConsultation.getConsultationNumber(), "invoice") + ".pdf";
            byte[] pdfBytes = buildBrowserRenderedPdf("Invoice", buildInvoiceBodyHtml(selectedInvoiceConsultation), buildInvoiceFooterHtml(), "invoice-preview");
            consultationInvoicePdf = DefaultStreamedContent.builder()
                    .name(fileName)
                    .contentType("application/pdf")
                    .stream(() -> new ByteArrayInputStream(pdfBytes))
                    .build();
        } catch (Exception exception) {
            consultationInvoicePdf = null;
            addMessage(FacesMessage.SEVERITY_ERROR, "Download failed", safeText(exception.getMessage(), "Unable to generate invoice PDF."));
        }
    }

    public int getInvoiceCount() {
        return reportConsultations.size();
    }

    public int getUniquePatientCount() {
        return (int) reportConsultations.stream()
                .map(consultation -> consultation.getPatient() == null ? null : consultation.getPatient().getId())
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    public BigDecimal getTotalInvoiceValue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Consultation consultation : reportConsultations) {
            total = total.add(zeroIfNull(consultation.getInvoiceTotal()));
        }
        return scaleAmount(total);
    }

    public BigDecimal getAverageInvoiceValue() {
        if (reportConsultations.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scaleAmount(getTotalInvoiceValue().divide(BigDecimal.valueOf(reportConsultations.size()), 2, RoundingMode.HALF_UP));
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

    public String getInvoiceTrendJson() {
        LinkedHashMap<String, BigDecimal> trendMap = new LinkedHashMap<>();
        List<Consultation> ascending = reportConsultations.stream()
                .sorted(Comparator.comparing(Consultation::getConsultationDate))
                .collect(Collectors.toList());
        for (Consultation consultation : ascending) {
            String label = consultation.getConsultationDate().toLocalDateTime().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
            trendMap.put(label, trendMap.getOrDefault(label, BigDecimal.ZERO).add(zeroIfNull(consultation.getInvoiceTotal())));
        }
        if (trendMap.isEmpty()) {
            return "[]";
        }
        return trendMap.entrySet().stream()
                .map(entry -> "['" + entry.getKey() + "', " + scaleAmount(entry.getValue()).toPlainString() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getPaymentModeJson() {
        Map<String, Long> mix = reportConsultations.stream()
                .collect(Collectors.groupingBy(consultation -> safeText(consultation.getInvoicePaidBy(), "Unspecified"), Collectors.counting()));
        if (mix.isEmpty()) {
            return "[]";
        }
        return mix.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getTopPatientsInvoiceJson() {
        Map<String, BigDecimal> patientMap = new LinkedHashMap<>();
        for (Consultation consultation : reportConsultations) {
            String patientName = consultation.getPatient() == null
                    ? "Unknown Patient"
                    : safeText(consultation.getPatient().getPatientName(), "Unknown Patient");
            patientMap.put(patientName, patientMap.getOrDefault(patientName, BigDecimal.ZERO).add(zeroIfNull(consultation.getInvoiceTotal())));
        }
        if (patientMap.isEmpty()) {
            return "[]";
        }
        return patientMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(8)
                .map(entry -> "['" + escapeJs(entry.getKey()) + "', " + scaleAmount(entry.getValue()).toPlainString() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getInvoiceStatusJson() {
        int issued = reportConsultations.size();
        int overdue = (int) reportConsultations.stream()
                .filter(consultation -> consultation.getInvoiceDueDate() != null)
                .filter(consultation -> consultation.getInvoiceDueDate().toLocalDateTime().toLocalDate().isBefore(LocalDate.now()))
                .count();
        int dueToday = (int) reportConsultations.stream()
                .filter(consultation -> consultation.getInvoiceDueDate() != null)
                .filter(consultation -> consultation.getInvoiceDueDate().toLocalDateTime().toLocalDate().equals(LocalDate.now()))
                .count();
        return "[['Issued', " + issued + "], ['Due Today', " + dueToday + "], ['Overdue', " + overdue + "]]";
    }

    public String formatAmount(BigDecimal amount) {
        BigDecimal scaled = scaleAmount(amount);
        return (safeText(clinicSettings.getBaseCurrencySymbol(), "").isEmpty() ? "" : safeText(clinicSettings.getBaseCurrencySymbol(), "") + " ") + scaled.toPlainString();
    }

    public String getPreviewHtml() {
        if (selectedInvoiceConsultation == null) {
            return "<div style=\"padding:1rem;color:#64748b;\">No invoice selected.</div>";
        }
        return buildInvoiceBodyHtml(selectedInvoiceConsultation);
    }

    public StreamedContent getConsultationInvoicePdf() {
        return consultationInvoicePdf;
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

    public Integer getSelectedInvoiceConsultationId() {
        return selectedInvoiceConsultationId;
    }

    public void setSelectedInvoiceConsultationId(Integer selectedInvoiceConsultationId) {
        this.selectedInvoiceConsultationId = selectedInvoiceConsultationId;
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

    private String buildInvoiceBodyHtml(Consultation consultation) {
        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"invoice-preview\" style=\"position:relative;background:#fff;border:1px solid #cbd5e1;border-radius:12px;padding:24px;color:")
                .append(escapeHtml(getPreviewTextColor()))
                .append(";font-family:")
                .append(safeCssFontFamily())
                .append(";font-size:")
                .append(invoiceSettings.getFontSize() == null ? 12 : invoiceSettings.getFontSize())
                .append("px;\">");

        if (invoiceSettings.isShowWatermark() && !isBlank(invoiceSettings.getWatermarkText())) {
            builder.append("<div style=\"position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:64px;font-weight:700;color:rgba(148,163,184,.15);transform:rotate(-24deg);pointer-events:none;\">")
                    .append(escapeHtml(invoiceSettings.getWatermarkText()))
                    .append("</div>");
        }

        builder.append("<div style=\"display:flex;justify-content:space-between;gap:24px;position:relative;z-index:1;\">")
                .append("<div style=\"display:flex;gap:16px;\">");

        if (isPreviewOrganizationLogoAvailable() && invoiceSettings.isShowClinicLogo()) {
            builder.append("<img src=\"")
                    .append(getPreviewOrganizationLogoDataUri())
                    .append("\" style=\"width:")
                    .append(getPreviewLogoSizePx())
                    .append("px;height:auto;border-radius:12px;object-fit:cover;\"/>");
        }

        builder.append("<div>")
                .append("<div style=\"font-size:1.35rem;font-weight:700;color:")
                .append(escapeHtml(getPreviewThemeColor()))
                .append(";\">")
                .append(escapeHtml(safeText(clinicSettings.getClinicName(), "Clinic")))
                .append("</div>")
                .append("<div style=\"margin-top:6px;color:#475569;\">")
                .append(escapeHtml(safeText(clinicSettings.getClinicTagline(), "")))
                .append("</div>")
                .append("<div style=\"margin-top:8px;color:#475569;line-height:1.5;\">")
                .append(escapeHtml(safeText(clinicSettings.getClinicAddress(), "")))
                .append("<br/>")
                .append(escapeHtml(safeText(clinicSettings.getClinicEmail(), "")))
                .append("</div>")
                .append("</div></div>");

        builder.append("<div style=\"text-align:right;min-width:240px;\">")
                .append("<div style=\"font-size:1.5rem;font-weight:700;color:")
                .append(escapeHtml(getPreviewThemeColor()))
                .append(";text-transform:uppercase;\">")
                .append(escapeHtml(safeText(invoiceSettings.getInvoiceTitle(), "Tax Invoice")))
                .append("</div>")
                .append("<div style=\"margin-top:8px;\"><strong>")
                .append(escapeHtml(safeText(invoiceSettings.getInvoiceNumberLabel(), "Invoice No")))
                .append(":</strong> ")
                .append(escapeHtml(safeText(consultation.getConsultationNumber(), "-")))
                .append("</div>")
                .append("<div style=\"margin-top:4px;\"><strong>")
                .append(escapeHtml(safeText(invoiceSettings.getIssueDateLabel(), "Issue Date")))
                .append(":</strong> ")
                .append(escapeHtml(formatDisplayDate(consultation.getInvoiceIssueDate() != null ? consultation.getInvoiceIssueDate().toLocalDateTime().toLocalDate() : consultation.getConsultationDate().toLocalDateTime().toLocalDate())))
                .append("</div>")
                .append("<div style=\"margin-top:4px;\"><strong>")
                .append(escapeHtml(safeText(invoiceSettings.getDueDateLabel(), "Due Date")))
                .append(":</strong> ")
                .append(escapeHtml(formatDisplayDate(consultation.getInvoiceDueDate() != null ? consultation.getInvoiceDueDate().toLocalDateTime().toLocalDate() : consultation.getConsultationDate().toLocalDateTime().toLocalDate())))
                .append("</div>")
                .append("<div style=\"margin-top:4px;\"><strong>")
                .append(escapeHtml(safeText(invoiceSettings.getPaidByLabel(), "Paid By")))
                .append(":</strong> ")
                .append(escapeHtml(safeText(consultation.getInvoicePaidBy(), "Unspecified")))
                .append("</div>")
                .append("</div></div>");

        builder.append("<div style=\"display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;margin-top:24px;position:relative;z-index:1;\">")
                .append("<div style=\"padding:16px;border-radius:14px;background:#f8fafc;border:1px solid #e2e8f0;\">")
                .append("<div style=\"font-size:.82rem;font-weight:700;color:")
                .append(escapeHtml(getPreviewThemeColor()))
                .append(";text-transform:uppercase;\">")
                .append(escapeHtml(safeText(invoiceSettings.getBillFromLabel(), "Bill From")))
                .append("</div>")
                .append("<div style=\"margin-top:10px;font-weight:700;\">")
                .append(escapeHtml(safeText(clinicSettings.getClinicName(), "Clinic")))
                .append("</div>")
                .append("<div style=\"margin-top:6px;color:#475569;line-height:1.6;\">")
                .append(escapeHtml(safeText(clinicSettings.getClinicAddress(), "")))
                .append("</div>")
                .append("</div>")
                .append("<div style=\"padding:16px;border-radius:14px;background:#f8fafc;border:1px solid #e2e8f0;\">")
                .append("<div style=\"font-size:.82rem;font-weight:700;color:")
                .append(escapeHtml(getPreviewThemeColor()))
                .append(";text-transform:uppercase;\">")
                .append(escapeHtml(safeText(invoiceSettings.getBillToLabel(), "Bill To")))
                .append("</div>")
                .append("<div style=\"margin-top:10px;font-weight:700;\">")
                .append(escapeHtml(consultation.getPatient() == null ? "Patient" : safeText(consultation.getPatient().getPatientName(), "Patient")))
                .append("</div>")
                .append("<div style=\"margin-top:6px;color:#475569;line-height:1.6;\">")
                .append(escapeHtml(consultation.getPatient() == null ? "" : safeText(consultation.getPatient().getPatientCode(), "")))
                .append("<br/>")
                .append(escapeHtml(consultation.getPatient() == null ? "" : safeText(consultation.getPatient().getPhoneNumber(), "")))
                .append("</div>")
                .append("</div></div>");

        builder.append("<div style=\"margin-top:24px;border:1px solid #e2e8f0;border-radius:16px;overflow:hidden;position:relative;z-index:1;\">")
                .append("<div style=\"display:grid;grid-template-columns:2.2fr .6fr .8fr .8fr;background:")
                .append(escapeHtml(getPreviewThemeColor()))
                .append(";color:#fff;font-weight:700;\">")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(safeText(invoiceSettings.getDescriptionColumnLabel(), "Description"))).append("</div>")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(safeText(invoiceSettings.getQuantityColumnLabel(), "Qty"))).append("</div>")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(safeText(invoiceSettings.getPriceColumnLabel(), "Price"))).append("</div>")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(safeText(invoiceSettings.getTotalColumnLabel(), "Total"))).append("</div>")
                .append("</div>")
                .append(buildInvoiceLineRows(consultation))
                .append("</div>");

        builder.append("<div style=\"display:flex;justify-content:flex-end;margin-top:18px;position:relative;z-index:1;\">")
                .append("<div style=\"min-width:320px;border:1px solid #e2e8f0;border-radius:16px;overflow:hidden;\">")
                .append("<div style=\"display:grid;grid-template-columns:1fr auto;background:#f8fafc;\">")
                .append("<div style=\"padding:12px;text-align:right;font-weight:700;\">")
                .append(escapeHtml(safeText(invoiceSettings.getGrandTotalLabel(), "Grand Total")))
                .append("</div>")
                .append("<div style=\"padding:12px;font-weight:700;\">")
                .append(escapeHtml(formatAmount(consultation.getInvoiceTotal())))
                .append("</div></div></div></div>");

        if (!isBlank(invoiceSettings.getFooterNote())) {
            builder.append("<div style=\"margin-top:18px;font-size:.88rem;color:#475569;line-height:1.7;position:relative;z-index:1;\">")
                    .append(escapeHtml(invoiceSettings.getFooterNote()))
                    .append("</div>");
        }

        builder.append("</div>");
        return builder.toString();
    }

    private String buildInvoiceLineRows(Consultation consultation) {
        StringBuilder rows = new StringBuilder();
        int rowIndex = 0;
        rowIndex = appendInvoiceLine(rows, rowIndex, "Consultation Fee", BigDecimal.ONE, consultation.getConsultationFee());
        if (consultation.getConsultationMedicines() != null) {
            for (ConsultationMedicine medicine : consultation.getConsultationMedicines()) {
                String name = medicine.getMedicine() == null
                        ? safeText(medicine.getDescriptionText(), "Medicine")
                        : safeText(medicine.getMedicine().getMedicineName(), "Medicine");
                rowIndex = appendInvoiceLine(rows, rowIndex, name, medicine.getQuantity(), medicine.getLineTotal(), medicine.getUnitPrice());
            }
        } else if (zeroIfNull(consultation.getMedicineTotal()).compareTo(BigDecimal.ZERO) > 0) {
            rowIndex = appendInvoiceLine(rows, rowIndex, "Medicines", BigDecimal.ONE, consultation.getMedicineTotal());
        }
        if (zeroIfNull(consultation.getMedicalCertificateFee()).compareTo(BigDecimal.ZERO) > 0) {
            rowIndex = appendInvoiceLine(rows, rowIndex, "Medical Certificate Fee", BigDecimal.ONE, consultation.getMedicalCertificateFee());
        }
        return rows.toString();
    }

    private int appendInvoiceLine(StringBuilder rows, int rowIndex, String description, BigDecimal quantity, BigDecimal lineTotal) {
        BigDecimal safeQuantity = quantity == null ? BigDecimal.ONE : quantity;
        BigDecimal safeLineTotal = zeroIfNull(lineTotal);
        BigDecimal unitPrice = safeQuantity.compareTo(BigDecimal.ZERO) == 0
                ? safeLineTotal
                : safeLineTotal.divide(safeQuantity, 2, RoundingMode.HALF_UP);
        return appendInvoiceLine(rows, rowIndex, description, safeQuantity, safeLineTotal, unitPrice);
    }

    private int appendInvoiceLine(StringBuilder rows, int rowIndex, String description, BigDecimal quantity, BigDecimal lineTotal, BigDecimal unitPrice) {
        String background = rowIndex % 2 == 0 ? "#ffffff" : "#f8fafc";
        rows.append("<div style=\"display:grid;grid-template-columns:2.2fr .6fr .8fr .8fr;background:")
                .append(background)
                .append(";border-top:1px solid #e2e8f0;\">")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(safeText(description, "Item"))).append("</div>")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(scaleAmount(quantity).stripTrailingZeros().toPlainString())).append("</div>")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(formatAmount(unitPrice))).append("</div>")
                .append("<div style=\"padding:10px 12px;\">").append(escapeHtml(formatAmount(lineTotal))).append("</div>")
                .append("</div>");
        return rowIndex + 1;
    }

    private String buildInvoiceFooterHtml() {
        return "<div class=\"invoice-preview-footer\" style=\"font-size:.82rem;color:#64748b;text-align:center;\">"
                + escapeHtml(safeText(invoiceSettings.getNotes(), "Generated from invoice history report."))
                + "</div>";
    }

    private byte[] buildBrowserRenderedPdf(String title, String bodyHtml, String footerHtml, String previewClass) throws Exception {
        Path tempDirectory = Files.createTempDirectory("carex-invoice-history-preview-");
        try {
            Path htmlPath = tempDirectory.resolve("preview.html");
            Path pdfPath = tempDirectory.resolve("preview.pdf");
            Files.writeString(htmlPath, buildBrowserRenderHtml(title, bodyHtml, footerHtml, previewClass), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    resolveBrowserExecutable(),
                    "--headless=new",
                    "--disable-gpu",
                    "--run-all-compositor-stages-before-draw",
                    "--print-to-pdf-no-header",
                    "--no-pdf-header-footer",
                    "--print-to-pdf=" + pdfPath.toAbsolutePath(),
                    htmlPath.toUri().toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Browser PDF generation timed out.");
            }
            if (process.exitValue() != 0 || !Files.exists(pdfPath)) {
                throw new IllegalStateException("Browser PDF generation failed. " + output);
            }
            return Files.readAllBytes(pdfPath);
        } finally {
            deleteRecursively(tempDirectory);
        }
    }

    private String buildBrowserRenderHtml(String title, String bodyHtml, String footerHtml, String previewClass) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><title>" + escapeHtml(title) + "</title><style>"
                + "html,body{font-family:" + safeCssFontFamily() + ";margin:0;padding:0;background:#fff;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;overflow:hidden !important;}"
                + "body{background:#fff;overflow:hidden !important;}.preview-shell{position:relative;box-sizing:border-box;width:186mm;height:272mm;max-height:272mm;margin:0 auto;padding:0;overflow:hidden !important;border:none !important;outline:none !important;box-shadow:none !important;}"
                + ".preview-shell *{-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;box-sizing:border-box;}"
                + ".print-body{padding-bottom:36mm;overflow:hidden !important;max-height:236mm;}"
                + ".print-body ." + previewClass + ",.print-body .card{border:none !important;outline:none !important;box-shadow:none !important;background:#fff !important;}"
                + ".print-footer{position:absolute;left:24px;right:24px;bottom:18px;}@page{size:A4 portrait;margin:12mm;}"
                + "</style></head><body><div class=\"preview-shell\"><div class=\"print-body\">"
                + bodyHtml
                + "</div>" + (isBlank(footerHtml) ? "" : "<div class=\"print-footer\">" + footerHtml + "</div>")
                + "</div></body></html>";
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

    private String getPreviewThemeColor() {
        return normalizeHexColor(invoiceSettings.getThemeColor(), "#0F766E");
    }

    private String getPreviewTextColor() {
        return normalizeHexColor(invoiceSettings.getTextColor(), "#111827");
    }

    private boolean isPreviewOrganizationLogoAvailable() {
        return selectedOrganization != null && selectedOrganization.getImage() != null && selectedOrganization.getImage().length > 0;
    }

    private String getPreviewOrganizationLogoDataUri() {
        if (!isPreviewOrganizationLogoAvailable()) {
            return "";
        }
        return "data:" + resolveImageMimeType(selectedOrganization.getImage()) + ";base64," + Base64.getEncoder().encodeToString(selectedOrganization.getImage());
    }

    private int getPreviewLogoSizePx() {
        String logoSize = safeText(invoiceSettings.getLogoSize(), "Medium");
        if ("Small".equalsIgnoreCase(logoSize)) {
            return 64;
        }
        if ("Large".equalsIgnoreCase(logoSize)) {
            return 116;
        }
        return 90;
    }

    private String safeCssFontFamily() {
        String fontFamily = safeText(invoiceSettings.getFontFamily(), "Helvetica");
        if ("Times New Roman".equalsIgnoreCase(fontFamily)) {
            return "'Times New Roman', Times, serif";
        }
        if ("Courier".equalsIgnoreCase(fontFamily)) {
            return "'Courier New', Courier, monospace";
        }
        if ("Verdana".equalsIgnoreCase(fontFamily)) {
            return "Verdana, Arial, sans-serif";
        }
        if ("Georgia".equalsIgnoreCase(fontFamily)) {
            return "Georgia, 'Times New Roman', serif";
        }
        return "Helvetica, Arial, sans-serif";
    }

    private String resolveBrowserExecutable() {
        String[] candidates = {
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        };
        for (String candidate : candidates) {
            if (Files.exists(Path.of(candidate))) {
                return candidate;
            }
        }
        throw new IllegalStateException("No supported browser executable found for PDF generation.");
    }

    private String safeFileName(String source, String fallback) {
        String value = safeText(source, fallback).replaceAll("[^A-Za-z0-9._-]", "-");
        return value.isEmpty() ? fallback : value;
    }

    private String formatDisplayDate(LocalDate date) {
        return date == null ? "-" : DISPLAY_DATE_FORMATTER.format(date);
    }

    private InvoiceSettings fallbackInvoiceSettings(InvoiceSettings settings) {
        return settings == null ? createDefaultInvoiceSettings() : settings;
    }

    private ClinicSettings fallbackClinicSettings(ClinicSettings settings) {
        return settings == null ? new ClinicSettings() : settings;
    }

    private InvoiceSettings createDefaultInvoiceSettings() {
        InvoiceSettings settings = new InvoiceSettings();
        settings.setThemeColor("#0F766E");
        settings.setTextColor("#111827");
        settings.setInvoiceTitle("Tax Invoice");
        settings.setInvoiceNumberLabel("Invoice No");
        settings.setBillFromLabel("Bill From");
        settings.setBillToLabel("Bill To");
        settings.setIssueDateLabel("Issue Date");
        settings.setDueDateLabel("Due Date");
        settings.setPaidByLabel("Paid By");
        settings.setDescriptionColumnLabel("Description");
        settings.setQuantityColumnLabel("Qty");
        settings.setPriceColumnLabel("Price");
        settings.setTotalColumnLabel("Total");
        settings.setGrandTotalLabel("Grand Total");
        settings.setFooterNote("Thank you for visiting the clinic.");
        settings.setFontFamily("Helvetica");
        settings.setFontSize(12);
        settings.setLogoSize("Medium");
        settings.setShowClinicLogo(true);
        return settings;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scaleAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeHexColor(String value, String fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        String normalized = value.trim();
        if (!normalized.startsWith("#")) {
            normalized = "#" + normalized;
        }
        return normalized.matches("^#[0-9A-Fa-f]{6}$") ? normalized.toUpperCase() : fallback;
    }

    private String resolveImageMimeType(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return "image/png";
        }
        if ((imageBytes[0] & 0xFF) == 0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
            return "image/png";
        }
        if ((imageBytes[0] & 0xFF) == 0xFF && (imageBytes[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        return "image/png";
    }

    private void deleteRecursively(Path path) {
        try {
            if (Files.notExists(path)) {
                return;
            }
            Files.walk(path).sorted((left, right) -> right.compareTo(left)).forEach(currentPath -> {
                try {
                    Files.deleteIfExists(currentPath);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String escapeHtml(String value) {
        return safeText(value, "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String escapeJs(String value) {
        return safeText(value, "").replace("\\", "\\\\").replace("'", "\\'");
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
