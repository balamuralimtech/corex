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
import com.persist.coretix.modal.systemmanagement.Regions;
import com.persist.coretix.modal.systemmanagement.Subregions;
import com.module.coretix.systemmanagement.ICountryService;
import com.module.coretix.systemmanagement.IRegionService;
import com.module.coretix.systemmanagement.ISubRegionService;
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
 * @author admin
 */
@Named("countryBean")
@Scope("session")
public class CountryBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(CountryBean.class);
    private List<Countries> countriesList = new ArrayList<>();

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    private Countries selectedCountry;

    private ResourceBundle resourceBundle;

    private String name;
    private String iso3;
    private String numericCode;
    private String iso2;
    private String phonecode;
    private String capital;
    private String currency;
    private String currencyName;
    private String currencySymbol;
    private String tld;
    private String nativeName;
    private String region;
    private String subregion;
    private String nationality;
    private String timezones;
    private String translations;

    // Field validation flags
    private boolean nameError = false;
    private boolean iso2Error = false;
    private boolean iso3Error = false;
    private boolean numericCodeError = false;
    private boolean phonecodeError = false;
    private boolean capitalError = false;
    private boolean currencyError = false;
    private boolean currencyNameError = false;
    private boolean currencySymbolError = false;
    private boolean regionError = false;
    private boolean subregionError = false;

    @Inject
    private ICountryService countryService;

    @Inject
    private IRegionService regionService;

    @Inject
    private ISubRegionService subregionService;

    /**
     * @return the countriesList
     */
    public List<Countries> getCountriesList() {
        return countriesList;
    }

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());

        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        
        name = "";
        iso3 = "";
        numericCode = "";
        iso2 = "";
        phonecode = "";
        capital = "";
        currency = "";
        currencyName = "";
        currencySymbol = "";
        tld = "";
        nativeName = "";
        region = "";
        subregion = "";
        nationality = "";
        timezones = "";
        translations = "";

        if (CollectionUtils.isNotEmpty(countriesList)) {
            logger.debug("inside  countriesList clear");
            countriesList.clear();
        }

        PrimeFaces.current().ajax().update("form:countryMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");

        name = "";
        iso3 = "";
        numericCode = "";
        iso2 = "";
        phonecode = "";
        capital = "";
        currency = "";
        currencyName = "";
        currencySymbol = "";
        tld = "";
        nativeName = "";
        region = "";
        subregion = "";
        nationality = "";
        timezones = "";
        translations = "";

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        nameError = false;
        iso2Error = false;
        iso3Error = false;
        numericCodeError = false;
        phonecodeError = false;
        capitalError = false;
        currencyError = false;
        currencyNameError = false;
        currencySymbolError = false;
        regionError = false;
        subregionError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchCountryList();
        logger.debug("end of searchButtonAction !!!");
    }

    private Regions getRegionsByRegionName(String regionName) {
        return regionService.getRegionByRegionName(regionName);
    }

    private Subregions getSubregionBySubregionName(String subregionName) {
        return subregionService.getSubregionBySubregionName(subregionName);
    }

    public void confirmEditButtonAction() {
        editCountry();
    }

    private void editCountry() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedOrganization.getId() : " + selectedCountry.getId());
        
        name = selectedCountry.getName();
        iso3 = selectedCountry.getIso3();
        numericCode = selectedCountry.getNumericCode();
        iso2 = selectedCountry.getIso2();
        phonecode = selectedCountry.getPhonecode();
        capital = selectedCountry.getCapital();
        currency = selectedCountry.getCurrency();
        currencyName = selectedCountry.getCurrencyName();
        currencySymbol = selectedCountry.getCurrencySymbol();
        tld = selectedCountry.getTld();
        nativeName = selectedCountry.getNativeName();
        region = selectedCountry.getRegionEntity().getName();
        subregion = selectedCountry.getSubregionEntity().getName();
        nationality = selectedCountry.getNationality();
        timezones = selectedCountry.getTimezones();
        translations = selectedCountry.getTranslations();

    }

    public List<String> completeRegion(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> regionList = new ArrayList<>();
        List<Regions> regions = regionService.getRegionsList();
        for (Regions region : regions) {
            regionList.add(region.getName());
        }
        return regionList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public List<String> completeSubregion(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> subregionList = new ArrayList<>();
        List<Subregions> regions = subregionService.getSubRegionsList();
        for (Subregions region : regions) {
            subregionList.add(region.getName());
        }
        return subregionList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public void saveCountry() {

        logger.debug("Inside save country method ");
        logger.debug("isAddOperation : " + isAddOperation);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation - Required fields: name*, iso2*, iso3*, capital*, region*, subregion*
        if (name == null || name.trim().isEmpty()) {
            logger.debug("name is null or empty");
            nameError = true;
            hasErrors = true;
            errorFieldIds.add("form:name");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Country name is required"));
        }

        if (iso2 == null || iso2.trim().isEmpty()) {
            logger.debug("iso2 is null or empty");
            iso2Error = true;
            hasErrors = true;
            errorFieldIds.add("form:iso2");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "ISO2 code is required"));
        }

        if (iso3 == null || iso3.trim().isEmpty()) {
            logger.debug("iso3 is null or empty");
            iso3Error = true;
            hasErrors = true;
            errorFieldIds.add("form:iso3");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "ISO3 code is required"));
        }

        if (capital == null || capital.trim().isEmpty()) {
            logger.debug("capital is null or empty");
            capitalError = true;
            hasErrors = true;
            errorFieldIds.add("form:capital");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Capital is required"));
        }

        if (region == null || region.trim().isEmpty()) {
            logger.debug("region is null or empty");
            regionError = true;
            hasErrors = true;
            errorFieldIds.add("form:regionlist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Region is required"));
        }

        if (subregion == null || subregion.trim().isEmpty()) {
            logger.debug("subregion is null or empty");
            subregionError = true;
            hasErrors = true;
            errorFieldIds.add("form:subregionlist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Subregion is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Countries country = new Countries();
        country.setName(name);
        country.setIso3(iso3);
        country.setNumericCode(numericCode);
        country.setIso2(iso2);
        country.setPhonecode(phonecode);
        country.setCapital(capital);
        country.setCurrency(currency);
        country.setCurrencyName(currencyName);
        country.setCurrencySymbol(currencySymbol);
        country.setTld(tld);
        country.setNativeName(nativeName);
        country.setRegion(region);
        country.setNationality(nationality);
        country.setTimezones(timezones);
        country.setTranslations(translations);

        Regions addRegions = getRegionsByRegionName(region);
        if(addRegions != null)
        {
            country.setRegionEntity(addRegions);
        }
        
        Subregions addSubregions = getSubregionBySubregionName(subregion);
        if(addSubregions != null)
        {
            country.setSubregionEntity(addSubregions);
        }

        UserActivityTO userActivityTO = populateUserActivityTO();

        if (isAddOperation) {

//            logger.debug("if (isAddOperation) {");
//
//            countryService.addCountry(country);
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country Added"));

            logger.debug("if (isAddOperation) {");
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(country.getName()+ " - New Country Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = countryService.addCountry(userActivityTO, country);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("countryAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("countryAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("countryAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }

        } else {

            logger.debug("else  edit operation !!");
            logger.debug("selectedOrganization.getId() : " + selectedCountry.getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Organization "+country.getName()+" Updated");
            country.setId(selectedCountry.getId());
            GeneralConstants updateStatus = countryService.updateCountry(userActivityTO,country);
            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country Updated Successfully"));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("countryAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("countryDoesNotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("countryUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }
        fetchCountryList();
        PrimeFaces.current().executeScript("PF('manageCountDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:countryDataTableId");
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


    public void confirmDeleteCountry() {
        deleteCountry();
    }

    private void deleteCountry() {
        logger.debug("inside delete Country ");
        logger.debug("selectedCountry.getId() : " + selectedCountry.getId());


        name = selectedCountry.getName();


        logger.debug("CountryName : " + name);
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Country "+name+" Deleted");

        GeneralConstants deleteStatus = countryService.deleteCountry(userActivityTO, getSelectedCountry());

        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country Removed Successfully"));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("countryDoesNotExistsLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("countryRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }


        fetchCountryList();
        PrimeFaces.current().ajax().update("form:messages", "form:countryDataTableId");
    }

    private void fetchCountryList() {
        datatableRendered = false;
        logger.debug("inside fetchCountryList ");
        if (CollectionUtils.isNotEmpty(countriesList)) {
            logger.debug("inside fetchCountryList clear");
            countriesList.clear();
        }
        countriesList.addAll(countryService.getCountriesList());

        if (CollectionUtils.isNotEmpty(countriesList)) {
            logger.debug("countriesList.size() : " + countriesList.size());
            datatableRendered = true;
            recordsCount = countriesList.size();
        }
    }

    /**
     * @return the selectedCountry
     */
    public Countries getSelectedCountry() {
        return selectedCountry;
    }

    /**
     * @param selectedCountry the selectedCountry to set
     */
    public void setSelectedCountry(Countries selectedCountry) {
        this.selectedCountry = selectedCountry;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the iso3
     */
    public String getIso3() {
        return iso3;
    }

    /**
     * @param iso3 the iso3 to set
     */
    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    /**
     * @return the numericCode
     */
    public String getNumericCode() {
        return numericCode;
    }

    /**
     * @param numericCode the numericCode to set
     */
    public void setNumericCode(String numericCode) {
        this.numericCode = numericCode;
    }

    /**
     * @return the iso2
     */
    public String getIso2() {
        return iso2;
    }

    /**
     * @param iso2 the iso2 to set
     */
    public void setIso2(String iso2) {
        this.iso2 = iso2;
    }

    /**
     * @return the phonecode
     */
    public String getPhonecode() {
        return phonecode;
    }

    /**
     * @param phonecode the phonecode to set
     */
    public void setPhonecode(String phonecode) {
        this.phonecode = phonecode;
    }

    /**
     * @return the capital
     */
    public String getCapital() {
        return capital;
    }

    /**
     * @param capital the capital to set
     */
    public void setCapital(String capital) {
        this.capital = capital;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
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
     * @return the currencySymbol
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * @param currencySymbol the currencySymbol to set
     */
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    /**
     * @return the tld
     */
    public String getTld() {
        return tld;
    }

    /**
     * @param tld the tld to set
     */
    public void setTld(String tld) {
        this.tld = tld;
    }

    /**
     * @return the nativeName
     */
    public String getNativeName() {
        return nativeName;
    }

    /**
     * @param nativeName the nativeName to set
     */
    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * @param nationality the nationality to set
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     * @return the timezones
     */
    public String getTimezones() {
        return timezones;
    }

    /**
     * @param timezones the timezones to set
     */
    public void setTimezones(String timezones) {
        this.timezones = timezones;
    }

    /**
     * @return the translations
     */
    public String getTranslations() {
        return translations;
    }

    /**
     * @param translations the translations to set
     */
    public void setTranslations(String translations) {
        this.translations = translations;
    }

    /**
     * @return the subregion
     */
    public String getSubregion() {
        return subregion;
    }

    /**
     * @param subregion the subregion to set
     */
    public void setSubregion(String subregion) {
        this.subregion = subregion;
    }

    // Getters for error flags
    public boolean isNameError() {
        return nameError;
    }

    public boolean isIso2Error() {
        return iso2Error;
    }

    public boolean isIso3Error() {
        return iso3Error;
    }

    public boolean isNumericCodeError() {
        return numericCodeError;
    }

    public boolean isPhonecodeError() {
        return phonecodeError;
    }

    public boolean isCapitalError() {
        return capitalError;
    }

    public boolean isCurrencyError() {
        return currencyError;
    }

    public boolean isCurrencyNameError() {
        return currencyNameError;
    }

    public boolean isCurrencySymbolError() {
        return currencySymbolError;
    }

    public boolean isRegionError() {
        return regionError;
    }

    public boolean isSubregionError() {
        return subregionError;
    }

}




