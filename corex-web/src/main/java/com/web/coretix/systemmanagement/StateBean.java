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

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.States;
import com.module.coretix.systemmanagement.ICountryService;
import com.module.coretix.systemmanagement.IStateService;

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
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author Pragadeesh
 */
@Named("stateBean")
@Scope("session")
public class StateBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(StateBean.class);
    private List<States> statesList = new ArrayList<>();

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    private States selectedState;

    private ResourceBundle resourceBundle;

    private String name;
    private String country;
    private String countryCode;
    private String fipsCode;
    private String iso2;
    private String type;

    // Field validation flags
    private boolean nameError = false;
    private boolean countryError = false;
    private boolean typeError = false;

    private String searchCountry;

    @Inject
    private ICountryService countryService;
    @Inject
    private IStateService stateService;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());

        setIsAddOperation(true);
        setDatatableRendered(false);
        setRecordsCount(0);

        resetFields();

        if (CollectionUtils.isNotEmpty(getStatesList())) {
            logger.debug("inside  statesList clear");
            getStatesList().clear();
        }

        PrimeFaces.current().ajax().update("form:stateMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");

        setName("");
        setCountry("");
        setCountryCode("");
        setFipsCode("");
        setIso2("");
        setType("");
        setSearchCountry("");

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        nameError = false;
        countryError = false;
        typeError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        setIsAddOperation(true);
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        if (searchCountry != null && !searchCountry.isEmpty()) {
            fetchStateList();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("countryShouldNotBeEmptyLabel")));
        }

        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editState();
    }

    private void editState() {
        logger.debug("entered into edit button action !!!");
        setIsAddOperation(false);

        logger.debug("isAddOperation : " + isIsAddOperation());
        logger.debug("selectedState.getId() : " + getSelectedState().getId());

        setName(getSelectedState().getName());
        setCountry(getSelectedState().getCountry().getName());
        setCountryCode(getSelectedState().getCountryCode());
        setFipsCode(getSelectedState().getFipsCode());
        setIso2(getSelectedState().getIso2());
        setType(getSelectedState().getType());

        name = selectedState.getName();
        country=selectedState.getCountry().getName();
        countryCode=selectedState.getCountryCode();
        fipsCode=selectedState.getFipsCode();
        iso2=selectedState.getIso2();
        type=selectedState.getType();

        logger.debug("stateName : " +name);
        logger.debug("country : " +country);
        logger.debug("countryCode : " +countryCode);
        logger.debug("fipsCode : " +fipsCode);
        logger.debug("iso2 : " +iso2);
        logger.debug("type : " +type);


    }

    public void onCountrySelect(){
        logger.debug("searchCountry : "+searchCountry);
    }

    public List<String> completeCountry(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> countryList = new ArrayList<>();
        List<Countries> countries = countryService.getCountriesList();
        for (Countries country : countries) {
            countryList.add(country.getName());
        }
        return countryList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    private Countries getCountriesByCountryName(String countryName) {
        return countryService.getCountryEntityByCountryName(countryName);
    }

    public void saveState() {

        logger.debug("Inside save state method ");
        logger.debug("stateName : " +name);
        logger.debug("country : " +country);
        logger.debug("countryCode : " +countryCode);
        logger.debug("fipsCode : " +fipsCode);
        logger.debug("iso2 : " +iso2);
        logger.debug("type : " +type);
        logger.debug("isAddOperation : " + isIsAddOperation());

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (name == null || name.trim().isEmpty()) {
            logger.debug("name is null or empty");
            nameError = true;
            hasErrors = true;
            errorFieldIds.add("form:name");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "State name is required"));
        }

        if (country == null || country.trim().isEmpty()) {
            logger.debug("country is null or empty");
            countryError = true;
            hasErrors = true;
            errorFieldIds.add("form:countrylist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Country is required"));
        }

        if (type == null || type.trim().isEmpty()) {
            logger.debug("type is null or empty");
            typeError = true;
            hasErrors = true;
            errorFieldIds.add("form:type");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Type is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        States state = new States();
        state.setName(getName());
        Countries addCountires = getCountriesByCountryName(getCountry());
        logger.debug("country name " + addCountires.getName());
        if (addCountires != null) {
            state.setCountry(addCountires);
        }
        state.setCountryCode(getCountryCode());
        state.setFipsCode(getFipsCode());
        state.setIso2(getIso2());
        state.setType(getType());

        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isIsAddOperation()) {

            logger.debug("if (isAddOperation) {");
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(state.getName()+ " - New State Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = stateService.addState(userActivityTO, state);

            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("stateAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("stateAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("stateAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedState.getId() : " + getSelectedState().getId());
            state.setId(getSelectedState().getId());

            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing State "+state.getName()+" Updated");
            GeneralConstants updateStatus = stateService.updateState(userActivityTO, state);

            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("stateUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("stateAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("stateDoesNotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("stateUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }

        fetchStateList();
        PrimeFaces.current().executeScript("PF('manageStateDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:stateDataTableId");
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

    public void confirmDeleteState() {
        deleteState();
    }

    private void deleteState() {
        logger.debug("inside delete State ");
        logger.debug("selectedState.getId() : " + getSelectedState().getId());


        name = selectedState.getName();
        country=selectedState.getCountry().getName();
        countryCode=selectedState.getCountryCode();
        fipsCode=selectedState.getFipsCode();
        iso2=selectedState.getIso2();
        type=selectedState.getType();

        logger.debug("stateName : " +name);
        logger.debug("country : " +country);
        logger.debug("countryCode : " +countryCode);
        logger.debug("fipsCode : " +fipsCode);
        logger.debug("iso2 : " +iso2);
        logger.debug("type : " +type);
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" State "+name+" Deleted");

        GeneralConstants deleteStatus = stateService.deleteState(userActivityTO,getSelectedState());

        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("stateRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("stateDoesNotExistsLabel")));
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("stateRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }
        fetchStateList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("State Removed"));
        PrimeFaces.current().ajax().update("form:messages", "form:stateDataTableId");
    }

    private void fetchStateList() {
        setDatatableRendered(false);
        logger.debug("inside fetchStateList ");
        if (CollectionUtils.isNotEmpty(getStatesList())) {
            logger.debug("inside fetchStateList clear");
            getStatesList().clear();
        }
        getStatesList().addAll(stateService.getStatesListByCountryId(countryService.getCountryEntityByCountryName(searchCountry).getId()));

        if (CollectionUtils.isNotEmpty(getStatesList())) {
            logger.debug("statesList.size() : " + getStatesList().size());
            setDatatableRendered(true);
            setRecordsCount(getStatesList().size());
        }
        else {
            setDatatableRendered(false);
            setRecordsCount(0);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("noStateFoundLabel")));
        }
    }

    public List<States> getStatesList() {
        return statesList;
    }

    public void setStatesList(List<States> statesList) {
        this.statesList = statesList;
    }

    public boolean isIsAddOperation() {
        return isAddOperation;
    }

    public void setIsAddOperation(boolean isAddOperation) {
        this.isAddOperation = isAddOperation;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public void setDatatableRendered(boolean datatableRendered) {
        this.datatableRendered = datatableRendered;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    public States getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(States selectedState) {
        this.selectedState = selectedState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFipsCode() {
        return fipsCode;
    }

    public void setFipsCode(String fipsCode) {
        this.fipsCode = fipsCode;
    }

    public String getIso2() {
        return iso2;
    }

    public void setIso2(String iso2) {
        this.iso2 = iso2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSearchCountry() {
        return searchCountry;
    }

    public void setSearchCountry(String searchCountry) {
        this.searchCountry = searchCountry;
    }

    // Getters for error flags
    public boolean isNameError() {
        return nameError;
    }

    public boolean isCountryError() {
        return countryError;
    }

    public boolean isTypeError() {
        return typeError;
    }
}




