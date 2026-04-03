package com.web.shipx.quotation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.ICurrencyDetailsService;
import com.module.shipx.quotation.IQuotationService;
import com.module.shipx.quotation.model.QuotationCostLine;
import com.module.shipx.quotation.model.QuotationCostSection;
import com.module.shipx.request.ICustomerRequestService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import com.persist.shipx.quotation.Quotation;
import com.persist.shipx.request.CustomerRequest;
import com.web.shipx.constants.QuotationStatusConstants;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private boolean editorPanelRendered;
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
    private String quotationStatus = QuotationStatusConstants.DRAFT.getValue();
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
        editorPanelRendered = false;
        recordsCount = 0;
        resetForm();
    }

    public void addButtonAction() {
        addOperation = true;
        viewMode = false;
        editorPanelRendered = true;
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
        editorPanelRendered = true;
        loadQuotationIntoForm(selectedQuotation.getId());
    }

    public void viewQuotationAction() {
        if (selectedQuotation == null || selectedQuotation.getId() == null) {
            return;
        }

        addOperation = false;
        viewMode = true;
        editorPanelRendered = true;
        loadQuotationIntoForm(selectedQuotation.getId());
    }

    public void newQuotationInlineAction() {
        addButtonAction();
    }

    public void closeEditorPanelAction() {
        selectedQuotation = new Quotation();
        addOperation = true;
        viewMode = false;
        editorPanelRendered = false;
        resetForm();
    }

    public void saveQuotation() {
        saveQuotationWithStatus(addOperation ? quotationStatus : null);
    }

    public void saveQuotationAsDraft() {
        saveQuotationWithStatus(QuotationStatusConstants.DRAFT.getValue());
    }

    public void saveQuotationAsNew() {
        saveQuotationWithStatus(QuotationStatusConstants.NEW.getValue());
    }

    private void saveQuotationWithStatus(String targetStatus) {
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
            quotation.setStatus(resolveQuotationStatus(targetStatus));
            quotation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            quotation.setCreatedByUserId(userActivityTO.getUserId());
            quotation.setCreatedByUserName(userActivityTO.getUserName());
            userActivityTO.setActivityType("Add");
            result = quotationService.addQuotation(userActivityTO, quotation);
        } else {
            quotation.setCreatedAt(selectedQuotation.getCreatedAt());
            quotation.setCreatedByUserId(selectedQuotation.getCreatedByUserId());
            quotation.setCreatedByUserName(selectedQuotation.getCreatedByUserName());
            quotation.setStatus(resolveQuotationStatus(quotationStatus));
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

    public StreamedContent downloadQuotationPdf(Quotation quotation) {
        if (quotation == null || quotation.getId() == null) {
            return null;
        }

        Quotation persistentQuotation = quotationService.getQuotationById(quotation.getId());
        if (persistentQuotation == null) {
            return null;
        }

        byte[] pdfBytes = buildQuotationPdf(persistentQuotation);
        String reference = isBlank(persistentQuotation.getQuotationReference())
                ? "quotation" : persistentQuotation.getQuotationReference().trim();

        return DefaultStreamedContent.builder()
                .name(reference + ".pdf")
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(pdfBytes))
                .build();
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
        return countByStatus(QuotationStatusConstants.DRAFT.getValue());
    }

    public int getSentCount() {
        return countByStatus(QuotationStatusConstants.WAITING_FOR_ACCEPTANCE.getValue());
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

    public boolean isEditorPanelRendered() {
        return editorPanelRendered;
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

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public List<String> getAvailableQuotationStatuses() {
        return QuotationStatusConstants.getAllValues();
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
        synchronizeExpiredStatuses(quotationList);
        datatableRendered = CollectionUtils.isNotEmpty(quotationList);
        recordsCount = quotationList.size();
    }

    private void loadQuotationIntoForm(Integer quotationId) {
        Quotation persistentQuotation = quotationService.getQuotationById(quotationId);
        if (persistentQuotation == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Quotation not found.");
            return;
        }

        persistentQuotation = synchronizeExpiredStatus(persistentQuotation);

        selectedQuotation = persistentQuotation;
        selectedCustomerRequestId = persistentQuotation.getCustomerRequest() == null
                ? null : persistentQuotation.getCustomerRequest().getId();
        quotationTitle = persistentQuotation.getQuotationTitle();
        serviceCategory = persistentQuotation.getServiceCategory();
        customerName = persistentQuotation.getCustomerName();
        contactPerson = persistentQuotation.getContactPerson();
        contactNumber = persistentQuotation.getContactNumber();
        recipientEmail = persistentQuotation.getRecipientEmail();
        quotationStatus = persistentQuotation.getStatus();
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
        quotation.setStatus(resolveQuotationStatus(quotationStatus));
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
        if (isExpiredDate(validUntil)) {
            quotation.setStatus(QuotationStatusConstants.EXPIRED.getValue());
        }
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
                editorPanelRendered = false;
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

    private byte[] buildQuotationPdf(Quotation quotation) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font companyFont = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(59, 65, 72));
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(59, 65, 72));
            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(59, 65, 72));
            Font quoteTitleFont = new Font(Font.HELVETICA, 28, Font.BOLD, new Color(59, 65, 72));
            Font tableHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font totalFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(59, 65, 72));

            PdfPTable headerTable = new PdfPTable(new float[]{1.1f, 0.9f});
            headerTable.setWidthPercentage(100f);
            headerTable.setSpacingAfter(18f);

            PdfPCell leftHeader = new PdfPCell();
            leftHeader.setBorder(Rectangle.NO_BORDER);
            leftHeader.addElement(new Paragraph("Your Company Inc.", companyFont));
            leftHeader.addElement(new Paragraph("1234 Company St,", bodyFont));
            leftHeader.addElement(new Paragraph("Company Town, ST 12345", bodyFont));
            leftHeader.setPaddingTop(10f);
            headerTable.addCell(leftHeader);

            PdfPCell rightHeader = new PdfPCell();
            rightHeader.setBorder(Rectangle.NO_BORDER);
            rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPTable logoTable = new PdfPTable(1);
            logoTable.setWidthPercentage(88f);
            PdfPCell logoCell = new PdfPCell(new Phrase("Upload Logo", labelFont));
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setFixedHeight(42f);
            logoCell.setBorderColor(new Color(150, 150, 150));
            logoCell.setPaddingTop(12f);
            logoTable.addCell(logoCell);
            rightHeader.addElement(logoTable);
            headerTable.addCell(rightHeader);
            document.add(headerTable);

            PdfPTable quoteInfoTable = new PdfPTable(new float[]{1f, 1f});
            quoteInfoTable.setWidthPercentage(100f);
            quoteInfoTable.setSpacingAfter(18f);

            PdfPCell billToCell = new PdfPCell();
            billToCell.setBorder(Rectangle.NO_BORDER);
            billToCell.addElement(new Paragraph("Bill To", labelFont));
            billToCell.addElement(new Paragraph(safeText(quotation.getCustomerName(), "Customer Name"), new Font(Font.HELVETICA, 14, Font.BOLD, new Color(59, 65, 72))));
            if (!isBlank(quotation.getRecipientEmail())) {
                billToCell.addElement(new Paragraph(quotation.getRecipientEmail().trim(), bodyFont));
            }
            if (!isBlank(quotation.getContactPerson())) {
                billToCell.addElement(new Paragraph(safeText(quotation.getContactPerson(), "-"), bodyFont));
            }
            if (!isBlank(quotation.getContactNumber())) {
                billToCell.addElement(new Paragraph(safeText(quotation.getContactNumber(), "-"), bodyFont));
            }
            if (!isBlank(quotation.getOriginLocation()) || !isBlank(quotation.getDestinationLocation())) {
                billToCell.addElement(new Paragraph(safeText(quotation.getOriginLocation(), "-")
                        + " -> " + safeText(quotation.getDestinationLocation(), "-"), bodyFont));
            }
            quoteInfoTable.addCell(billToCell);

            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            Paragraph quoteHeading = new Paragraph("Quotation", quoteTitleFont);
            quoteHeading.setAlignment(Element.ALIGN_RIGHT);
            quoteHeading.setSpacingAfter(14f);
            metaCell.addElement(quoteHeading);

            PdfPTable metaTable = new PdfPTable(new float[]{1f, 1f});
            metaTable.setWidthPercentage(70f);
            metaTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addMetaCell(metaTable, "Quote #", labelFont, Element.ALIGN_LEFT);
            addMetaCell(metaTable, safeText(quotation.getQuotationReference(), "-"), bodyFont, Element.ALIGN_RIGHT);
            addMetaCell(metaTable, "Quote date", labelFont, Element.ALIGN_LEFT);
            addMetaCell(metaTable, formatDateValue(quotation.getCreatedAt() == null ? null : new Date(quotation.getCreatedAt().getTime())), bodyFont, Element.ALIGN_RIGHT);
            addMetaCell(metaTable, "Due date", labelFont, Element.ALIGN_LEFT);
            addMetaCell(metaTable, formatDateValue(quotation.getValidUntil()), bodyFont, Element.ALIGN_RIGHT);
            metaCell.addElement(metaTable);
            quoteInfoTable.addCell(metaCell);
            document.add(quoteInfoTable);

            PdfPTable itemsTable = new PdfPTable(new float[]{0.7f, 4.0f, 1.2f, 1.3f});
            itemsTable.setWidthPercentage(100f);
            itemsTable.setSpacingAfter(12f);
            addStyledHeaderCell(itemsTable, "QTY", tableHeaderFont);
            addStyledHeaderCell(itemsTable, "Description", tableHeaderFont);
            addStyledHeaderCell(itemsTable, "Unit Price", tableHeaderFont);
            addStyledHeaderCell(itemsTable, "Amount", tableHeaderFont);

            List<QuotationCostSection> sections = cleanSections(deserializeSections(quotation.getPricingBreakdownJson()));
            for (QuotationCostSection section : sections) {
                PdfPCell sectionCell = new PdfPCell(new Phrase(safeText(section.getTitle(), "Costing Section"), labelFont));
                sectionCell.setColspan(4);
                sectionCell.setBorder(Rectangle.NO_BORDER);
                sectionCell.setPaddingTop(8f);
                sectionCell.setPaddingBottom(4f);
                itemsTable.addCell(sectionCell);

                for (QuotationCostLine line : section.getLines()) {
                    itemsTable.addCell(buildLineCell(formatQtyValue(line.getQuantity()), Element.ALIGN_CENTER));
                    itemsTable.addCell(buildLineCell(buildDescriptionValue(line), Element.ALIGN_LEFT));
                    itemsTable.addCell(buildLineCell(formatOptionalAmount(line.getUnitPrice()), Element.ALIGN_RIGHT));
                    itemsTable.addCell(buildLineCell(formatCurrencyAmount(line), Element.ALIGN_RIGHT));
                }
            }
            document.add(itemsTable);

            PdfPTable totalsTable = new PdfPTable(new float[]{1.3f, 0.7f});
            totalsTable.setWidthPercentage(44f);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setSpacingAfter(20f);
            addTotalRow(totalsTable, "Subtotal", safeText(quotation.getTotalSummaryLabel(), "No costing yet"), bodyFont, false);
            addTotalRow(totalsTable, "Sales Tax (0%)", "$0.00", bodyFont, false);
            addTotalRow(totalsTable, "Total (" + safeText(quotation.getCurrencyCode(), "USD") + ")", safeText(quotation.getTotalSummaryLabel(), "No costing yet"), totalFont, true);
            document.add(totalsTable);

            Paragraph termsTitle = new Paragraph("Terms and Conditions", labelFont);
            termsTitle.setSpacingAfter(6f);
            document.add(termsTitle);
            List<String> notes = cleanNotes(deserializeNotes(quotation.getNoteLinesJson()));
            if (notes.isEmpty()) {
                document.add(new Paragraph("Payment is due on or before the validity date.", bodyFont));
                document.add(new Paragraph("Please review the quotation details before confirmation.", bodyFont));
            } else {
                for (String note : notes) {
                    document.add(new Paragraph(note, bodyFont));
                }
            }

            PdfPTable signatureTable = new PdfPTable(1);
            signatureTable.setWidthPercentage(40f);
            signatureTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            signatureTable.setSpacingBefore(60f);
            PdfPCell signatureLine = new PdfPCell(new Phrase("customer signature", bodyFont));
            signatureLine.setHorizontalAlignment(Element.ALIGN_CENTER);
            signatureLine.setBorder(Rectangle.TOP);
            signatureLine.setPaddingTop(6f);
            signatureLine.setBorderColorTop(new Color(80, 80, 80));
            signatureLine.setBorderWidthTop(1f);
            signatureLine.setBorderWidthLeft(0f);
            signatureLine.setBorderWidthRight(0f);
            signatureLine.setBorderWidthBottom(0f);
            signatureTable.addCell(signatureLine);
            document.add(signatureTable);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate quotation PDF.", exception);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addPdfHeaderCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addStyledHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(17, 43, 77));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(7f);
        table.addCell(cell);
    }

    private void addMetaCell(PdfPTable table, String value, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(3f);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String amount, Font font, boolean highlight) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        PdfPCell amountCell = new PdfPCell(new Phrase(amount, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(6f);
        amountCell.setPadding(6f);
        if (highlight) {
            Color bg = new Color(240, 242, 245);
            labelCell.setBackgroundColor(bg);
            amountCell.setBackgroundColor(bg);
            labelCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            amountCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        } else {
            labelCell.setBorder(Rectangle.NO_BORDER);
            amountCell.setBorder(Rectangle.NO_BORDER);
        }
        table.addCell(labelCell);
        table.addCell(amountCell);
    }

    private PdfPCell buildLineCell(String value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell buildPdfCell(String value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        return cell;
    }

    private String formatOptionalAmount(BigDecimal value) {
        return value == null ? "-" : formatAmount(value);
    }

    private String formatQtyValue(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.scale() <= 0 ? normalized.toPlainString() : value.toPlainString();
    }

    private String buildDescriptionValue(QuotationCostLine line) {
        StringBuilder builder = new StringBuilder(safeText(line.getDescription(), "-"));
        if (!isBlank(line.getUnit())) {
            builder.append(" (").append(line.getUnit().trim()).append(")");
        }
        if (!isBlank(line.getBasis())) {
            builder.append("\n").append(line.getBasis().trim());
        }
        return builder.toString();
    }

    private String formatCurrencyAmount(QuotationCostLine line) {
        if (line == null || line.getAmount() == null) {
            return "-";
        }
        String currency = trimToNull(line.getCurrencyCode());
        return (currency == null ? "" : currency + " ") + formatAmount(line.getAmount());
    }

    private String formatDateValue(Date value) {
        return value == null ? "-" : new SimpleDateFormat("dd-MMM-yyyy").format(value);
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
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
        quotationStatus = QuotationStatusConstants.DRAFT.getValue();
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

    private void synchronizeExpiredStatuses(List<Quotation> quotations) {
        if (quotations == null) {
            return;
        }
        for (int index = 0; index < quotations.size(); index++) {
            quotations.set(index, synchronizeExpiredStatus(quotations.get(index)));
        }
    }

    private Quotation synchronizeExpiredStatus(Quotation quotation) {
        if (quotation == null || quotation.getId() == null || !isExpiredDate(quotation.getValidUntil())) {
            return quotation;
        }
        if (QuotationStatusConstants.EXPIRED.getValue().equalsIgnoreCase(quotation.getStatus())) {
            return quotation;
        }

        quotation.setStatus(QuotationStatusConstants.EXPIRED.getValue());
        quotation.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        quotationService.updateQuotation(populateExpiryUserActivity(), quotation);
        return quotationService.getQuotationById(quotation.getId());
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

    private String resolveQuotationStatus(String statusValue) {
        if (isBlank(statusValue)) {
            return QuotationStatusConstants.DRAFT.getValue();
        }
        try {
            if (isExpiredDate(validUntil)) {
                return QuotationStatusConstants.EXPIRED.getValue();
            }
            return QuotationStatusConstants.getByValue(statusValue).getValue();
        } catch (IllegalArgumentException exception) {
            return QuotationStatusConstants.DRAFT.getValue();
        }
    }

    private boolean isExpiredDate(Date value) {
        if (value == null) {
            return false;
        }
        LocalDate validDate;
        if (value instanceof java.sql.Date) {
            validDate = ((java.sql.Date) value).toLocalDate();
        } else {
            validDate = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return validDate.isBefore(LocalDate.now());
    }

    private UserActivityTO populateExpiryUserActivity() {
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Auto Expire");
        return userActivityTO;
    }
}
