package com.web.carex.settings;

import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.ICurrencyDetailsService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("clinicSettingsBean")
@Scope("session")
public class ClinicSettingsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private IClinicSettingsService clinicSettingsService;

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private ICurrencyDetailsService currencyDetailsService;

    private ClinicSettings clinicSettings = new ClinicSettings();
    private List<Organizations> organizationList = new ArrayList<>();
    private List<CurrencyDetails> currencyDetailsList = new ArrayList<>();
    private Integer selectedOrganizationId;
    private String selectedBaseCurrencyDisplay;
    private boolean initialized;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        loadOrganizations();
        loadCurrencies();
        if (selectedOrganizationId == null) {
            selectedOrganizationId = resolveCurrentOrganizationId();
        }
        loadClinicSettings();
        initialized = true;
    }

    public void onOrganizationChange() {
        loadClinicSettings();
        PrimeFaces.current().ajax().update("form:clinicSettingsPanel", "form:messages");
    }

    public void saveClinicSettings() {
        if (!validateForm()) {
            PrimeFaces.current().ajax().update("form:messages", "form:clinicSettingsPanel");
            return;
        }

        Integer organizationId = selectedOrganizationId;
        if (organizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return;
        }

        Organizations organization = organizationService.getOrganizationById(organizationId);
        if (organization == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization missing", "The selected organization could not be found.");
            return;
        }

        applySelectedCurrency();
        clinicSettings.setOrganization(organization);
        normalizeFees();
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Save");

        GeneralConstants result = clinicSettingsService.saveClinicSettings(userActivityTO, clinicSettings);
        if (result == GeneralConstants.SUCCESSFUL) {
            loadClinicSettings();
            addMessage(FacesMessage.SEVERITY_INFO, "Saved", "Clinic settings saved successfully.");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Save failed", "Unable to save clinic settings.");
        }

        PrimeFaces.current().ajax().update("form:messages", "form:clinicSettingsPanel");
    }

    public ClinicSettings getClinicSettings() {
        return clinicSettings;
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

    public String getSelectedBaseCurrencyDisplay() {
        return selectedBaseCurrencyDisplay;
    }

    public void setSelectedBaseCurrencyDisplay(String selectedBaseCurrencyDisplay) {
        this.selectedBaseCurrencyDisplay = selectedBaseCurrencyDisplay;
    }

    public List<String> completeCurrency(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        return currencyDetailsList.stream()
                .filter(currency -> normalizedQuery.isEmpty()
                        || safeText(currency.getCurrencyCode(), "").toLowerCase().contains(normalizedQuery)
                        || safeText(currency.getCurrencyName(), "").toLowerCase().contains(normalizedQuery)
                        || safeText(currency.getSymbol(), "").toLowerCase().contains(normalizedQuery))
                .sorted(Comparator.comparing(CurrencyDetails::getCurrencyCode, String.CASE_INSENSITIVE_ORDER))
                .map(this::formatCurrencyDisplay)
                .collect(Collectors.toList());
    }

    public String getBaseCurrencySummary() {
        if (isBlank(clinicSettings.getBaseCurrencyCode())) {
            return "No base currency selected";
        }
        return clinicSettings.getBaseCurrencyCode() + " " + safeText(clinicSettings.getBaseCurrencySymbol(), "")
                + " - " + safeText(clinicSettings.getBaseCurrencyName(), "");
    }

    public String getCurrentOrganizationName() {
        Integer organizationId = selectedOrganizationId;
        if (organizationId == null) {
            return "Unknown Organization";
        }
        Organizations organization = organizationService.getOrganizationById(organizationId);
        return organization == null ? "Unknown Organization" : organization.getOrganizationName();
    }

    private void loadClinicSettings() {
        Integer organizationId = selectedOrganizationId;
        ClinicSettings existing = organizationId == null ? null : clinicSettingsService.getClinicSettingsByOrganizationId(organizationId);
        clinicSettings = existing == null ? createDefaultClinicSettings() : existing;
        selectedBaseCurrencyDisplay = formatCurrencyDisplay(clinicSettings.getBaseCurrencyCode(),
                clinicSettings.getBaseCurrencySymbol(),
                clinicSettings.getBaseCurrencyName());
    }

    private void loadOrganizations() {
        organizationList = new ArrayList<>(organizationService.getOrganizationsList());
    }

    private void loadCurrencies() {
        currencyDetailsList = new ArrayList<>(currencyDetailsService.getCurrencyDetailsList());
    }

    private ClinicSettings createDefaultClinicSettings() {
        ClinicSettings settings = new ClinicSettings();
        settings.setWeekdaysOpen(true);
        settings.setSaturdayOpen(true);
        settings.setSundayOpen(false);
        settings.setRequireInvoice(true);
        settings.setRequireMedicalCertificate(false);
        settings.setSlotDurationMinutes(15);
        settings.setConsultationFee(BigDecimal.ZERO);
        settings.setFollowupFee(BigDecimal.ZERO);
        settings.setRegistrationFee(BigDecimal.ZERO);
        settings.setMedicalCertificateFee(BigDecimal.ZERO);
        selectedBaseCurrencyDisplay = "";
        return settings;
    }

    private boolean validateForm() {
        if (isBlank(clinicSettings.getClinicName())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Clinic name required", "Enter the clinic name.");
            return false;
        }
        if (clinicSettings.getOpeningTime() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Opening time required", "Select the clinic opening time.");
            return false;
        }
        if (clinicSettings.getClosingTime() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Closing time required", "Select the clinic closing time.");
            return false;
        }
        if (!clinicSettings.getOpeningTime().before(clinicSettings.getClosingTime())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid timings", "Closing time must be later than opening time.");
            return false;
        }
        if (clinicSettings.getBreakStartTime() != null && clinicSettings.getBreakEndTime() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Break end required", "Select a break end time.");
            return false;
        }
        if (clinicSettings.getBreakStartTime() == null && clinicSettings.getBreakEndTime() != null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Break start required", "Select a break start time.");
            return false;
        }
        if (clinicSettings.getBreakStartTime() != null) {
            if (!clinicSettings.getBreakStartTime().before(clinicSettings.getBreakEndTime())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Invalid break timings", "Break end time must be later than break start time.");
                return false;
            }
            if (clinicSettings.getBreakStartTime().before(clinicSettings.getOpeningTime())
                    || clinicSettings.getBreakEndTime().after(clinicSettings.getClosingTime())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Break outside clinic hours", "Break timings must be within clinic opening hours.");
                return false;
            }
        }
        if (clinicSettings.getSlotDurationMinutes() == null || clinicSettings.getSlotDurationMinutes() <= 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid slot duration", "Slot duration must be greater than zero.");
            return false;
        }
        if (hasNegativeValue(clinicSettings.getConsultationFee())
                || hasNegativeValue(clinicSettings.getFollowupFee())
                || hasNegativeValue(clinicSettings.getRegistrationFee())
                || hasNegativeValue(clinicSettings.getMedicalCertificateFee())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid fee structure", "Fee values cannot be negative.");
            return false;
        }
        if (isBlank(selectedBaseCurrencyDisplay)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Base currency required", "Select a base currency.");
            return false;
        }
        if (resolveCurrencyByDisplay(selectedBaseCurrencyDisplay) == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid base currency", "Select a valid currency from the list.");
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasNegativeValue(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    private void normalizeFees() {
        clinicSettings.setConsultationFee(normalizeAmount(clinicSettings.getConsultationFee()));
        clinicSettings.setFollowupFee(normalizeAmount(clinicSettings.getFollowupFee()));
        clinicSettings.setRegistrationFee(normalizeAmount(clinicSettings.getRegistrationFee()));
        clinicSettings.setMedicalCertificateFee(normalizeAmount(clinicSettings.getMedicalCertificateFee()));
    }

    private void applySelectedCurrency() {
        CurrencyDetails selectedCurrency = resolveCurrencyByDisplay(selectedBaseCurrencyDisplay);
        if (selectedCurrency == null) {
            return;
        }
        clinicSettings.setBaseCurrencyCode(selectedCurrency.getCurrencyCode());
        clinicSettings.setBaseCurrencySymbol(selectedCurrency.getSymbol());
        clinicSettings.setBaseCurrencyName(selectedCurrency.getCurrencyName());
        selectedBaseCurrencyDisplay = formatCurrencyDisplay(selectedCurrency);
    }

    private CurrencyDetails resolveCurrencyByDisplay(String displayValue) {
        if (isBlank(displayValue)) {
            return null;
        }
        return currencyDetailsList.stream()
                .filter(currency -> formatCurrencyDisplay(currency).equalsIgnoreCase(displayValue.trim()))
                .findFirst()
                .orElseGet(() -> currencyDetailsList.stream()
                        .filter(currency -> safeText(currency.getCurrencyCode(), "").equalsIgnoreCase(displayValue.trim()))
                        .findFirst()
                        .orElse(null));
    }

    private String formatCurrencyDisplay(CurrencyDetails currencyDetails) {
        if (currencyDetails == null) {
            return "";
        }
        return formatCurrencyDisplay(currencyDetails.getCurrencyCode(), currencyDetails.getSymbol(), currencyDetails.getCurrencyName());
    }

    private String formatCurrencyDisplay(String code, String symbol, String name) {
        if (isBlank(code) && isBlank(name)) {
            return "";
        }
        String safeCode = safeText(code, "");
        String safeSymbol = safeText(symbol, "");
        String safeName = safeText(name, "");
        String symbolPart = isBlank(safeSymbol) ? "" : " (" + safeSymbol + ")";
        String namePart = isBlank(safeName) ? "" : " - " + safeName;
        return safeCode + symbolPart + namePart;
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        Object organizationId = sessionMap.get("organizationId");
        return organizationId instanceof Integer ? (Integer) organizationId : null;
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

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
