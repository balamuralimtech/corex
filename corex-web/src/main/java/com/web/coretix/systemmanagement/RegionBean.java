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
import com.persist.coretix.modal.systemmanagement.Regions;
import com.module.coretix.systemmanagement.IRegionService;
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
@Named("regionBean")
@Scope("session")
public class RegionBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(RegionBean.class);
    private List<Regions> regionList = new ArrayList<>();

    private String regionName;
    private ResourceBundle resourceBundle;


    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    // Field validation flags
    private boolean nameError = false;

    private Regions selectedRegion = new Regions();


    @Inject
    private transient IRegionService regionService;


    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        regionName = "";

        resourceBundle = ResourceBundle.getBundle("coreAppMessages",FacesContext.getCurrentInstance().getViewRoot().getLocale());


        if (CollectionUtils.isNotEmpty(getRegionList())) {
            logger.debug("inside  organizationList clear");
            getRegionList().clear();
        }

        PrimeFaces.current().ajax().update("form:regionMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");
        regionName = "";

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        nameError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchRegionList();
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editOrganization();
    }

    private void editOrganization() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedOrganization.getId() : " + getSelectedRegion().getId());

        regionName = selectedRegion.getName();
        logger.debug("regionName : " + regionName);


    }


    public void saveRegion() {

        logger.debug("Inside saveRegion method ");
        logger.debug("isAddOperation : " + isAddOperation);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (regionName == null || regionName.trim().isEmpty()) {
            logger.debug("regionName is null or empty");
            nameError = true;
            hasErrors = true;
            errorFieldIds.add("form:name");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Region name is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Regions region = new Regions();

        region.setName(getRegionName());
        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");

            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(region.getName()+ " - New Region Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = regionService.addRegion(userActivityTO, region);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("regionAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,resourceBundle.getString("warningLabel"),resourceBundle.getString("regionAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("regionAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {

            region.setId(getSelectedRegion().getId());
            logger.debug("else  edit operation !!");
            logger.debug("selectedOrganization.getId() : " + selectedRegion.getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Region "+region.getName()+" Updated");
            region.setId(selectedRegion.getId());
            GeneralConstants updateStatus = regionService.updateRegion(userActivityTO, region);
            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("regionUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("regionAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("regionDoesNotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("regionUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
            }


        }

        fetchRegionList();
        PrimeFaces.current().executeScript("PF('manageRegionDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:regDataTableId");
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


    public void confirmDeleteRegion() {
        deleteRegion();
    }

    private void deleteRegion() {
        logger.debug("inside delete organization ");
        logger.debug("selectedOrganization.getId() : " + selectedRegion.getId());

        regionName = selectedRegion.getName();

        logger.debug("regionName : " + getRegionName());
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Region "+regionName+" Deleted");

        GeneralConstants deleteStatus = regionService.deleteRegion(userActivityTO, getSelectedRegion());
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("regionRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,resourceBundle.getString("warningLabel"),resourceBundle.getString("regionDoesNotExistsLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("regionRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }

        fetchRegionList();
        PrimeFaces.current().ajax().update("form:messages", "form:regDataTableId");
    }

    private void fetchRegionList() {
        datatableRendered = false;
        logger.debug("inside fetchRegionList ");
        if (CollectionUtils.isNotEmpty(getRegionList())) {
            logger.debug("inside fetchRegionList clear");
            getRegionList().clear();
        }
        getRegionList().addAll(regionService.getRegionsList());

        if (CollectionUtils.isNotEmpty(getRegionList())) {
            logger.debug("fetchRegionList.size() : " + getRegionList().size());
            datatableRendered = true;
            recordsCount = getRegionList().size();
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
     * @return the selectedRegion
     */
    public Regions getSelectedRegion() {
        return selectedRegion;
    }

    /**
     * @param selectedRegion the selectedRegion to set
     */
    public void setSelectedRegion(Regions selectedRegion) {
        this.selectedRegion = selectedRegion;
    }

    /**
     * @return the regionName
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * @param regionName the regionName to set
     */
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    /**
     * @return the regionList
     */
    public List<Regions> getRegionList() {
        return regionList;
    }

    /**
     * @param regionList the regionList to set
     */
    public void setRegionList(List<Regions> regionList) {
        this.regionList = regionList;
    }

    // Getters for error flags
    public boolean isNameError() {
        return nameError;
    }

}



