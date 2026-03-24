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
import com.persist.coretix.modal.systemmanagement.Cities;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.States;
import com.module.coretix.systemmanagement.ICityService;
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
@Named("cityBean")
@Scope("session")
public class CityBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535432L;
    private static final Logger logger = LoggerFactory.getLogger(CityBean.class);
    private List<Cities> citiesList = new ArrayList<>();

    private boolean isAddOperation;
    private boolean datatableRendered;
    private ResourceBundle resourceBundle;


    private int recordsCount;

    private Cities selectedCity;

    private String name;
    private String state;
    private String stateCode;
    private String country;
    private String countryCode;

    private String searchCountry;
    private String searchState;

    // Field validation flags
    private boolean nameError = false;
    private boolean countryError = false;
    private boolean stateError = false;

    @Inject
    private ICountryService countryService;
        @Inject
    private IStateService stateService;
        @Inject
        private ICityService cityService;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        setIsAddOperation(true);
        setDatatableRendered(false);

        setRecordsCount(0);
        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());



        resetFields();

        if (CollectionUtils.isNotEmpty(getCitiesList())) {
            logger.debug("inside  cityList clear");
            getCitiesList().clear();
        }

        PrimeFaces.current().ajax().update("form:cityMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");

        setName("");
        setState("");
        setStateCode("");
        setCountry("");
        setCountryCode("");
        setSearchCountry("");
        setSearchState("");

        // Reset error flags
        resetErrorFlags();
        logger.debug("end of resetFields action !!!");
    }

    private void resetErrorFlags() {
        nameError = false;
        countryError = false;
        stateError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        setIsAddOperation(true);
        resetFields();
    }

    public void onCountrySelect() {
        logger.debug("entered into onCountrySelect action !!!");
        logger.debug("searchCountry : "+searchCountry);
        logger.debug("searchState : "+searchState);
        logger.debug("end of onCountrySelect action !!!");
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        logger.debug("searchCountry : "+searchCountry);
        logger.debug("searchState : "+searchState);

        if (searchCountry != null || !searchCountry.isEmpty()) {
            logger.debug("inside ");
            if (searchState != null || !searchState.isEmpty()) {
                fetchCityList();
                logger.debug("end of searchButtonAction !!!");
            }
            else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("stateShouldNotBeEmptyLabel")));
            }

        }
        else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("countryShouldNotBeEmptyLabel")));
        }
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editCity();
    }

    private void editCity() {
        logger.debug("entered into edit button action !!!");
        setIsAddOperation(false);

        logger.debug("isAddOperation : " + isIsAddOperation());
        logger.debug("selectedCity.getId() : " + getSelectedCity().getId());

        setName(getSelectedCity().getName());
             setState(getSelectedCity().getState().getName());
        setStateCode(getSelectedCity().getCountryCode());
        setCountry(getSelectedCity().getCountry().getName());
        setCountryCode(getSelectedCity().getCountryCode());
    }
    public List<String> completeCountry(String query) {
        logger.debug("inside completeCountry : "+searchCountry+" "+searchState);
        String queryLowerCase = query.toLowerCase();
        List<String> countryList = new ArrayList<>();
        List<Countries> countries = countryService.getCountriesList();
        for (Countries country : countries) {
            countryList.add(country.getName());
        }
        return countryList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
       public List<String> completeState(String query) {
        logger.debug("inside completeState"+searchCountry+" "+searchState);
        String queryLowerCase = query.toLowerCase();
        List<String> stateList = new ArrayList<>();
        List<States> states = stateService.getStatesListByCountryId(countryService.getCountryEntityByCountryName(searchCountry).getId());
        for (States state : states) {
            stateList.add(state.getName());
        }
        return stateList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    private Countries getCountriesByCountryName(String countryName) {
        return countryService.getCountryEntityByCountryName(countryName);
    }
        private States getStatesByStateName(String stateName) {
        return stateService.getStateEntityByStateName(stateName);
    }


    public void saveCity() {

        logger.debug("Inside save city method ");
        logger.debug("isAddOperation : " + isIsAddOperation());

//        private String name;
//        private String state;
//        private String stateCode;
//        private String country;
//        private String countryCode;

        logger.debug("cityName : " + name);
        logger.debug("state : " + state);
        logger.debug("country : " + country);

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
            FacesContext.getCurrentInstance().addMessage("form:dialogMessages",
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "City name is required"));
        }

        if (country == null || country.trim().isEmpty()) {
            logger.debug("country is null or empty");
            countryError = true;
            hasErrors = true;
            errorFieldIds.add("form:countrylist");
            FacesContext.getCurrentInstance().addMessage("form:dialogMessages",
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Country is required"));
        }

        if (state == null || state.trim().isEmpty()) {
            logger.debug("state is null or empty");
            stateError = true;
            hasErrors = true;
            errorFieldIds.add("form:statelist");
            FacesContext.getCurrentInstance().addMessage("form:dialogMessages",
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "State is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            PrimeFaces.current().ajax().update("form:dialogMessages");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Cities city = new Cities();
        city.setName(getName());
        
        Countries addCountries = getCountriesByCountryName(getCountry());
        logger.debug("country name " + addCountries.getName());
        if (addCountries != null) {
            city.setCountry(addCountries);
        }
        city.setCountryCode(getCountryCode());

          States addStates = getStatesByStateName(getState());
        logger.debug("state name " + addStates.getName());
        if (addStates != null) {
            city.setState(addStates);
        }
        city.setStateCode(getStateCode());

        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isIsAddOperation()) {

            logger.debug("if (isAddOperation) {");
//
//            cityService.addCity(city);
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("City Added"));
//
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(city+getName()+" - New city Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = cityService.addCity(userActivityTO,city);
            logger.debug("addStatus : "+addStatus);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("cityAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("cityAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
            }

        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedCity.getId() : "+selectedCity.getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing City "+city.getName()+" Updated");
            city.setId(selectedCity.getId());
            GeneralConstants updateStatus = cityService.updateCity(userActivityTO,city);
            logger.debug("Update Status"+updateStatus);

            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("cityUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("cityAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("cityDoesNotExistsLabel")));
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("cityUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }
        fetchCityList();
        PrimeFaces.current().executeScript("PF('manageCityDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:cityDataTableId");
    }

//          cityService.updateCity(city);

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


    public void confirmDeleteCity() {
        deleteCity();
    }

    private void deleteCity() {

        logger.debug("inside delete city ");
        logger.debug("selectedCity.getId() : " + selectedCity.getId());



//        private String name;
//        private String state;
//        private String stateCode;
//        private String country;
//        private String countryCode;

        name = selectedCity.getName();
        country = selectedCity.getCountry().getName();
        state = selectedCity.getState().getName();


        logger.debug("Name : " + name);
        logger.debug("Country : " + country);
        logger.debug("State : " + state);

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" City "+name+" Deleted");

        GeneralConstants deleteStatus = cityService.deleteCity(userActivityTO,getSelectedCity());
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("City Removed Successfully"));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("cityDoesNotExistsLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("cityRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }

        fetchCityList();
        PrimeFaces.current().ajax().update("form:messages", "form:orgDataTableId");
    }

    private void fetchCityList() {
        setDatatableRendered(false);
        logger.debug("inside fetchCityList ");
        if (CollectionUtils.isNotEmpty(getCitiesList())) {
            logger.debug("inside fetchCityList clear");
            getCitiesList().clear();
        }
        logger.debug("searchCountry : "+searchCountry);
        logger.debug("searchState : "+searchState);
        getCitiesList().addAll(
                cityService.getCitiesListByCountryIdAndStateId(countryService.getCountryEntityByCountryName(searchCountry).getId(),
                stateService.getStateEntityByStateName(searchState).getId()));

        if (CollectionUtils.isNotEmpty(getCitiesList())) {
            logger.debug("citiesList.size() : " + getCitiesList().size());
            setDatatableRendered(true);
            setRecordsCount(getCitiesList().size());
        }
        else {
            setDatatableRendered(false);
            setRecordsCount(0);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("noCityFoundLabel")));
        }
    }

    public List<Cities> getCitiesList() {
        return citiesList;
    }

    public void setCitiesList(List<Cities> citiesList) {
        this.citiesList = citiesList;
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

    public Cities getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(Cities selectedCity) {
        this.selectedCity = selectedCity;
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
        public String getState() {
        return state;
    }

    public void setCountry(String country) {
        this.country = country;
    }
        public void setState(String state) {
        this.state = state;
    }

    public String getCountryCode() {
        return countryCode;
    }
    public String getStateCode() {
        return stateCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getSearchCountry() {
        return searchCountry;
    }

    public void setSearchCountry(String searchCountry) {
        this.searchCountry = searchCountry;
    }

    public String getSearchState() {
        return searchState;
    }

    public void setSearchState(String searchState) {
        this.searchState = searchState;
    }

    // Getters for error flags
    public boolean isNameError() {
        return nameError;
    }

    public boolean isCountryError() {
        return countryError;
    }

    public boolean isStateError() {
        return stateError;
    }
}



