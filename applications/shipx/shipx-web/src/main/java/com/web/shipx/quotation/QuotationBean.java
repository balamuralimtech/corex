package com.web.shipx.quotation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.ICurrencyDetailsService;
import com.module.shipx.quotation.IQuotationService;
import com.module.shipx.quotation.model.QuotationCostLine;
import com.module.shipx.quotation.model.QuotationCostSection;
import com.module.shipx.request.ICustomerRequestService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import com.persist.shipx.quotation.Quotation;
import com.persist.shipx.request.CustomerRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("quotationBean")
@Scope("session")
public class QuotationBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();
    private static final Type SECTION_LIST_TYPE = new TypeToken<List<QuotationCostSection>>() { }.getType();
    private static final Type NOTE_LIST_TYPE = new TypeToken<List<String>>() { }.getType();

    @Inject
    private IQuotationService quotationService;

    @Inject
    private ICustomerRequestService customerRequestService;

    @Inject
    private ICurrencyDetailsService currencyDetailsService;

    private List<Quotation> quotationList = new ArrayList<>();
    private List<CustomerRequest> availableCustomerRequests = new ArrayList<>();
    private List<String> availableCurrencyCodes = new ArrayList<>();
    private Quotation selectedQuotation = new Quotation();
    private boolean addOperation = true;
    private boolean viewMode;
    private boolean datatableRendered;
    private int recordsCount;

    private Integer selectedCustomerRequestId;
    private String quotationTitle;
    private String serviceCategory = "FREIGHT_FORWARDING";
    private String customerName;
    private String contactPerson;
    private String contactNumber;
    private String recipientEmail;
    private String originLocation;
    private String destinationLocation;
    private String cargoSummary;
    private Date validUntil;
    private String emailSubject;
    private String emailBody;
    private List<QuotationCostSection> costSections = new ArrayList<>();
    private List<String> noteLines = new ArrayList<>();

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback()) {
            if (CollectionUtils.isEmpty(availableCustomerRequests)) {
                loadAvailableCustomerRequests();
            }
            if (CollectionUtils.isEmpty(availableCurrencyCodes)) {
                loadAvailableCurrencyCodes();
            }
            ensureModelsInitialized();
            return;
        }

        loadAvailableCustomerRequests();
        loadAvailableCurrencyCodes();
        quotationList.clear();
        selectedQuotation = new Quotation();
        addOperation = true;
        viewMode = false;
        datatableRendered = false;
        recordsCount = 0;
        resetForm();
    }

    public void addButtonAction() {
        addOperation = true;
        viewMode = false;
        selectedQuotation = new Quotation();
        resetForm();
    }

    public void searchButtonAction() {
        fetchQuotationList();
    }

    public void onCustomerRequestChange() {
        refreshDerivedAmounts();
        CustomerRequest customerRequest = getSelectedCustomerRequestEntity();
        if (customerRequest == null) {
            return;
        }

        customerName = customerRequest.getCustomerName();
        contactPerson = customerRequest.getContactPerson();
        contactNumber = customerRequest.getContactNumber();
        originLocation = customerRequest.getOriginCountry() == null ? null : customerRequest.getOriginCountry().getName();
        destinationLocation = customerRequest.getDestinationCountry() == null ? null : customerRequest.getDestinationCountry().getName();
        cargoSummary = customerRequest.getCapacitySummary();
        if (isBlank(quotationTitle)) {
            quotationTitle = customerRequest.getRequestReference() + " quotation";
        }
        if (isBlank(emailSubject)) {
            emailSubject = "Quotation " + buildPreviewReference() + " - " + customerRequest.getCustomerName();
        }
        if (CollectionUtils.isNotEmpty(costSections) && CollectionUtils.isEmpty(costSections.get(0).getLines())) {
            addCostLine(costSections.get(0));
        }
    }

    public void addCostSection() {
        refreshDerivedAmounts();
        QuotationCostSection section = new QuotationCostSection();
        section.setTitle("New Costing Section");
        section.getLines().add(new QuotationCostLine());
        costSections.add(section);
    }

    public void removeCostSection(QuotationCostSection section) {
        refreshDerivedAmounts();
        costSections.remove(section);
        ensureModelsInitialized();
    }

    public void addCostLine(QuotationCostSection section) {
        refreshDerivedAmounts();
        if (section == null) {
            return;
        }
        section.getLines().add(new QuotationCostLine());
    }

    public void removeCostLine(QuotationCostSection section, QuotationCostLine line) {
        refreshDerivedAmounts();
        if (section == null || line == null) {
            return;
        }
        section.getLines().remove(line);
        if (CollectionUtils.isEmpty(section.getLines())) {
            section.getLines().add(new QuotationCostLine());
        }
    }

    public void addNoteLine() {
        refreshDerivedAmounts();
        noteLines.add("");
    }

    public void removeNoteLineAt(int index) {
        refreshDerivedAmounts();
        if (index >= 0 && index < noteLines.size()) {
            noteLines.remove(index);
        }
        if (noteLines.isEmpty()) {
            noteLines.add("");
        }
    }

    public void confirmEditButtonAction() {
        if (selectedQuotation == null || selectedQuotation.getId() == null) {
            return;
        }

        addOperation = false;
        viewMode = false;
        loadQuotationIntoForm(selectedQuotation.getId());
    }

    public void viewQuotationAction() {
        if (selectedQuotation == null || selectedQuotation.getId() == null) {
            return;
        }

        addOperation = false;
        viewMode = true;
        loadQuotationIntoForm(selectedQuotation.getId());
    }

    public void newQuotationInlineAction() {
        addButtonAction();
    }

    public void saveQuotation() {
        Quotation quotation = addOperation ? new Quotation() : quotationService.getQuotationById(selectedQuotation.getId());
        if (quotation == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Quotation not found.");
            return;
        }

        if (!populateQuotation(quotation)) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;

        if (addOperation) {
            quotation.setQuotationReference(generateQuotationReference());
            quotation.setStatus("DRAFT");
            quotation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            quotation.setCreatedByUserId(userActivityTO.getUserId());
            quotation.setCreatedByUserName(userActivityTO.getUserName());
            userActivityTO.setActivityType("Add");
            result = quotationService.addQuotation(userActivityTO, quotation);
        } else {
            quotation.setCreatedAt(selectedQuotation.getCreatedAt());
            quotation.setCreatedByUserId(selectedQuotation.getCreatedByUserId());
            quotation.setCreatedByUserName(selectedQuotation.getCreatedByUserName());
            quotation.setStatus(selectedQuotation.getStatus());
            quotation.setSentAt(selectedQuotation.getSentAt());
            quotation.setSentToEmail(selectedQuotation.getSentToEmail());
            userActivityTO.setActivityType("Update");
            result = quotationService.updateQuotation(userActivityTO, quotation);
        }

        handleSaveResult(result, addOperation ? "created" : "updated");
    }

    public void sendQuotationAction() {
        if (selectedQuotation == null || selectedQuotation.getId() == null) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Email");
        GeneralConstants result = quotationService.sendQuotationEmail(userActivityTO, resolveCurrentOrganizationId(), selectedQuotation);

        switch (result) {
            case SUCCESSFUL:
                fetchQuotationList();
                addMessage(FacesMessage.SEVERITY_INFO, "Quotation sent",
                        "Quotation " + selectedQuotation.getQuotationReference() + " was emailed successfully.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Quotation does not exist.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Email failed",
                        "Unable to send quotation email. Check notification SMTP settings for the current organization.");
                break;
        }
    }

    public void confirmDeleteQuotation() {
        if (selectedQuotation == null || selectedQuotation.getId() == null) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = quotationService.deleteQuotation(userActivityTO, selectedQuotation);

        switch (result) {
            case SUCCESSFUL:
                fetchQuotationList();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Quotation deleted successfully.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Quotation does not exist.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete quotation.");
                break;
        }
    }

    public String buildPreviewReference() {
        if (!addOperation && selectedQuotation != null && selectedQuotation.getQuotationReference() != null) {
            return selectedQuotation.getQuotationReference();
        }
        return "QT-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public String getCurrencySummary() {
        return formatCurrencyTotals(buildCurrencyTotals());
    }

    public String getGrandTotalSummary() {
        return formatCurrencyTotals(buildCurrencyTotals());
    }

    public String getSectionTotalSummary(QuotationCostSection section) {
        return formatCurrencyTotals(buildCurrencyTotals(section));
    }

    public String sectionTotalSummary(QuotationCostSection section) {
        return getSectionTotalSummary(section);
    }

    public String lineTotalAmount(QuotationCostLine line) {
        return formatAmount(calculateLineAmount(line));
    }

    public int getDraftCount() {
        return countByStatus("DRAFT");
    }

    public int getSentCount() {
        return countByStatus("SENT");
    }

    public int getLinkedRequestCount() {
        int count = 0;
        for (Quotation quotation : quotationList) {
            if (quotation.getLinkedRequestReference() != null && !quotation.getLinkedRequestReference().trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public List<Quotation> getQuotationList() {
        return quotationList;
    }

    public List<CustomerRequest> getAvailableCustomerRequests() {
        return availableCustomerRequests;
    }

    public List<String> getAvailableCurrencyCodes() {
        return availableCurrencyCodes;
    }

    public Quotation getSelectedQuotation() {
        return selectedQuotation;
    }

    public void setSelectedQuotation(Quotation selectedQuotation) {
        this.selectedQuotation = selectedQuotation;
    }

    public boolean isAddOperation() {
        return addOperation;
    }

    public boolean isViewMode() {
        return viewMode;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public Integer getSelectedCustomerRequestId() {
        return selectedCustomerRequestId;
    }

    public void setSelectedCustomerRequestId(Integer selectedCustomerRequestId) {
        this.selectedCustomerRequestId = selectedCustomerRequestId;
    }

    public String getQuotationTitle() {
        return quotationTitle;
    }

    public void setQuotationTitle(String quotationTitle) {
        this.quotationTitle = quotationTitle;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getCargoSummary() {
        return cargoSummary;
    }

    public void setCargoSummary(String cargoSummary) {
        this.cargoSummary = cargoSummary;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public List<QuotationCostSection> getCostSections() {
        return costSections;
    }

    public List<String> getNoteLines() {
        return noteLines;
    }

    private void loadAvailableCustomerRequests() {
        availableCustomerRequests = new ArrayList<>(customerRequestService.getCustomerRequestList());
    }

    private void loadAvailableCurrencyCodes() {
        availableCurrencyCodes = new ArrayList<>();
        List<CurrencyDetails> currencyDetailsList = currencyDetailsService.getCurrencyDetailsList();
        if (currencyDetailsList == null) {
            return;
        }
        for (CurrencyDetails currencyDetails : currencyDetailsList) {
            if (currencyDetails != null && !isBlank(currencyDetails.getCurrencyCode())) {
                availableCurrencyCodes.add(currencyDetails.getCurrencyCode().trim().toUpperCase());
            }
        }
    }

    private void fetchQuotationList() {
        quotationList = new ArrayList<>(quotationService.getQuotationList());
        datatableRendered = CollectionUtils.isNotEmpty(quotationList);
        recordsCount = quotationList.size();
    }

    private void loadQuotationIntoForm(Integer quotationId) {
        Quotation persistentQuotation = quotationService.getQuotationById(quotationId);
        if (persistentQuotation == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Quotation not found.");
            return;
        }

        selectedQuotation = persistentQuotation;
        selectedCustomerRequestId = persistentQuotation.getCustomerRequest() == null
                ? null : persistentQuotation.getCustomerRequest().getId();
        quotationTitle = persistentQuotation.getQuotationTitle();
        serviceCategory = persistentQuotation.getServiceCategory();
        customerName = persistentQuotation.getCustomerName();
        contactPerson = persistentQuotation.getContactPerson();
        contactNumber = persistentQuotation.getContactNumber();
        recipientEmail = persistentQuotation.getRecipientEmail();
        originLocation = persistentQuotation.getOriginLocation();
        destinationLocation = persistentQuotation.getDestinationLocation();
        cargoSummary = persistentQuotation.getCargoSummary();
        validUntil = persistentQuotation.getValidUntil();
        emailSubject = persistentQuotation.getEmailSubject();
        emailBody = persistentQuotation.getEmailBody();
        costSections = deserializeSections(persistentQuotation.getPricingBreakdownJson());
        noteLines = deserializeNotes(persistentQuotation.getNoteLinesJson());
        ensureModelsInitialized();
    }

    private boolean populateQuotation(Quotation quotation) {
        if (!validateForm()) {
            return false;
        }

        refreshDerivedAmounts();
        CustomerRequest customerRequest = getSelectedCustomerRequestEntity();
        Map<String, BigDecimal> currencyTotals = buildCurrencyTotals();

        quotation.setCustomerRequest(customerRequest);
        quotation.setRequestReferenceSnapshot(customerRequest == null ? null : customerRequest.getRequestReference());
        quotation.setQuotationTitle(quotationTitle.trim());
        quotation.setServiceCategory(serviceCategory);
        quotation.setCustomerName(customerName.trim());
        quotation.setContactPerson(trimToNull(contactPerson));
        quotation.setContactNumber(trimToNull(contactNumber));
        quotation.setRecipientEmail(recipientEmail.trim());
        quotation.setOriginLocation(trimToNull(originLocation));
        quotation.setDestinationLocation(trimToNull(destinationLocation));
        quotation.setCargoSummary(trimToNull(cargoSummary));
        quotation.setCurrencyCode(resolvePrimaryCurrency(currencyTotals));
        quotation.setSubtotalAmount(resolvePrimaryTotal(currencyTotals));
        quotation.setTaxAmount(null);
        quotation.setTotalAmount(resolvePrimaryTotal(currencyTotals));
        quotation.setTotalSummaryLabel(getCurrencySummary());
        quotation.setPricingBreakdownJson(GSON.toJson(cleanSections(costSections), SECTION_LIST_TYPE));
        quotation.setNoteLinesJson(GSON.toJson(cleanNotes(noteLines), NOTE_LIST_TYPE));
        quotation.setValidUntil(validUntil);
        quotation.setEmailSubject(trimToNull(emailSubject));
        quotation.setEmailBody(trimToNull(emailBody));
        quotation.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        quotation.setFlag(true);
        return true;
    }

    private boolean validateForm() {
        if (isBlank(quotationTitle)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Quotation title required", "Enter a quotation title.");
            return false;
        }
        if (isBlank(serviceCategory)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Service category required", "Select a service category.");
            return false;
        }
        if (isBlank(customerName)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Customer name required", "Enter the customer name.");
            return false;
        }
        if (isBlank(recipientEmail) || !recipientEmail.contains("@")) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Recipient email required", "Enter a valid recipient email address.");
            return false;
        }
        if (validUntil == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Validity required", "Select the quotation validity date.");
            return false;
        }
        if (cleanSections(costSections).isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Costing required", "Add at least one costing line.");
            return false;
        }
        return true;
    }

    private void handleSaveResult(GeneralConstants result, String actionLabel) {
        switch (result) {
            case SUCCESSFUL:
                fetchQuotationList();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Quotation " + actionLabel + " successfully.");
                selectedQuotation = new Quotation();
                addOperation = true;
                viewMode = false;
                resetForm();
                break;
            case ENTRY_ALREADY_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Quotation reference already exists.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Quotation does not exist.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to save quotation.");
                break;
        }
    }

    private UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UserActivityTO userActivityTO = new UserActivityTO();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

        if (sessionMap != null) {
            Integer userId = (Integer) sessionMap.get("userAccountId");
            userActivityTO.setUserId(userId == null ? 0 : userId);
            userActivityTO.setUserName((String) sessionMap.get("username"));
            userActivityTO.setIpAddress((String) sessionMap.get("Machine IP"));
            userActivityTO.setDeviceInfo((String) sessionMap.get("Machine Name"));
            userActivityTO.setLocationInfo((String) sessionMap.get("browserClientInfo"));
            userActivityTO.setCreatedAt(new Date());
        }
        return userActivityTO;
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Object organizationId = facesContext.getExternalContext().getSessionMap().get("organizationId");
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private CustomerRequest getSelectedCustomerRequestEntity() {
        if (selectedCustomerRequestId == null) {
            return null;
        }
        return customerRequestService.getCustomerRequestById(selectedCustomerRequestId);
    }

    private int countByStatus(String status) {
        int count = 0;
        for (Quotation quotation : quotationList) {
            if (status.equals(quotation.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private String generateQuotationReference() {
        return "QT-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    private Map<String, BigDecimal> buildCurrencyTotals() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (QuotationCostSection section : cleanSections(costSections)) {
            mergeCurrencyTotals(totals, buildCurrencyTotals(section));
        }
        return totals;
    }

    private Map<String, BigDecimal> buildCurrencyTotals(QuotationCostSection section) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        if (section == null || section.getLines() == null) {
            return totals;
        }
        for (QuotationCostLine line : section.getLines()) {
            BigDecimal amount = calculateLineAmount(line);
            if (isBlank(line.getCurrencyCode()) || amount == null) {
                continue;
            }
            totals.merge(line.getCurrencyCode().trim().toUpperCase(), amount, BigDecimal::add);
        }
        return totals;
    }

    private String resolvePrimaryCurrency(Map<String, BigDecimal> totals) {
        if (totals.isEmpty()) {
            return "USD";
        }
        if (totals.size() == 1) {
            return totals.keySet().iterator().next();
        }
        return "MULTI";
    }

    private BigDecimal resolvePrimaryTotal(Map<String, BigDecimal> totals) {
        if (totals.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (totals.size() == 1) {
            return totals.values().iterator().next().setScale(2, RoundingMode.HALF_UP);
        }
        return totals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String formatAmount(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatCurrencyTotals(Map<String, BigDecimal> totals) {
        if (totals == null || totals.isEmpty()) {
            return "No costing yet";
        }
        return totals.entrySet().stream()
                .map(entry -> entry.getKey() + " " + formatAmount(entry.getValue()))
                .collect(Collectors.joining(" | "));
    }

    private void mergeCurrencyTotals(Map<String, BigDecimal> target, Map<String, BigDecimal> source) {
        for (Map.Entry<String, BigDecimal> entry : source.entrySet()) {
            target.merge(entry.getKey(), entry.getValue(), BigDecimal::add);
        }
    }

    private List<QuotationCostSection> cleanSections(List<QuotationCostSection> sections) {
        if (sections == null) {
            return Collections.emptyList();
        }

        List<QuotationCostSection> sanitizedSections = new ArrayList<>();
        for (QuotationCostSection section : sections) {
            if (section == null) {
                continue;
            }
            QuotationCostSection sanitizedSection = new QuotationCostSection();
            sanitizedSection.setTitle(trimToNull(section.getTitle()));
            List<QuotationCostLine> sanitizedLines = new ArrayList<>();
            if (section.getLines() != null) {
                for (QuotationCostLine line : section.getLines()) {
                    if (line == null || isBlank(line.getDescription())) {
                        continue;
                    }
                    QuotationCostLine sanitizedLine = new QuotationCostLine();
                    sanitizedLine.setDescription(line.getDescription().trim());
                    sanitizedLine.setCurrencyCode(trimToNull(line.getCurrencyCode()) == null
                            ? null : line.getCurrencyCode().trim().toUpperCase());
                    sanitizedLine.setQuantity(sanitizeDecimal(line.getQuantity(), 3));
                    sanitizedLine.setUnitPrice(sanitizeDecimal(line.getUnitPrice(), 2));
                    sanitizedLine.setAmount(calculateLineAmount(line));
                    sanitizedLine.setUnit(trimToNull(line.getUnit()));
                    sanitizedLine.setBasis(trimToNull(line.getBasis()));
                    sanitizedLines.add(sanitizedLine);
                }
            }
            if (!sanitizedLines.isEmpty()) {
                sanitizedSection.setLines(sanitizedLines);
                sanitizedSections.add(sanitizedSection);
            }
        }
        return sanitizedSections;
    }

    private List<String> cleanNotes(List<String> notes) {
        if (notes == null) {
            return Collections.emptyList();
        }
        return notes.stream()
                .map(this::trimToNull)
                .filter(note -> note != null)
                .collect(Collectors.toList());
    }

    private List<QuotationCostSection> deserializeSections(String json) {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        List<QuotationCostSection> sections = GSON.fromJson(json, SECTION_LIST_TYPE);
        return sections == null ? new ArrayList<>() : sections;
    }

    private List<String> deserializeNotes(String json) {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        List<String> notes = GSON.fromJson(json, NOTE_LIST_TYPE);
        return notes == null ? new ArrayList<>() : notes;
    }

    private void ensureModelsInitialized() {
        if (costSections == null) {
            costSections = new ArrayList<>();
        }
        if (costSections.isEmpty()) {
            QuotationCostSection defaultSection = new QuotationCostSection();
            defaultSection.setTitle("Primary Costing");
            defaultSection.getLines().add(new QuotationCostLine());
            costSections.add(defaultSection);
        } else {
            for (QuotationCostSection section : costSections) {
                if (section.getLines() == null || section.getLines().isEmpty()) {
                    section.setLines(new ArrayList<>());
                    section.getLines().add(new QuotationCostLine());
                }
            }
        }

        if (noteLines == null) {
            noteLines = new ArrayList<>();
        }
        if (noteLines.isEmpty()) {
            noteLines.add("");
        }

        refreshDerivedAmounts();
    }

    private void resetForm() {
        selectedCustomerRequestId = null;
        quotationTitle = null;
        serviceCategory = "FREIGHT_FORWARDING";
        customerName = null;
        contactPerson = null;
        contactNumber = null;
        recipientEmail = null;
        originLocation = null;
        destinationLocation = null;
        cargoSummary = null;
        validUntil = null;
        emailSubject = null;
        emailBody = null;
        costSections = new ArrayList<>();
        noteLines = new ArrayList<>();
        ensureModelsInitialized();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private void refreshDerivedAmounts() {
        if (costSections == null) {
            return;
        }
        for (QuotationCostSection section : costSections) {
            if (section == null || section.getLines() == null) {
                continue;
            }
            for (QuotationCostLine line : section.getLines()) {
                if (line == null) {
                    continue;
                }
                line.setAmount(calculateLineAmount(line));
            }
        }
    }

    private BigDecimal calculateLineAmount(QuotationCostLine line) {
        if (line == null || line.getQuantity() == null || line.getUnitPrice() == null) {
            return null;
        }
        return line.getQuantity()
                .multiply(line.getUnitPrice())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sanitizeDecimal(BigDecimal value, int scale) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }
}
