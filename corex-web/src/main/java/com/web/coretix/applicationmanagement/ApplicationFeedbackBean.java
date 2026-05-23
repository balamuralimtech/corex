package com.web.coretix.applicationmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.web.coretix.appgeneral.GenericManagedBean;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Date;

@Named("applicationFeedbackBean")
@Scope("session")
public class ApplicationFeedbackBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 785899861234004411L;
    private static final int FEEDBACK_SUBJECT_MAX_LENGTH = 140;
    private static final int FEEDBACK_MESSAGE_MAX_LENGTH = 700;
    private static final String FEEDBACK_PREFIX = "[FEEDBACK]";

    private String subject;
    private String feedbackMessage;

    @Inject
    private transient IApplicationNotificationService applicationNotificationService;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && !facesContext.isPostback()) {
            resetForm();
        }
    }

    public void sendFeedback() {
        String trimmedSubject = normalize(subject, FEEDBACK_SUBJECT_MAX_LENGTH);
        String trimmedMessage = normalize(feedbackMessage, FEEDBACK_MESSAGE_MAX_LENGTH);

        if (trimmedSubject.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Feedback Required", "Enter a short subject for your feedback.");
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }
        if (trimmedMessage.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Feedback Required", "Enter your feedback before sending it.");
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }

        ApplicationNotification feedback = new ApplicationNotification();
        feedback.setMessage(buildFeedbackNotificationMessage(trimmedSubject, trimmedMessage));
        feedback.setCreatedByUserId(fetchCurrentUserId());
        feedback.setCreatedByUserName(fetchCurrentUsername());

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
        userActivityTO.setActivityDescription("Application feedback submitted");
        userActivityTO.setCreatedAt(new Date());

        GeneralConstants result = applicationNotificationService.addApplicationNotification(userActivityTO, feedback);
        if (result != GeneralConstants.SUCCESSFUL) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Send Failed",
                    "Unable to send your feedback right now. Please try again.");
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }

        resetForm();
        addMessage(FacesMessage.SEVERITY_INFO, "Feedback Sent",
                "Your feedback has been sent to the application admin.");
        PrimeFaces.current().ajax().update("form");
    }

    public void resetForm() {
        subject = "";
        feedbackMessage = "";
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    private UserActivityTO populateUserActivityTO() {
        UserActivityTO userActivityTO = new UserActivityTO();
        HttpSession httpSession = getHttpSession();
        if (httpSession != null) {
            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }
        return userActivityTO;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private String buildFeedbackNotificationMessage(String trimmedSubject, String trimmedMessage) {
        String organizationName = StringUtils.defaultIfEmpty(getCurrentOrganizationName(), "Not Assigned");
        return FEEDBACK_PREFIX
                + " Subject: " + trimmedSubject
                + " | User: " + fetchCurrentUsername()
                + " | Organization: " + organizationName
                + " | Message: " + trimmedMessage;
    }

    private String normalize(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmedValue = value.trim();
        return trimmedValue.length() <= maxLength ? trimmedValue : trimmedValue.substring(0, maxLength);
    }
}
