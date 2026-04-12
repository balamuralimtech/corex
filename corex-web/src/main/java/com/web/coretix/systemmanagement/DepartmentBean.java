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
import com.persist.coretix.modal.systemmanagement.Departments;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.web.coretix.appgeneral.GenericManagedBean;
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
import com.module.coretix.systemmanagement.IDepartmentService;

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
@Named("departmentBean")
@Scope("session")
public class DepartmentBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 131435345345354355L;
    private static final Logger logger = LoggerFactory.getLogger(DepartmentBean.class);
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
    private List<Departments> departmentList = new ArrayList<>();

    private ResourceBundle resourceBundle;

    private String departmentName;
    private String departmentOrganizationName;
    private String departmentPhoneNumber;
    private String departmentEmail;
    
    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    // Field validation flags
    private boolean departmentNameError = false;
    private boolean organizationError = false;
    private boolean phoneError = false;
    private boolean emailError = false;

    private Departments selectedDepartment = new Departments();

    @Inject
    private transient IOrganizationService organizationService;
    
    @Inject
    private transient IDepartmentService departmentService;
    


    public void initializePageAttributes()
    {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("coreAppMessages",FacesContext.getCurrentInstance().getViewRoot().getLocale());


        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        
        if(CollectionUtils.isNotEmpty(departmentList))
        {
            logger.debug("inside Departments list clear");
            departmentList.clear();
        }
        
        PrimeFaces.current().ajax().update("form:departmentMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }
    
    private void resetFields()
    {
        logger.debug("entered into resetFields action !!!");
        setDepartmentOrganizationName("");
        departmentName = "";
        departmentPhoneNumber = "";
        departmentEmail = "";

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        departmentNameError = false;
        organizationError = false;
        phoneError = false;
        emailError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }
    
    public void confirmEditButtonAction()
    {
        editDepartment();
    }
    
    private void editDepartment()
    {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;
        
        logger.debug("isAddOperation : "+isAddOperation);
        logger.debug("selectedDepartments.getId() : "+selectedDepartment.getId());
        departmentName = selectedDepartment.getDepartmentName();
        departmentOrganizationName = selectedDepartment.getOrganization().getOrganizationName();
        departmentPhoneNumber = selectedDepartment.getPhoneNumber();
        departmentEmail = selectedDepartment.getEmail();
        logger.debug("departmentName : " + departmentName);
        logger.debug("departmentOrganizationName : " + getDepartmentOrganizationName());
        logger.debug("organizationPhoneNumber : " + departmentPhoneNumber);
        logger.debug("organizationWebsite : " + departmentEmail);
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
    

    public void saveDepartment() {

        logger.debug("Inside save department method ");
        logger.debug("isAddOperation : "+isAddOperation);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (departmentName == null || departmentName.trim().isEmpty()) {
            logger.debug("departmentName is null or empty");
            departmentNameError = true;
            hasErrors = true;
            errorFieldIds.add("form:departmentname");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Department name is required"));
        }

        if (departmentOrganizationName == null || departmentOrganizationName.trim().isEmpty()) {
            logger.debug("departmentOrganizationName is null or empty");
            organizationError = true;
            hasErrors = true;
            errorFieldIds.add("form:organizationlist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Organization is required"));
        }

        if (departmentPhoneNumber == null || departmentPhoneNumber.trim().isEmpty()) {
            logger.debug("departmentPhoneNumber is null or empty");
            phoneError = true;
            hasErrors = true;
            errorFieldIds.add("form:phonenumber");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Phone number is required"));
        }

        if (departmentEmail == null || departmentEmail.trim().isEmpty()) {
            logger.debug("departmentEmail is null or empty");
            emailError = true;
            hasErrors = true;
            errorFieldIds.add("form:email");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Email is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Departments departments = new Departments();
        Integer organizationId = resolveCurrentOrganizationId();

        departments.setDepartmentName(departmentName);
        
        Organizations addOrganization = getOrganizationsByOrganizationName(getDepartmentOrganizationName());
        logger.debug("country name " + addOrganization.getOrganizationName());
        if (addOrganization != null) {
            departments.setOrganization(addOrganization);
        }
        departments.setPhoneNumber(departmentPhoneNumber);
        departments.setEmail(departmentEmail);
        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isAddOperation) {
            
            logger.debug("if (isAddOperation) {");
            
           userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(departments.getDepartmentName()+ " - New Department Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = departmentService.addDepartment(userActivityTO,departments);
            logger.debug("addStatus : "+addStatus);
            switch (addStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildDepartmentChangeNotification(departments.getDepartmentName(), "added"));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("departmentAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("departmentAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("departmentAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }

        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedDepartments.getId() : "+selectedDepartment.getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Department "+departments.getDepartmentName()+" Updated");
            departments.setId(selectedDepartment.getId());
            GeneralConstants updateStatus = departmentService.updateDepartment(userActivityTO, departments);
            switch (updateStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildDepartmentChangeNotification(departments.getDepartmentName(), "edited"));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("departmentUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("departmentAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("departmentDoesNotExistLabel")));
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("departmentUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }
        fetchDepartmentsList();
        PrimeFaces.current().executeScript("PF('manageDeptDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:deptDataTableId");
    }

    public void confirmDeleteDepartment() {
        deleteDepartment();
    }
    
    private void deleteDepartment()
    {
        logger.debug("inside delete organization ");
        logger.debug("selectedDepartments.getId() : "+selectedDepartment.getId());
        departmentName = selectedDepartment.getDepartmentName();
        setDepartmentOrganizationName(selectedDepartment.getOrganization().getOrganizationName());
        departmentPhoneNumber = selectedDepartment.getPhoneNumber();
        departmentEmail = selectedDepartment.getEmail();
        
        logger.debug("departmentName : "+departmentName);
        logger.debug("departmentOrganizationName : "+getDepartmentOrganizationName());
        logger.debug("organizationPhoneNumber : "+departmentPhoneNumber);
        logger.debug("organizationWebsite : "+departmentEmail);
        Integer organizationId = resolveCurrentOrganizationId();

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Department "+departmentName+" Deleted");

        GeneralConstants deleteStatus = departmentService.deleteDepartment(userActivityTO, getSelectedDepartment());
        switch (deleteStatus) {
            case SUCCESSFUL:
                notifyActiveOrganizationUsers(organizationId,
                        buildDepartmentChangeNotification(departmentName, "deleted"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("departmentRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("departmentDoesNotExistLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("departmentRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }
        fetchDepartmentsList();
        PrimeFaces.current().ajax().update("form:messages", "form:deptDataTableId");
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




    public void searchButtonAction()
    {
        fetchDepartmentsList();
    }

    private void fetchDepartmentsList()
    {
        logger.debug("inside Departments List ");
        if(CollectionUtils.isNotEmpty(departmentList))
        {
            logger.debug("inside Departments list clear");
            departmentList.clear();
        }
        departmentList.addAll(departmentService.getDepartmentsList());
        

        if (CollectionUtils.isNotEmpty(departmentList)) {
            logger.debug("organizationList.size() : " + departmentList.size());
            datatableRendered = true;
            recordsCount = departmentList.size();
        }
    }
    
    
    
    private Organizations getOrganizationsByOrganizationName(String organziationName)
    {
        return organizationService.getOrganizationsEntityByOrganizationName(organziationName);
    }
    

    /**
     * @return the departmentName
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * @param departmentName the departmentName to set
     */
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    /**
     * @return the departmentPhoneNumber
     */
    public String getDepartmentPhoneNumber() {
        return departmentPhoneNumber;
    }

    /**
     * @param departmentPhoneNumber the departmentPhoneNumber to set
     */
    public void setDepartmentPhoneNumber(String departmentPhoneNumber) {
        this.departmentPhoneNumber = departmentPhoneNumber;
    }

    /**
     * @return the departmentEmail
     */
    public String getDepartmentEmail() {
        return departmentEmail;
    }

    /**
     * @param departmentEmail the departmentEmail to set
     */
    public void setDepartmentEmail(String departmentEmail) {
        this.departmentEmail = departmentEmail;
    }


    /**
     * @return the selectedDepartment
     */
    public Departments getSelectedDepartment() {
        return selectedDepartment;
    }

    /**
     * @param selectedDepartment the selectedDepartment to set
     */
    public void setSelectedDepartment(Departments selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }

    /**
     * @return the departmentList
     */
    public List<Departments> getDepartmentList() {
        return departmentList;
    }

    /**
     * @param departmentList the departmentList to set
     */
    public void setDepartmentList(List<Departments> departmentList) {
        this.departmentList = departmentList;
    }

    /**
     * @return the departmentOrganizationName
     */
    public String getDepartmentOrganizationName() {
        return departmentOrganizationName;
    }

    /**
     * @param departmentOrganizationName the departmentOrganizationName to set
     */
    public void setDepartmentOrganizationName(String departmentOrganizationName) {
        this.departmentOrganizationName = departmentOrganizationName;
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
    public boolean isDepartmentNameError() {
        return departmentNameError;
    }

    public boolean isOrganizationError() {
        return organizationError;
    }

    public boolean isPhoneError() {
        return phoneError;
    }

    public boolean isEmailError() {
        return emailError;
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

    private String buildDepartmentChangeNotification(String changedDepartmentName, String action) {
        String actorUserName = resolveCurrentUserName();
        String formattedDateTime = LocalDateTime.now().format(NOTIFICATION_DATE_TIME_FORMATTER);
        return "Department '" + changedDepartmentName + "' was " + action + " by " + actorUserName + " on " + formattedDateTime + ".";
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



