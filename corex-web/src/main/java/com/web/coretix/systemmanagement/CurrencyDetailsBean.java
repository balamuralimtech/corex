/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
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
 * Project: `app.name`
 */
package com.web.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import com.module.coretix.systemmanagement.ICurrencyDetailsService;

import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author admin
 */
@Named("currencyDetailsBean")
@Scope("session")
public class CurrencyDetailsBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(CurrencyDetailsBean.class);
    private List<CurrencyDetails> currenciesList = new ArrayList<>();
    
    private String currencyName;
    private String currencyCode;
    private String symbol;
    private ResourceBundle resourceBundle;


    private boolean isAddOperation;
    private boolean datatableRendered;
    
    private int recordsCount;

    private boolean currencyNameError = false;
    private boolean currencyCodeError = false;
    private boolean symbolError = false;

    private CurrencyDetails selectedCurrencyDetails = new CurrencyDetails();

    @Inject
    private ICurrencyDetailsService currencyDetailsService;


    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());


        currencyName ="";
        currencyCode ="";
        symbol ="";

        if (CollectionUtils.isNotEmpty(getCurrenciesList())) {
            logger.debug("inside  organizationList clear");
            getCurrenciesList().clear();
        }
        
        PrimeFaces.current().ajax().update("form:currencyMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");
        currencyName ="";
        currencyCode ="";
        symbol ="";

        resetErrorFlags();
    }

    private void resetErrorFlags() {
        currencyNameError = false;
        currencyCodeError = false;
        symbolError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchCurrenciesList();
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editCurrency();
    }

    private void editCurrency() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedOrganization.getId() : " + getSelectedCurrencyDetails().getCurrencyId());

        currencyName = selectedCurrencyDetails.getCurrencyName();
        currencyCode= selectedCurrencyDetails.getCurrencyCode();
        symbol=selectedCurrencyDetails.getSymbol();

        logger.debug("selectedOrganization.getId() : " + getSelectedCurrencyDetails().getCurrencyId());
        logger.debug("selectedOrganization.getName() : " + getSelectedCurrencyDetails().getCurrencyName());
        logger.debug("selectedOrganization.getCode() : " + getSelectedCurrencyDetails().getCurrencyCode());
        logger.debug("selectedOrganization.getSymbol() : " + getSelectedCurrencyDetails().getSymbol());
    }

    public void saveCurrency() {

        logger.debug("Inside save organization method ");
        logger.debug("isAddOperation : " + isAddOperation);

        logger.debug("selectedOrganization.getId() : " + getSelectedCurrencyDetails().getCurrencyId());
        logger.debug("selectedOrganization.getName() : " + getSelectedCurrencyDetails().getCurrencyName());
        logger.debug("selectedOrganization.getCode() : " + getSelectedCurrencyDetails().getCurrencyCode());
        logger.debug("selectedOrganization.getSymbol() : " + getSelectedCurrencyDetails().getSymbol());

        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        if (currencyName == null || currencyName.trim().isEmpty()) {
            logger.debug("currencyName is null or empty");
            currencyNameError = true;
            hasErrors = true;
            errorFieldIds.add("form:name");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Currency name is required"));
        }

        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            logger.debug("currencyCode is null or empty");
            currencyCodeError = true;
            hasErrors = true;
            errorFieldIds.add("form:currencyCode");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Currency code is required"));
        }

        if (symbol == null || symbol.trim().isEmpty()) {
            logger.debug("symbol is null or empty");
            symbolError = true;
            hasErrors = true;
            errorFieldIds.add("form:currencySymbol");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Currency symbol is required"));
        }

        if (hasErrors) {
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        CurrencyDetails currencyDetail = new CurrencyDetails();

        currencyDetail.setCurrencyName(getCurrencyName());
        currencyDetail.setCurrencyCode(getCurrencyCode());
        currencyDetail.setSymbol(getSymbol());


        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");

            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(currencyDetail.getCurrencyName()+ " - New Currency Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = currencyDetailsService.addCurrencyDetails(userActivityTO, currencyDetail);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("currencyAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("currencyAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("currencyAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedCurrencyDetails.getId() : " + selectedCurrencyDetails.getCurrencyId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Currency "+currencyDetail.getCurrencyName()+" Updated");
            currencyDetail.setCurrencyId(selectedCurrencyDetails.getCurrencyId());
            GeneralConstants updateStatus = currencyDetailsService.updateCurrencyDetails(userActivityTO, currencyDetail);
            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("currencyUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("currencyAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("currencyDoesnotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("currencyUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }
        fetchCurrenciesList();
        PrimeFaces.current().executeScript("PF('manageCurrDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:currDataTableId");
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

    public void confirmDeleteCurrency() {
        deleteCurrency();
    }

    private void deleteCurrency() {
        logger.debug("inside delete organization ");
        logger.debug("selectedCurrencyDetails.getId() : " + getSelectedCurrencyDetails().getCurrencyId());

        currencyName = selectedCurrencyDetails.getCurrencyName();
        currencyCode= selectedCurrencyDetails.getCurrencyCode();
        symbol=selectedCurrencyDetails.getSymbol();

        logger.debug("selectedOrganization.getId() : " + getSelectedCurrencyDetails().getCurrencyId());
        logger.debug("selectedOrganization.getName() : " + getSelectedCurrencyDetails().getCurrencyName());
        logger.debug("selectedOrganization.getCode() : " + getSelectedCurrencyDetails().getCurrencyCode());
        logger.debug("selectedOrganization.getSymbol() : " + getSelectedCurrencyDetails().getSymbol());

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" CurrencyDetail "+currencyName+" Deleted");

       GeneralConstants deleteStatus = currencyDetailsService.deleteCurrencyDetails(userActivityTO, getSelectedCurrencyDetails());
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("currencyRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("currencyDoesnotExistsLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("currencyRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }

        fetchCurrenciesList();
        PrimeFaces.current().ajax().update("form:messages", "form:currDataTableId");
    }

    private void fetchCurrenciesList() {
        datatableRendered = false;
        logger.debug("inside fetchOrganizationList ");
        if (CollectionUtils.isNotEmpty(getCurrenciesList())) {
            logger.debug("inside fetchOrganizationList clear");
            getCurrenciesList().clear();
        }
        getCurrenciesList().addAll(currencyDetailsService.getCurrencyDetailsList());

        if (CollectionUtils.isNotEmpty(getCurrenciesList())) {
            logger.debug("organizationList.size() : " + getCurrenciesList().size());
            datatableRendered = true;
            recordsCount = getCurrenciesList().size();
        }
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

    /**
     * @return the currenciesList
     */
    public List<CurrencyDetails> getCurrenciesList() {
        return currenciesList;
    }

    /**
     * @param currenciesList the currenciesList to set
     */
    public void setCurrenciesList(List<CurrencyDetails> currenciesList) {
        this.currenciesList = currenciesList;
    }

    /**
     * @return the currencyName
     */
    public String getCurrencyName() {
        return currencyName;
    }

    /**
     * @param currencyName the currencyName to set
     */
    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    /**
     * @return the currencyCode
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * @param currencyCode the currencyCode to set
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return the selectedCurrencyDetails
     */
    public CurrencyDetails getSelectedCurrencyDetails() {
        return selectedCurrencyDetails;
    }

    /**
     * @param selectedCurrencyDetails the selectedCurrencyDetails to set
     */
    public void setSelectedCurrencyDetails(CurrencyDetails selectedCurrencyDetails) {
        this.selectedCurrencyDetails = selectedCurrencyDetails;
    }

    public boolean isCurrencyNameError() {
        return currencyNameError;
    }

    public boolean isCurrencyCodeError() {
        return currencyCodeError;
    }

    public boolean isSymbolError() {
        return symbolError;
    }

}




