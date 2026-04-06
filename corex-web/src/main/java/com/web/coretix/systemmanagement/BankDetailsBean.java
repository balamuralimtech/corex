/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.web.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IBankDetailsService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.BankDetails;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.web.coretix.appgeneral.GenericManagedBean;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.NotificationService;
import org.springframework.context.annotation.Scope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.PrimeFaces;

/**
 *
 * Entity Backed Bean
 *
 * @author Pragadeesh
 *
 */
@Named("bankDetailsBean")
@Scope("session")
public class BankDetailsBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13355L;
    private static final Logger logger = LoggerFactory.getLogger(BankDetailsBean.class);
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
    private List<BankDetails> bankDetailsList = new ArrayList<>();

    private ResourceBundle resourceBundle;

    private String bDOrganizationName;
    private String searchBDOrganizationName;
    private String bankAccountDetails;

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;
    private BankDetails selectedBankDetails = new BankDetails();

    // Field validation flags
    private boolean organizationError = false;
    private boolean bankAccountDetailsError = false;

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IBankDetailsService bankDetailsService;

    public void initializePageAttributes()
    {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());


        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        if(CollectionUtils.isNotEmpty(bankDetailsList))
        {
            logger.debug("inside bankDetails list clear");
            bankDetailsList.clear();
        }

        PrimeFaces.current().ajax().update("form:bankDetailsMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields()
    {
        logger.debug("entered into resetFields action !!!");
        bDOrganizationName = "";
        searchBDOrganizationName="";

        bankAccountDetails = "";

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        organizationError = false;
        bankAccountDetailsError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void confirmEditButtonAction()
    {
        editBankDetails();
    }

    private void editBankDetails() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedBankDetails.getId() : " + selectedBankDetails.getId());

        bankAccountDetails = selectedBankDetails.getBankAccountDetails();
        bDOrganizationName = selectedBankDetails.getOrganization().getOrganizationName();

        logger.debug("bankAccountDetails : " + bankAccountDetails);
        logger.debug("bDOrganizationName : " + bDOrganizationName);

    }


    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizationList = new ArrayList<>();
        List<Organizations> org = organizationService.getOrganizationsList();
        for (Organizations Organization : org) {
            organizationList.add(Organization.getOrganizationName());
        }

        return organizationList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }


    public void saveBankDetails() {

        logger.debug("Inside save organization method ");

        logger.debug("isAddOperation : "+isAddOperation);
        logger.debug("bankAccountDetails : "+bankAccountDetails);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (bDOrganizationName == null || bDOrganizationName.trim().isEmpty()) {
            logger.debug("bDOrganizationName is null or empty");
            organizationError = true;
            hasErrors = true;
            errorFieldIds.add("form:orglist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Organization name is required"));
        }

        if (bankAccountDetails == null || bankAccountDetails.trim().isEmpty()) {
            logger.debug("bankAccountDetails is null or empty");
            bankAccountDetailsError = true;
            hasErrors = true;
            errorFieldIds.add("form:bankaccountdetails");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Bank account details are required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        BankDetails bankDetails = new BankDetails();
        Integer organizationId = resolveCurrentOrganizationId();
        bankDetails.setBankAccountDetails(bankAccountDetails);


        Organizations addOrganization = getOrganizationsByOrganizationName(bDOrganizationName);
        logger.debug("Org name " + addOrganization.getOrganizationName());
        if (addOrganization != null) {
            bankDetails.setOrganization(addOrganization);
        }


        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");

            bankDetails.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            bankDetails.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(bankDetails.getOrganization().getOrganizationName()+ " - New BankDetail Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = bankDetailsService.addBankDetails(userActivityTO,bankDetails);
            logger.debug("addStatus : "+addStatus);
            switch (addStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildBankDetailsChangeNotification(bankDetails.getOrganization().getOrganizationName(), "added"));
                    fetchBankDetailsList();
                    PrimeFaces.current().executeScript("PF('manageBDDialog').hide()");
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("bankDetailsAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("bankDetailsAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("bankDetailsAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }

        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedBankDetails.getId() : "+selectedBankDetails.getId());

            bankDetails.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Bank Details "+bankDetails.getOrganization().getOrganizationName()+" Updated");
            bankDetails.setId(selectedBankDetails.getId());
            GeneralConstants updateStatus = bankDetailsService.updateBankDetails(userActivityTO, bankDetails);
            switch (updateStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildBankDetailsChangeNotification(bankDetails.getOrganization().getOrganizationName(), "edited"));
                    fetchBankDetailsList();
                    PrimeFaces.current().executeScript("PF('manageBDDialog').hide()");
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("bankDetailsUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("bankDetailsAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("bankDetailsDoesNotExistLabel")));
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("bankDetailsUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }

        PrimeFaces.current().ajax().update("form:messages","form:bankDetailsMainPanelId");
    }

    public void confirmDeleteBankDetails() {
        deleteBankDetails();
    }

    private void deleteBankDetails()
    {
        logger.debug("inside delete Bank details method ");
        logger.debug("selectedBankDetails.getId() : "+selectedBankDetails.getId());

        bankAccountDetails = selectedBankDetails.getBankAccountDetails();
        bDOrganizationName = selectedBankDetails.getOrganization().getOrganizationName();

        logger.debug("bankAccountDetails : "+bankAccountDetails);
        logger.debug("bDOrganizationName : "+bDOrganizationName);
        Integer organizationId = resolveCurrentOrganizationId();


        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Bank Details "+bDOrganizationName+" Deleted");

        GeneralConstants deleteStatus = bankDetailsService.deleteBankDetails(userActivityTO, getSelectedBankDetails());
        switch (deleteStatus) {
            case SUCCESSFUL:
                notifyActiveOrganizationUsers(organizationId,
                        buildBankDetailsChangeNotification(bDOrganizationName, "deleted"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("bankDetailsRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("bankDetailsDoesNotExistLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("bankDetailsRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }
        fetchBankDetailsList();
        PrimeFaces.current().ajax().update("form:messages","form:bankDetailsDatatableId");
    }

    public UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

        if (httpSession != null) {
            logger.debug("httpSession.getId() : " + httpSession.getId());
            logger.debug("#############################################################################");
            logger.debug("{}", (Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            logger.debug("{}", (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            logger.debug("{}", (String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            logger.debug("{}", (String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            logger.debug("#############################################################################");

            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            // Assuming appropriate keys for the following attributes
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }

    public void onOrganizationSelect(){

        logger.debug("searchBDOrganizationName : "+searchBDOrganizationName);
        logger.debug("bDOrganizationName : "+bDOrganizationName);

    }

    public void searchButtonAction()
    {
        logger.debug("inside Bank Details List ");
        if (searchBDOrganizationName == null || searchBDOrganizationName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("errorLabel"), "Organization name cannot be empty!"));
            return;
        }

        logger.debug("searchBDOrganizationName : "+searchBDOrganizationName);
        fetchBankDetailsList();
    }

    private void fetchBankDetailsList()
    {
        logger.debug("inside Bank Details List ");
        if(CollectionUtils.isNotEmpty(bankDetailsList))
        {
            logger.debug("inside Bank Details list clear");
            bankDetailsList.clear();
        }

        logger.debug("searchBDOrganizationName : "+searchBDOrganizationName);
        Organizations searchOrg = getOrganizationsByOrganizationName(searchBDOrganizationName);
        logger.debug("searchOrg id : "+searchOrg.getId());

        bankDetailsList.addAll(bankDetailsService.getBankDetailsListByOrgId(searchOrg.getId()));

        logger.debug("bankDetailsList.size() : " + bankDetailsList.size());
        if (CollectionUtils.isNotEmpty(bankDetailsList)) {
            logger.debug("bankDetailsList.size() : " + bankDetailsList.size());
            datatableRendered = true;
            recordsCount = bankDetailsList.size();
        }
    }

    private Organizations getOrganizationsByOrganizationName(String organizationName)
    {

        Organizations organization = organizationService.getOrganizationsEntityByOrganizationName(organizationName);

        if (organization == null) {
            logger.warn("No organization found with the name: {}"+ organizationName);
        } else {
            logger.debug("Organization retrieved: {}"+ organization);
        }

        return organization;

    }

    // Getters and Setters

    /**
     * @return the selectedBankDetails
     */
    public BankDetails getSelectedBankDetails() {
        return selectedBankDetails;
    }

    /**
     * @param selectedBankDetails the selectedBankDetails to set
     */
    public void setSelectedBankDetails(BankDetails selectedBankDetails) {
        this.selectedBankDetails = selectedBankDetails;
    }

    public List<BankDetails> getBankDetailsList() {
        return bankDetailsList;
    }

    public void setBankAccountDetailsList(List<BankDetails> bankDetailsList) {
        this.bankDetailsList = bankDetailsList;
    }

    /**
     * @return the datatableRendered
     */
    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    /**
     * @param datatableRendered the datatableRendered to set
     */
    public void setDatatableRendered(boolean datatableRendered) {
        this.datatableRendered = datatableRendered;
    }

    /**
     * @return the recordsCount
     */
    public int getRecordsCount() {
        return recordsCount;
    }

    /**
     * @param recordsCount the recordsCount to set
     */
    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }


    public String getBankAccountDetails() {
        return bankAccountDetails;
    }

    public void setBankAccountDetails(String bankAccountDetails) {
        this.bankAccountDetails = bankAccountDetails;
    }

    public String getbDOrganizationName() {
        return bDOrganizationName;
    }

    public void setbDOrganizationName(String bDOrganizationName) {
        this.bDOrganizationName = bDOrganizationName;
    }

    public String getSearchBDOrganizationName() {
        return searchBDOrganizationName;
    }

    public void setSearchBDOrganizationName(String searchBDanizationName) {
        this.searchBDOrganizationName = searchBDanizationName;
    }

    // Getters for error flags
    public boolean isOrganizationError() {
        return organizationError;
    }

    public boolean isBankAccountDetailsError() {
        return bankAccountDetailsError;
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Object session = facesContext.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return null;
        }

        Object organizationId = ((HttpSession) session).getAttribute(SessionAttributes.ORGANIZATION_ID.getName());
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private void notifyActiveOrganizationUsers(Integer organizationId, String message) {
        NotificationService.sendGrowlMessageToOrganization(organizationId, message);
    }

    private String buildBankDetailsChangeNotification(String changedOrganizationName, String action) {
        String actorUserName = resolveCurrentUserName();
        String formattedDateTime = LocalDateTime.now().format(NOTIFICATION_DATE_TIME_FORMATTER);
        return "Bank details for organization '" + changedOrganizationName + "' were " + action + " by " + actorUserName + " on " + formattedDateTime + ".";
    }

    private String resolveCurrentUserName() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return "Unknown user";
        }

        Object session = facesContext.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return "Unknown user";
        }

        Object username = ((HttpSession) session).getAttribute(SessionAttributes.USERNAME.getName());
        return username instanceof String && !((String) username).trim().isEmpty()
                ? ((String) username).trim()
                : "Unknown user";
    }
}





