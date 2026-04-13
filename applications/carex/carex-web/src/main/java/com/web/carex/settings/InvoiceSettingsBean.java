package com.web.carex.settings;

import com.module.carex.settings.IClinicSettingsService;
import com.module.carex.settings.IInvoiceSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.settings.ClinicSettings;
import com.persist.carex.settings.InvoiceSettings;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.carex.appgeneral.CarexManagedBean;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("invoiceSettingsBean")
@Scope("session")
public class InvoiceSettingsBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(InvoiceSettingsBean.class.getName());

    @Inject
    private transient IInvoiceSettingsService invoiceSettingsService;

    @Inject
    private transient IOrganizationService organizationService;

    @Inject
    private transient IClinicSettingsService clinicSettingsService;

    private InvoiceSettings invoiceSettings = new InvoiceSettings();
    private List<Organizations> organizationList = new ArrayList<>();
    private Integer selectedOrganizationId;
    private String downloadPreviewBodyHtml;
    private String downloadPreviewFooterHtml;
    private StreamedContent sampleInvoicePdf;
    private boolean initialized;
    private boolean renderDataWarningShown;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        try {
            loadOrganizations();
            selectedOrganizationId = resolveDefaultOrganizationId(organizationList, selectedOrganizationId);
            loadInvoiceSettings();
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to initialize invoice settings page", exception);
            organizationList = new ArrayList<>();
            selectedOrganizationId = null;
            invoiceSettings = createDefaultInvoiceSettings();
            addMessage(FacesMessage.SEVERITY_ERROR, "Invoice settings unavailable",
                    "Unable to load invoice settings data. Default values are shown.");
        }
        initialized = true;
    }

    public void onOrganizationChange() {
        loadInvoiceSettings();
        resetPreparedDownload();
        PrimeFaces.current().ajax().update("form:invoiceSettingsPanel", "form:messages");
    }

    public void saveInvoiceSettings() {
        if (!validateForm()) {
            PrimeFaces.current().ajax().update("form:messages", "form:invoiceSettingsPanel");
            return;
        }

        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return;
        }

        Organizations organization = organizationService.getOrganizationById(selectedOrganizationId);
        if (organization == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization missing", "The selected organization could not be found.");
            return;
        }

        invoiceSettings.setOrganization(organization);
        invoiceSettings.setThemeColor(normalizeHexColor(invoiceSettings.getThemeColor(), "#0F766E"));
        invoiceSettings.setTextColor(normalizeHexColor(invoiceSettings.getTextColor(), "#111827"));
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Save");

        GeneralConstants result = invoiceSettingsService.saveInvoiceSettings(userActivityTO, invoiceSettings);
        if (result == GeneralConstants.SUCCESSFUL) {
            loadInvoiceSettings();
            resetPreparedDownload();
            addMessage(FacesMessage.SEVERITY_INFO, "Saved", "Invoice settings saved successfully.");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Save failed", "Unable to save invoice settings.");
        }

        PrimeFaces.current().ajax().update("form:messages", "form:invoiceSettingsPanel");
    }

    public void prepareSampleInvoiceDownload() {
        try {
            if (isBlank(downloadPreviewBodyHtml)) {
                throw new IllegalStateException("Preview content was not captured for download.");
            }
            byte[] pdfBytes = buildBrowserRenderedInvoicePdf();
            sampleInvoicePdf = DefaultStreamedContent.builder()
                    .name("invoice-sample.pdf")
                    .contentType("application/pdf")
                    .stream(() -> new ByteArrayInputStream(pdfBytes))
                    .build();
        } catch (Exception exception) {
            sampleInvoicePdf = null;
            addMessage(FacesMessage.SEVERITY_ERROR, "Download failed", safeText(exception.getMessage(), "Unable to generate invoice PDF."));
        }
    }

    public StreamedContent getSampleInvoicePdf() {
        return sampleInvoicePdf;
    }

    public InvoiceSettings getInvoiceSettings() {
        return invoiceSettings;
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = resolveAccessibleOrganizationId(selectedOrganizationId);
    }

    public String getDownloadPreviewBodyHtml() {
        return downloadPreviewBodyHtml;
    }

    public void setDownloadPreviewBodyHtml(String downloadPreviewBodyHtml) {
        this.downloadPreviewBodyHtml = downloadPreviewBodyHtml;
    }

    public String getDownloadPreviewFooterHtml() {
        return downloadPreviewFooterHtml;
    }

    public void setDownloadPreviewFooterHtml(String downloadPreviewFooterHtml) {
        this.downloadPreviewFooterHtml = downloadPreviewFooterHtml;
    }

    public List<String> getAvailablePageSizes() {
        return Arrays.asList("A4", "A5", "Letter");
    }

    public List<String> getAvailableTemplates() {
        return Arrays.asList("Classic", "Minimal", "Professional");
    }

    public List<String> getAvailableFontFamilies() {
        return Arrays.asList("Helvetica", "Times New Roman", "Courier", "Verdana", "Georgia");
    }

    public List<String> getAvailableLogoSizes() {
        return Arrays.asList("Small", "Medium", "Large");
    }

    public String getPreviewFontFamily() {
        return isBlank(invoiceSettings.getFontFamily()) ? "Helvetica" : invoiceSettings.getFontFamily();
    }

    public String getPreviewThemeColor() {
        return normalizeHexColor(invoiceSettings.getThemeColor(), "#0F766E");
    }

    public String getPreviewTextColor() {
        return normalizeHexColor(invoiceSettings.getTextColor(), "#111827");
    }

    public String getPreviewWatermarkText() {
        return isBlank(invoiceSettings.getWatermarkText()) ? "INVOICE" : invoiceSettings.getWatermarkText().trim();
    }

    public int getPreviewLogoSizePx() {
        String logoSize = safeText(invoiceSettings.getLogoSize(), "Medium");
        if ("Small".equalsIgnoreCase(logoSize)) {
            return 64;
        }
        if ("Large".equalsIgnoreCase(logoSize)) {
            return 116;
        }
        return 90;
    }

    public boolean isPreviewOrganizationLogoAvailable() {
        Organizations organization = getSelectedOrganizationEntity();
        return organization != null && organization.getImage() != null && organization.getImage().length > 0;
    }

    public String getPreviewOrganizationLogoDataUri() {
        Organizations organization = getSelectedOrganizationEntity();
        if (organization == null || organization.getImage() == null || organization.getImage().length == 0) {
            return "";
        }
        return "data:" + resolveImageMimeType(organization.getImage()) + ";base64,"
                + Base64.getEncoder().encodeToString(organization.getImage());
    }

    public String getPreviewClinicName() { return safeText(getActiveClinicSettings().getClinicName(), "XXX XXXX XXX"); }
    public String getPreviewClinicTagline() { return safeText(getActiveClinicSettings().getClinicTagline(), "XXX XXXX XXX XXX"); }
    public String getPreviewDoctorName() { return safeText(getActiveClinicSettings().getDoctorName(), "Dr. XXX XXXX XXX"); }
    public String getPreviewDoctorQualification() { return safeText(getActiveClinicSettings().getDoctorQualification(), "MD Pediatrics"); }
    public String getPreviewDoctorSpecialization() { return safeText(getActiveClinicSettings().getDoctorSpecialization(), "Senior Consultant Pediatrician"); }
    public String getPreviewClinicEmail() { return safeText(getActiveClinicSettings().getClinicEmail(), "xxx@xxxx.com"); }
    public String getPreviewRegistrationNumber() { return safeText(getActiveClinicSettings().getRegistrationNumber(), "XXX88"); }
    public String getPreviewClinicAddress() { return safeText(getActiveClinicSettings().getClinicAddress(), "Clinic address"); }
    public String getPreviewAppointmentContact() { return safeText(getActiveClinicSettings().getAppointmentContact(), "XXXX1 3XX38"); }
    public String getPreviewScheduleLineOne() { return safeText(getActiveClinicSettings().getScheduleLineOne(), "Days : Mon - Sat (Except Tuesday) | Timings : 9.00 AM - 12.00 PM & 6.00 PM - 9.00 PM"); }
    public String getPreviewScheduleLineTwo() { return safeText(getActiveClinicSettings().getScheduleLineTwo(), "Sunday : 10.00 AM - 1.00 PM | Tuesday : 6.00 PM - 9.00 PM"); }
    public String getPreviewCurrencySymbol() { return safeText(getActiveClinicSettings().getBaseCurrencySymbol(), ""); }

    private void loadInvoiceSettings() {
        try {
            InvoiceSettings existing = selectedOrganizationId == null
                    ? null
                    : invoiceSettingsService.getInvoiceSettingsByOrganizationId(selectedOrganizationId);
            invoiceSettings = existing == null ? createDefaultInvoiceSettings() : existing;
        } catch (Exception exception) {
            invoiceSettings = createDefaultInvoiceSettings();
            addMessage(FacesMessage.SEVERITY_ERROR, "Invoice settings unavailable",
                    "Unable to load saved invoice settings. Default values are shown.");
        }
    }

    private void loadOrganizations() {
        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
    }

    private Organizations getSelectedOrganizationEntity() {
        if (selectedOrganizationId == null) {
            return null;
        }
        try {
            return organizationService.getOrganizationById(selectedOrganizationId);
        } catch (Exception exception) {
            logRenderDataIssue("Failed to load organization details for invoice preview", exception);
            return null;
        }
    }

    private ClinicSettings getActiveClinicSettings() {
        if (selectedOrganizationId == null) {
            return new ClinicSettings();
        }
        try {
            ClinicSettings settings = clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId);
            return settings == null ? new ClinicSettings() : settings;
        } catch (Exception exception) {
            logRenderDataIssue("Failed to load clinic settings for invoice preview", exception);
            return new ClinicSettings();
        }
    }

    private InvoiceSettings createDefaultInvoiceSettings() {
        InvoiceSettings settings = new InvoiceSettings();
        settings.setPageSize("A4");
        settings.setTemplateName("Classic");
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
        settings.setFontFamily("Helvetica");
        settings.setFontSize(12);
        settings.setLogoSize("Medium");
        settings.setShowClinicLogo(true);
        settings.setShowWatermark(false);
        settings.setFooterNote("XXX XXXX XXXX XXXX XXXX.");
        return settings;
    }

    private boolean validateForm() {
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return false;
        }
        if (isBlank(invoiceSettings.getPageSize()) || !getAvailablePageSizes().contains(invoiceSettings.getPageSize())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid page size", "Select a valid page size.");
            return false;
        }
        if (isBlank(invoiceSettings.getTemplateName()) || !getAvailableTemplates().contains(invoiceSettings.getTemplateName())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid template", "Select a valid template.");
            return false;
        }
        if (isBlank(invoiceSettings.getInvoiceTitle())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invoice title required", "Enter the invoice title.");
            return false;
        }
        if (isBlank(invoiceSettings.getBillFromLabel()) || isBlank(invoiceSettings.getBillToLabel())
                || isBlank(invoiceSettings.getIssueDateLabel()) || isBlank(invoiceSettings.getDueDateLabel())
                || isBlank(invoiceSettings.getPaidByLabel())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invoice labels required", "Enter all invoice section labels.");
            return false;
        }
        if (isBlank(invoiceSettings.getDescriptionColumnLabel()) || isBlank(invoiceSettings.getQuantityColumnLabel())
                || isBlank(invoiceSettings.getPriceColumnLabel()) || isBlank(invoiceSettings.getTotalColumnLabel())
                || isBlank(invoiceSettings.getGrandTotalLabel())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Table labels required", "Enter all invoice table labels.");
            return false;
        }
        if (isBlank(invoiceSettings.getFontFamily()) || !getAvailableFontFamilies().contains(invoiceSettings.getFontFamily())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid font family", "Select a valid font family.");
            return false;
        }
        if (isBlank(invoiceSettings.getLogoSize()) || !getAvailableLogoSizes().contains(invoiceSettings.getLogoSize())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid logo size", "Select a valid logo size.");
            return false;
        }
        if (invoiceSettings.getFontSize() == null || invoiceSettings.getFontSize() < 8 || invoiceSettings.getFontSize() > 24) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid font size", "Font size must be between 8 and 24.");
            return false;
        }
        if (!isValidColor(invoiceSettings.getThemeColor()) || !isValidColor(invoiceSettings.getTextColor())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid colors", "Colors must be valid hex codes such as #0F766E.");
            return false;
        }
        if (invoiceSettings.isShowWatermark() && isBlank(invoiceSettings.getWatermarkText())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Watermark text required", "Enter watermark text when watermark is enabled.");
            return false;
        }
        return true;
    }

    private byte[] buildBrowserRenderedInvoicePdf() throws Exception {
        Path tempDirectory = Files.createTempDirectory("carex-invoice-preview-");
        try {
            Path htmlPath = tempDirectory.resolve("invoice-preview.html");
            Path pdfPath = tempDirectory.resolve("invoice-preview.pdf");
            Files.writeString(htmlPath, buildBrowserRenderHtml(), StandardCharsets.UTF_8,
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

            String processOutput;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
                processOutput = builder.toString();
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Browser PDF generation timed out.");
            }
            if (process.exitValue() != 0 || !Files.exists(pdfPath)) {
                throw new IllegalStateException("Browser PDF generation failed. " + processOutput);
            }
            return Files.readAllBytes(pdfPath);
        } finally {
            deleteRecursively(tempDirectory);
        }
    }

    private String buildBrowserRenderHtml() {
        String bodyHtml = safeHtmlFragment(downloadPreviewBodyHtml);
        String footerHtml = safeHtmlFragment(downloadPreviewFooterHtml);
        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"/>" +
                "<title>" + escapeHtml(safeText(invoiceSettings.getInvoiceTitle(), "Invoice Sample")) + "</title>" +
                "<style>" +
                "html,body{font-family:" + safeCssFontFamily() + ";margin:0;padding:0;background:#fff;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;overflow:hidden !important;}" +
                "body{background:#fff;overflow:hidden !important;}" +
                ".preview-shell{position:relative;box-sizing:border-box;width:186mm;height:272mm;max-height:272mm;margin:0 auto;padding:0;overflow:hidden !important;border:none !important;outline:none !important;box-shadow:none !important;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;}" +
                ".preview-shell *{-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;box-sizing:border-box;}" +
                ".print-body{padding-bottom:42mm;overflow:hidden !important;max-height:230mm;}" +
                ".print-body .invoice-preview,.print-body .card{border:none !important;outline:none !important;box-shadow:none !important;background:#fff !important;}" +
                ".print-footer{position:absolute;left:24px;right:24px;bottom:18px;}" +
                ".print-footer .invoice-preview-footer{border:none !important;outline:none !important;box-shadow:none !important;}" +
                "@page{size:A4 portrait;margin:12mm;}" +
                "@media print{html,body{background:#fff !important;overflow:hidden !important;}body{padding:0;margin:0;overflow:hidden !important;}}" +
                "</style></head><body><div class=\"preview-shell\"><div class=\"print-body\">" +
                bodyHtml +
                "</div>" +
                (isBlank(footerHtml) ? "" : "<div class=\"print-footer\">" + footerHtml + "</div>") +
                "</div></body></html>";
    }

    private String safeCssFontFamily() {
        String fontFamily = safeText(getPreviewFontFamily(), "Helvetica");
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
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
                "/Applications/Chromium.app/Contents/MacOS/Chromium",
                "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser",
                "/Applications/Opera.app/Contents/MacOS/Opera",
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

    private boolean isValidColor(String value) {
        return value != null && value.matches("^#?[0-9A-Fa-f]{6}$");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
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

    private UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        UserActivityTO userActivityTO = new UserActivityTO();
        userActivityTO.setUserId(sessionMap.get("userAccountId") instanceof Integer ? (Integer) sessionMap.get("userAccountId") : 0);
        userActivityTO.setUserName((String) sessionMap.get("username"));
        userActivityTO.setIpAddress((String) sessionMap.get("Machine IP"));
        userActivityTO.setDeviceInfo((String) sessionMap.get("Machine Name"));
        userActivityTO.setLocationInfo((String) sessionMap.get("browserClientInfo"));
        userActivityTO.setCreatedAt(new Date());
        return userActivityTO;
    }

    private void resetPreparedDownload() {
        sampleInvoicePdf = null;
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
        if (imageBytes[0] == 'G' && imageBytes[1] == 'I' && imageBytes[2] == 'F') {
            return "image/gif";
        }
        return "image/png";
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private void logRenderDataIssue(String message, Exception exception) {
        LOGGER.log(Level.SEVERE, message, exception);
        if (!renderDataWarningShown) {
            renderDataWarningShown = true;
            addMessage(FacesMessage.SEVERITY_WARN, "Partial data unavailable",
                    "Some clinic/organization preview data could not be loaded.");
        }
    }

    private String safeHtmlFragment(String html) {
        return html == null ? "" : html.trim();
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void deleteRecursively(Path path) {
        try {
            if (Files.notExists(path)) {
                return;
            }
            Files.walk(path)
                    .sorted((left, right) -> right.compareTo(left))
                    .forEach(currentPath -> {
                        try {
                            Files.deleteIfExists(currentPath);
                        } catch (Exception ignored) {
                            // Best-effort temp cleanup.
                        }
                    });
        } catch (Exception ignored) {
            // Best-effort temp cleanup.
        }
    }
}
