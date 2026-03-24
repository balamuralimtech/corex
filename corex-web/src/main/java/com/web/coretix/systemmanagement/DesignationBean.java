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

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Designations;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.NotificationService;
import org.springframework.context.annotation.Scope;
import com.module.coretix.systemmanagement.IDesignationService;

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
@Named("designationBean")
@Scope("session")
public class DesignationBean implements Serializable {

    private static final long serialVersionUID = 135415345345354355L;
    private static final Logger logger = LoggerFactory.getLogger(DesignationBean.class);
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
    private List<Designations> designationList = new ArrayList<>();

    private ResourceBundle resourceBundle;

    private String designationName;
    private String designationOrganizationName;

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    // Field validation flags
    private boolean designationNameError = false;
    private boolean organizationError = false;

    private Designations selectedDesignation = new Designations();

    @Inject
    private IOrganizationService organizationService;
    
    @Inject
    private IDesignationService designationService;
    

    public void initializePageAttributes()
    {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());

        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        if (CollectionUtils.isNotEmpty(designationList)) {
            logger.debug("inside  designationList clear");
            designationList.clear();
        }
        
        PrimeFaces.current().ajax().update("form:designationMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }
    
    private void resetFields()
    {
        logger.debug("entered into resetFields action !!!");
        setDesignationOrganizationName("");
        designationName = "";

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        designationNameError = false;
        organizationError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }
    
    public void confirmEditButtonAction()
    {
        editDesignation();
    }
    
    private void editDesignation()
    {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;
        
        logger.debug("isAddOperation : "+isAddOperation);
        logger.debug("selectedDesignations.getId() : "+getSelectedDesignation().getId());
        
        designationName = selectedDesignation.getDesignationName();
        designationOrganizationName = selectedDesignation.getOrganization().getOrganizationName();
        logger.debug("designationName : "+designationName);
        logger.debug("designationOrganizationName : "+designationOrganizationName);
    }
    
    
    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizationList = new ArrayList<>();
        List<Organizations> countries = organizationService.getOrganizationsList();
        for (Organizations Organization : countries) {
            organizationList.add(Organization.getOrganizationName());
        }

        return organizationList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    

    public void saveDesignation() {

        logger.debug("Inside save designation method ");
        logger.debug("isAddOperation : "+isAddOperation);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (designationName == null || designationName.trim().isEmpty()) {
            logger.debug("designationName is null or empty");
            designationNameError = true;
            hasErrors = true;
            errorFieldIds.add("form:designationname");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Designation name is required"));
        }

        if (designationOrganizationName == null || designationOrganizationName.trim().isEmpty()) {
            logger.debug("designationOrganizationName is null or empty");
            organizationError = true;
            hasErrors = true;
            errorFieldIds.add("form:organizationlist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Organization is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Designations designation = new Designations();
        Integer organizationId = resolveCurrentOrganizationId();

        designation.setDesignationName(designationName);


        Organizations addOrganization = getOrganizationsByOrganizationName(getDesignationOrganizationName());
        logger.debug("country name " + addOrganization.getOrganizationName());
        if (addOrganization != null) {
            designation.setOrganization(addOrganization);
        }

        UserActivityTO userActivityTO = populateUserActivityTO();

        if (isAddOperation) {
            
            logger.debug("if (isAddOperation) {");

            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(designation.getDesignationName()+ " - New Designation Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = designationService.addDesignation(userActivityTO,designation);
            logger.debug("addStatus : "+addStatus);
            switch (addStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildDesignationChangeNotification(designation.getDesignationName(), "added"));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("designationAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("designationAlreadyExistLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("designationAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }

        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedDesignations.getId() : "+selectedDesignation.getId());
            designation.setId(getSelectedDesignation().getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Designation "+designation.getDesignationName()+" Updated");
            GeneralConstants updateStatus = designationService.updateDesignation(userActivityTO,designation);


            switch (updateStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildDesignationChangeNotification(designation.getDesignationName(), "edited"));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("designationUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("designationAlreadyExistLabel")));
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("designationUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }
        fetchDesignationsList();
        PrimeFaces.current().executeScript("PF('manageDesigDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:desigDataTableId");
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

    public void confirmDeleteDesignation() {
        deleteDesignation();
    }
    
    private void deleteDesignation()
    {
        logger.debug("inside delete designation ");
        logger.debug("selectedDesignations.getId() : "+getSelectedDesignation().getId());

        designationName = getSelectedDesignation().getDesignationName();
        setDesignationOrganizationName(getSelectedDesignation().getOrganization().getOrganizationName());
        Integer organizationId = resolveCurrentOrganizationId();
        
        logger.debug("departmentName : "+designationName);
        logger.debug("departmentOrganizationName : "+getDesignationOrganizationName());
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Designation "+designationName+" Deleted");

        GeneralConstants deleteStatus = designationService.deleteDesignation(userActivityTO, getSelectedDesignation());
        switch (deleteStatus) {
            case SUCCESSFUL:
                notifyActiveOrganizationUsers(organizationId,
                        buildDesignationChangeNotification(designationName, "deleted"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("designationRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("designationDoesNotExistLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("designationRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }
        fetchDesignationsList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Designation Removed"));
        PrimeFaces.current().ajax().update("form:messages", "form:desigDataTableId");
    }
    
    public void searchButtonAction()
    {
        fetchDesignationsList();
    }

    private void fetchDesignationsList()
    {
        logger.debug("inside Designations List ");
        if(CollectionUtils.isNotEmpty(designationList))
        {
            logger.debug("inside Designations list clear");
            designationList.clear();
        }
        designationList.addAll(designationService.getDesignationsList());
        
        if (CollectionUtils.isNotEmpty(designationList)) {
            logger.debug("organizationList.size() : " + designationList.size());
            datatableRendered = true;
            recordsCount = designationList.size();
        }
    }
    
    
    
    private Organizations getOrganizationsByOrganizationName(String organziationName)
    {
        return organizationService.getOrganizationsEntityByOrganizationName(organziationName);
    }
    

    /**
     * @return the designationName
     */
    public String getDesignationName() {
        return designationName;
    }

    /**
     * @param designationName the designationName to set
     */
    public void setDesignationName(String designationName) {
        this.designationName = designationName;
    }


    /**
     * @return the designationList
     */
    public List<Designations> getDesignationList() {
        return designationList;
    }

    /**
     * @param designationList the designationList to set
     */
    public void setDesignationList(List<Designations> designationList) {
        this.designationList = designationList;
    }

    /**
     * @return the designationOrganizationName
     */
    public String getDesignationOrganizationName() {
        return designationOrganizationName;
    }

    /**
     * @param designationOrganizationName the designationOrganizationName to set
     */
    public void setDesignationOrganizationName(String designationOrganizationName) {
        this.designationOrganizationName = designationOrganizationName;
    }

    /**
     * @return the selectedDesignation
     */
    public Designations getSelectedDesignation() {
        return selectedDesignation;
    }

    /**
     * @param selectedDesignation the selectedDesignation to set
     */
    public void setSelectedDesignation(Designations selectedDesignation) {
        this.selectedDesignation = selectedDesignation;
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

    // Getters for error flags
    public boolean isDesignationNameError() {
        return designationNameError;
    }

    public boolean isOrganizationError() {
        return organizationError;
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

    private String buildDesignationChangeNotification(String changedDesignationName, String action) {
        String actorUserName = resolveCurrentUserName();
        String formattedDateTime = LocalDateTime.now().format(NOTIFICATION_DATE_TIME_FORMATTER);
        return "Designation '" + changedDesignationName + "' was " + action + " by " + actorUserName + " on " + formattedDateTime + ".";
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




