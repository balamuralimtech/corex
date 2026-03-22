/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.web.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Regions;
import com.persist.coretix.modal.systemmanagement.Subregions;
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
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author admin
 */
@Named("subregionBean")
@Scope("session")
public class SubRegionBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private final Logger logger = Logger.getLogger(getClass());
    private List<Subregions> subregionList = new ArrayList<>();

    private String regionName;
    private String subregionName;
    private ResourceBundle resourceBundle;

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    private Subregions selectedSubRegion = new Subregions();

    // Field validation flags
    private boolean nameError = false;
    private boolean regionError = false;

    @Inject
    private IRegionService regionService;

    @Inject
    private ISubRegionService subregionService;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        regionName = "";
        subregionName = "";

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());


        if (CollectionUtils.isNotEmpty(getSubregionList())) {
            logger.debug("inside  organizationList clear");
            getSubregionList().clear();
        }

        PrimeFaces.current().ajax().update("form:subregionMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");
        subregionName = "";
        regionName = "";

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        nameError = false;
        regionError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchSubRegionList();
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editOrganization();
    }

    private void editOrganization() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedOrganization.getId() : " + getSelectedSubRegion().getId());

        subregionName=selectedSubRegion.getName();
        logger.debug("subRegionName : " + subregionName);

    }

    private Regions getRegionsByRegionName(String regionName) {
        return regionService.getRegionByRegionName(regionName);
    }

    public void saveSubregion() {

        logger.debug("Inside saveRegion method ");
        logger.debug("isAddOperation : " + isAddOperation);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (subregionName == null || subregionName.trim().isEmpty()) {
            logger.debug("subregionName is null or empty");
            nameError = true;
            hasErrors = true;
            errorFieldIds.add("form:name");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "SubRegion name is required"));
        }

        if (regionName == null || regionName.trim().isEmpty()) {
            logger.debug("regionName is null or empty");
            regionError = true;
            hasErrors = true;
            errorFieldIds.add("form:regionlist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Region is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Subregions subregion = new Subregions();

        subregion.setName(subregionName);

        Regions addRegions = getRegionsByRegionName(regionName);
        logger.debug("addRegions name " + addRegions.getName());
        if (addRegions != null) {
            subregion.setRegion(addRegions);
        }

        UserActivityTO userActivityTO = populateUserActivityTO();

        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");

            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(subregion.getName()+ " - New SubRegion Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = subregionService.addSubRegion(userActivityTO, subregion);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("subRegionAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("subRegionAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("subRegionAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedOrganization.getId() : " + getSelectedSubRegion().getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing SubRegion "+subregion.getName()+" Updated");
            subregion.setId(selectedSubRegion.getId());
            GeneralConstants updateStatus = subregionService.updateSubRegion(userActivityTO, subregion);
            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("subRegionUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("subRegionAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("subregionDoesnotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"), resourceBundle.getString("subregionUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }

        fetchSubRegionList();
        PrimeFaces.current().executeScript("PF('manageSubrDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:subrDataTableId");
    }
    public UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

        if (httpSession != null) {
            logger.debug("httpSession.getId() : " + httpSession.getId());
            logger.debug("#############################################################################");
            logger.debug((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            logger.debug((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            logger.debug((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            logger.debug((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
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

    public void confirmDeleteSubregion() {
        deleteSubregion();
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

    private void deleteSubregion() {
        logger.debug("inside delete organization ");
        logger.debug("selectedOrganization.getId() : " + getSelectedSubRegion().getId());

        subregionName = selectedSubRegion.getName();

        logger.debug("subregionName : " + subregionName);

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" SubRegion "+subregionName+" Deleted");

        GeneralConstants deleteStatus = subregionService.deleteSubRegion(userActivityTO, getSelectedSubRegion());
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("subRegionRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),resourceBundle.getString("subregionDoesnotExistsLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("subRegionRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }

        fetchSubRegionList();
        PrimeFaces.current().ajax().update("form:messages", "form:subrDataTableId");
    }

    private void fetchSubRegionList() {
        datatableRendered = false;
        logger.debug("inside fetchRegionList ");
        if (CollectionUtils.isNotEmpty(getSubregionList())) {
            logger.debug("inside fetchRegionList clear");
            getSubregionList().clear();
        }
        getSubregionList().addAll(subregionService.getSubRegionsList());

        if (CollectionUtils.isNotEmpty(getSubregionList())) {
            logger.debug("fetchRegionList.size() : " + getSubregionList().size());
            datatableRendered = true;
            recordsCount = getSubregionList().size();
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
     * @return the selectedSubRegion
     */
    public Subregions getSelectedSubRegion() {
        return selectedSubRegion;
    }

    /**
     * @param selectedSubRegion the selectedSubRegion to set
     */
    public void setSelectedSubRegion(Subregions selectedSubRegion) {
        this.selectedSubRegion = selectedSubRegion;
    }

    /**
     * @return the subregionName
     */
    public String getSubregionName() {
        return subregionName;
    }

    /**
     * @param subregionName the subregionName to set
     */
    public void setSubregionName(String subregionName) {
        this.subregionName = subregionName;
    }

    /**
     * @return the subregionList
     */
    public List<Subregions> getSubregionList() {
        return subregionList;
    }

    /**
     * @param subregionList the subregionList to set
     */
    public void setSubregionList(List<Subregions> subregionList) {
        this.subregionList = subregionList;
    }

    // Getters for error flags
    public boolean isNameError() {
        return nameError;
    }

    public boolean isRegionError() {
        return regionError;
    }

}
