package com.web.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("applicationNotificationBean")
@Scope("session")
public class ApplicationNotificationBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNotificationBean.class);
    private static final long serialVersionUID = 8801471157164048123L;

    private List<ApplicationNotification> notificationList = new ArrayList<>();
    private ApplicationNotification selectedNotification = new ApplicationNotification();
    private String message;
    private boolean addOperation = true;
    private boolean datatableRendered;
    private int recordsCount;
    private boolean messageError;

    @Inject
    private IApplicationNotificationService applicationNotificationService;

    public void initializePageAttributes() {
        addOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        resetFields();
        if (CollectionUtils.isNotEmpty(notificationList)) {
            notificationList.clear();
        }
        PrimeFaces.current().ajax().update("form:notificationMainPanelId");
    }

    public void addButtonAction() {
        addOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        fetchNotificationList();
    }

    public void confirmEditButtonAction() {
        if (selectedNotification == null) {
            return;
        }

        addOperation = false;
        ApplicationNotification persistentNotification =
                applicationNotificationService.getApplicationNotificationById(selectedNotification.getId());
        if (persistentNotification == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Notification not found"));
            return;
        }

        selectedNotification = persistentNotification;
        message = persistentNotification.getMessage();
        resetErrorFlags();
    }

    public void saveNotification() {
        String trimmedMessage = message == null ? "" : message.trim();
        resetErrorFlags();

        if (trimmedMessage.isEmpty()) {
            messageError = true;
            PrimeFaces.current().executeScript("highlightErrorFields(['form:notificationMessage']);");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Message is required"));
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;

        if (addOperation) {
            ApplicationNotification notification = new ApplicationNotification();
            notification.setMessage(trimmedMessage);
            notification.setCreatedByUserId(userActivityTO.getUserId());
            notification.setCreatedByUserName(userActivityTO.getUserName());
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription("Application notification created");
            userActivityTO.setCreatedAt(new Date());

            result = applicationNotificationService.addApplicationNotification(userActivityTO, notification);
        } else {
            selectedNotification.setMessage(trimmedMessage);
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Application notification updated");
            userActivityTO.setCreatedAt(new Date());
            result = applicationNotificationService.updateApplicationNotification(userActivityTO, selectedNotification);
        }

        handleSaveResult(result, addOperation ? "created" : "updated");
    }

    public void confirmDeleteNotification() {
        if (selectedNotification == null) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription("Application notification deleted");
        userActivityTO.setCreatedAt(new Date());

        GeneralConstants result =
                applicationNotificationService.deleteApplicationNotification(userActivityTO, selectedNotification);

        switch (result) {
            case SUCCESSFUL:
                fetchNotificationList();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Notification deleted successfully"));
                PrimeFaces.current().ajax().update("form:messages", "form:notificationDataTableId", ":topbar-items-form");
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Notification does not exist"));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete notification"));
                break;
        }
    }

    private void handleSaveResult(GeneralConstants result, String actionLabel) {
        switch (result) {
            case SUCCESSFUL:
                resetFields();
                fetchNotificationList();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                "Notification " + actionLabel + " successfully"));
                PrimeFaces.current().executeScript("PF('manageApplicationNotificationDialog').hide()");
                PrimeFaces.current().ajax().update("form:messages", "form:notificationDataTableId", ":topbar-items-form");
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Notification does not exist"));
                break;
            default:
                logger.error("Unable to save application notification. Result={}", result);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to save notification"));
                break;
        }
    }

    private void fetchNotificationList() {
        datatableRendered = false;
        notificationList = new ArrayList<>(applicationNotificationService.getRecentNotifications(50));
        recordsCount = notificationList.size();
        datatableRendered = !notificationList.isEmpty();
    }

    private void resetFields() {
        selectedNotification = new ApplicationNotification();
        message = "";
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        messageError = false;
    }

    private UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

        if (httpSession != null) {
            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }

    public List<ApplicationNotification> getNotificationList() {
        return notificationList;
    }

    public void setNotificationList(List<ApplicationNotification> notificationList) {
        this.notificationList = notificationList;
    }

    public ApplicationNotification getSelectedNotification() {
        return selectedNotification;
    }

    public void setSelectedNotification(ApplicationNotification selectedNotification) {
        this.selectedNotification = selectedNotification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAddOperation() {
        return addOperation;
    }

    public void setAddOperation(boolean addOperation) {
        this.addOperation = addOperation;
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

    public boolean isMessageError() {
        return messageError;
    }
}
