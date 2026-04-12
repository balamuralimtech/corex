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
import com.persist.coretix.modal.systemmanagement.NotificationSettings;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.module.coretix.systemmanagement.INotificationSettingService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.web.coretix.appgeneral.GenericManagedBean;
import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.NotificationService;
import org.primefaces.PrimeFaces;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

/*
 * @author Pragadeesh
 */
@Named("notificationSettingBean")
@Scope("session")
public class NotificationSettingBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 135435341334535432L;
    private static final Logger logger = LoggerFactory.getLogger(NotificationSettingBean.class);
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");

    private List<NotificationSettings> notificationSettingList = new ArrayList<>();
    private boolean isAddOperation;
    private boolean datatableRendered;
    private int recordsCount;
    private NotificationSettings selectedNotificationSetting;
    private ResourceBundle resourceBundle;


    private String organizationName;
    private String emailId;
    private String password;
    private boolean smtpAuth;
    private boolean smtpStarttlsEnable;
    private String smtpHost;
    private String smtpPort;

    private boolean organizationError = false;
    private boolean emailError = false;
    private boolean passwordError = false;
    private boolean smtpHostError = false;
    private boolean smtpPortError = false;

    @Inject
    private transient INotificationSettingService notificationSettingService;

    @Inject
    private transient IOrganizationService organizationService;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        setIsAddOperation(true);
        setDatatableRendered(false);
        setRecordsCount(0);
        resourceBundle = ResourceBundle.getBundle("coreAppMessages",FacesContext.getCurrentInstance().getViewRoot().getLocale());


        resetFields();

        if (CollectionUtils.isNotEmpty(getNotificationSettingsList())) {
            logger.debug("inside  notificationSettingsList clear");
            getNotificationSettingsList().clear();
        }

        PrimeFaces.current().ajax().update("form:notificationSettingMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");

        setOrganizationName("");
        setEmailId("");
        setPassword("");
        setSmtpAuth(false);
        setSmtpStarttlsEnable(false);
        setSmtpHost("");
        setSmtpPort("");

        resetErrorFlags();
    }

    private void resetErrorFlags() {
        organizationError = false;
        emailError = false;
        passwordError = false;
        smtpHostError = false;
        smtpPortError = false;

    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");


            getNotificationSettingsList().clear();
        getNotificationSettingsList().addAll(notificationSettingService.getNotificationSettingsList());

            setDatatableRendered(true);
            setRecordsCount(getNotificationSettingsList().size());


        if(recordsCount == 0)
        {
            resetFields();
            logger.debug("Inside the check of notification setting list ==0");
            logger.debug("notificationSettingList.size() ="+getNotificationSettingsList().size());
            logger.debug("notificationSettingList.size() ="+recordsCount);
        setIsAddOperation(true);
            PrimeFaces.current().executeScript("PF('manageNotifDialog').show()");
        }

       else{
            logger.debug("Inside add action but a row exists already");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Notification Setting Exists",
                            "Notification Settings has been set already. Please edit or delete the existing setting."));
            PrimeFaces.current().ajax().update("form:messages");
        }
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchNotificationSettingList();
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editNotificationSetting();
    }

    private void editNotificationSetting() {
        logger.debug("entered into edit button action !!!");
        setIsAddOperation(false);

        logger.debug("isAddOperation : " + isIsAddOperation());
        logger.debug("selectedNotificationSetting.getId() : " + getSelectedNotificationSetting().getId());
        logger.debug("selectedNotificationSetting..getOrganization().getOrganizationName() : " + getSelectedNotificationSetting().getOrganization().getOrganizationName());
        logger.debug("selectedNotificationSetting.getEmailId() : " + getSelectedNotificationSetting().getEmailId());
        logger.debug("selectedNotificationSetting.isSmtpAuth() : " + getSelectedNotificationSetting().isSmtpAuth());
        logger.debug("selectedNotificationSetting.isSmtpStarttlsEnable() : " + getSelectedNotificationSetting().isSmtpStarttlsEnable());
        logger.debug("selectedNotificationSetting.getSmtpHost() : " + getSelectedNotificationSetting().getSmtpHost());
        logger.debug("selectedNotificationSetting.getSmtpPort() : " + getSelectedNotificationSetting().getSmtpPort());

        setOrganizationName(getSelectedNotificationSetting().getOrganization().getOrganizationName());
        setEmailId(getSelectedNotificationSetting().getEmailId());
        setPassword(getSelectedNotificationSetting().getPassword());
        setSmtpAuth(getSelectedNotificationSetting().isSmtpAuth());
        setSmtpStarttlsEnable(getSelectedNotificationSetting().isSmtpStarttlsEnable());
        setSmtpHost(getSelectedNotificationSetting().getSmtpHost());
        setSmtpPort(getSelectedNotificationSetting().getSmtpPort());

    }

    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizationList = new ArrayList<>();
        List<Organizations> organizations = organizationService.getOrganizationsList();
        for (Organizations organization : organizations) {
            organizationList.add(organization.getOrganizationName());
        }
        return organizationList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    private Organizations getOrganizationsByOrganizationName(String organizationName) {
        return organizationService.getOrganizationsEntityByOrganizationName(organizationName);
    }

    public void saveNotificationSetting() {

        logger.debug("Inside save notification setting  method ");
        logger.debug("isAddOperation : " + isIsAddOperation());

        NotificationSettings notificationSetting = new NotificationSettings();
        
        logger.debug("getUserId : " + getEmailId());
        logger.debug("getPassword : " + getPassword());
        logger.debug("isSmtpAuth : " + isSmtpAuth());
        logger.debug("getSmtpHost : " + getSmtpHost());
        logger.debug("smtpPort : " + getSmtpPort());
        logger.debug("isSmtpStarttlsEnable : " + isSmtpStarttlsEnable());
        logger.debug("getOrganizationName() : " + getOrganizationName());

        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        if (organizationName == null || organizationName.trim().isEmpty()) {
            logger.debug("organizationName is null or empty");
            organizationError = true;
            hasErrors = true;
            errorFieldIds.add("form:orglist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Organization is required"));
        }

        if (emailId == null || emailId.trim().isEmpty()) {
            logger.debug("emailId is null or empty");
            emailError = true;
            hasErrors = true;
            errorFieldIds.add("form:UserId");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Email ID is required"));
        }

        if (password == null || password.trim().isEmpty()) {
            logger.debug("password is null or empty");
            passwordError = true;
            hasErrors = true;
            errorFieldIds.add("form:Password");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Password is required"));
        }

        if (smtpHost == null || smtpHost.trim().isEmpty()) {
            logger.debug("smtpHost is null or empty");
            smtpHostError = true;
            hasErrors = true;
            errorFieldIds.add("form:smtpHost");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "SMTP host is required"));
        }

        if (smtpPort == null || smtpPort.trim().isEmpty()) {
            logger.debug("smtpPort is null or empty");
            smtpPortError = true;
            hasErrors = true;
            errorFieldIds.add("form:smtpPort");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "SMTP port is required"));
        }

        Organizations addOrganizations = null;
        if (!hasErrors) {
            addOrganizations = getOrganizationsByOrganizationName(getOrganizationName());
            if (addOrganizations == null) {
                logger.debug("organization not found for name : " + getOrganizationName());
                organizationError = true;
                hasErrors = true;
                errorFieldIds.add("form:orglist");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        resourceBundle.getString("errorLabel"),
                        "Select a valid organization"));
            }
        }

        if (hasErrors) {
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        notificationSetting.setEmailId(getEmailId());
        notificationSetting.setPassword(getPassword());
        notificationSetting.setSmtpAuth(isSmtpAuth());
        notificationSetting.setSmtpHost(getSmtpHost());
        notificationSetting.setSmtpPort(getSmtpPort());
        notificationSetting.setSmtpStarttlsEnable(isSmtpStarttlsEnable());

        logger.debug("Organization name " + addOrganizations.getOrganizationName());
        if (addOrganizations != null) {
            notificationSetting.setOrganization(addOrganizations);
        }
        notificationSetting.setEmailId(emailId);
        notificationSetting.setPassword(getPassword());
        notificationSetting.setSmtpAuth(isSmtpAuth());
        notificationSetting.setSmtpHost(getSmtpHost());
        notificationSetting.setSmtpPort(getSmtpPort());
        notificationSetting.setSmtpStarttlsEnable(isSmtpStarttlsEnable());

        UserActivityTO userActivityTO = populateUserActivityTO();
        Integer organizationId = resolveCurrentOrganizationId();

        if (isIsAddOperation()) {
            logger.debug("if (isAddOperation) {");
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(notificationSetting.getEmailId()+ " - New NotificationSetting Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = notificationSettingService.addNotificationSetting(userActivityTO, notificationSetting);
            switch (addStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildNotificationSettingChangeNotification(notificationSetting.getOrganization().getOrganizationName(), "added"));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("NotificationSetting Added Successfully"));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "NotificationSetting already exists"));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"), "NotificationSetting Add Failed"));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("NotificationSetting.getUserId() : " + getSelectedNotificationSetting().getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing NotificationSetting "+notificationSetting.getEmailId()+" Updated");
            notificationSetting.setId(selectedNotificationSetting.getId());
            GeneralConstants updateStatus = notificationSettingService.updateNotificationSetting(userActivityTO, notificationSetting);
            switch (updateStatus) {
                case SUCCESSFUL:
                    notifyActiveOrganizationUsers(organizationId,
                            buildNotificationSettingChangeNotification(notificationSetting.getOrganization().getOrganizationName(), "edited"));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("NotificationSetting Updated Successfully"));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "NotificationSetting already exists"));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "NotificationSetting does not exists"));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"), "Organization Update Failed"));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }

        fetchNotificationSettingList();
        PrimeFaces.current().executeScript("PF('manageNotifDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:notifDataTableId");
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
    public void confirmDeleteNotificationSetting() {
        deleteNotificationSetting();
    }

    private void deleteNotificationSetting() {
        logger.debug("inside delete NotificationSetting ");
        logger.debug("getSelectedNotificationSetting.getUserId(): " + getSelectedNotificationSetting().getEmailId());


        emailId = getSelectedNotificationSetting().getEmailId();
        password = getSelectedNotificationSetting().getPassword();
        smtpAuth = getSelectedNotificationSetting().isSmtpAuth();
        smtpHost = getSelectedNotificationSetting().getSmtpHost();
        smtpPort = getSelectedNotificationSetting().getSmtpPort();
        smtpStarttlsEnable = getSelectedNotificationSetting().isSmtpStarttlsEnable();
        organizationName = getSelectedNotificationSetting().getOrganization().getOrganizationName();
        Integer organizationId = resolveCurrentOrganizationId();

        logger.debug("EmailId:"+emailId);
        logger.debug("Password:"+password);
        logger.debug("smtpAuth:"+smtpAuth);
        logger.debug("smtpHost:"+smtpHost);
        logger.debug("smtpPort:"+smtpPort);
        logger.debug("smtpStarttlsEnable:"+smtpStarttlsEnable);
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" NotificationSetting "+emailId+" Deleted");

        GeneralConstants deleteStatus = notificationSettingService.deleteNotificationSetting(userActivityTO, getSelectedNotificationSetting());
        switch (deleteStatus) {
            case SUCCESSFUL:
                notifyActiveOrganizationUsers(organizationId,
                        buildNotificationSettingChangeNotification(organizationName, "deleted"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Organization Removed Successfully"));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Organization does not exist"));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"), "Organization Removal Failed"));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"),resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }

        fetchNotificationSettingList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("NotificationSetting Removed"));
        PrimeFaces.current().ajax().update("form:messages", "form:notifDataTableId");
    }

    private void fetchNotificationSettingList() {
        setDatatableRendered(false);
        logger.debug("inside fetchNotificationSettingList ");
        if (CollectionUtils.isNotEmpty(getNotificationSettingsList())) {
            logger.debug("inside fetchNotificationSettingList clear");
            getNotificationSettingsList().clear();
        }
        getNotificationSettingsList().addAll(notificationSettingService.getNotificationSettingsList());

        if (CollectionUtils.isNotEmpty(getNotificationSettingsList())) {
            logger.debug("notificationSettings.size() : " + getNotificationSettingsList().size());
            setDatatableRendered(true);
            setRecordsCount(getNotificationSettingsList().size());
        }
        else {
            setRecordsCount(0);
            logger.debug("List empty");

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,resourceBundle.getString("errorLabel"), "Notification settings list is empty"));
        }
    }

    public List<NotificationSettings> getNotificationSettingsList() {
        return notificationSettingList;
    }

    public void setNotificationSettingList(List<NotificationSettings> notificationSettingList) {
        this.notificationSettingList = notificationSettingList;
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

    public NotificationSettings getSelectedNotificationSetting() {
        return selectedNotificationSetting;
    }

    public void setSelectedNotificationSetting(NotificationSettings selectedNotificationSetting) {
        this.selectedNotificationSetting = selectedNotificationSetting;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public boolean isSmtpStarttlsEnable() {
        return smtpStarttlsEnable;
    }

    public void setSmtpStarttlsEnable(boolean smtpStarttlsEnable) {
        this.smtpStarttlsEnable = smtpStarttlsEnable;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public boolean isOrganizationError() {
        return organizationError;
    }

    public boolean isEmailError() {
        return emailError;
    }

    public boolean isPasswordError() {
        return passwordError;
    }

    public boolean isSmtpHostError() {
        return smtpHostError;
    }

    public boolean isSmtpPortError() {
        return smtpPortError;
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

    private String buildNotificationSettingChangeNotification(String changedOrganizationName, String action) {
        String actorUserName = resolveCurrentUserName();
        String formattedDateTime = LocalDateTime.now().format(NOTIFICATION_DATE_TIME_FORMATTER);
        return "Notification setting for organization '" + changedOrganizationName + "' was " + action + " by " + actorUserName + " on " + formattedDateTime + ".";
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



