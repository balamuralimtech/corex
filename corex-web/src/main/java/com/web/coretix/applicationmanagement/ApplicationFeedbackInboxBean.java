package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IApplicationNotificationService;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.web.coretix.appgeneral.GenericManagedBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("applicationFeedbackInboxBean")
@Scope("session")
public class ApplicationFeedbackInboxBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 5058142524277517114L;
    private static final int MAX_FEEDBACK_ROWS = 500;
    private static final String FEEDBACK_PREFIX = "[FEEDBACK]";

    private List<FeedbackInboxRow> feedbackList = new ArrayList<>();

    @Inject
    private transient IApplicationNotificationService applicationNotificationService;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!isApplicationAdmin()) {
            redirectToHome(facesContext);
            return;
        }
        if (facesContext != null && !facesContext.isPostback()) {
            refreshFeedback();
        }
    }

    public void refreshFeedback() {
        List<FeedbackInboxRow> rows = new ArrayList<>();
        for (ApplicationNotification notification : applicationNotificationService.getRecentNotifications(MAX_FEEDBACK_ROWS)) {
            if (notification == null || !StringUtils.startsWith(notification.getMessage(), FEEDBACK_PREFIX)) {
                continue;
            }
            rows.add(new FeedbackInboxRow(
                    notification.getCreatedByUserName(),
                    notification.getCreatedAt(),
                    extractSegment(notification.getMessage(), "Subject: ", " | User:"),
                    extractSegment(notification.getMessage(), "Organization: ", " | Message:"),
                    extractSegment(notification.getMessage(), "Message: ", null)
            ));
        }
        feedbackList = rows;
    }

    public List<FeedbackInboxRow> getFeedbackList() {
        return feedbackList;
    }

    private void redirectToHome(FacesContext facesContext) {
        if (facesContext == null) {
            return;
        }
        try {
            String contextPath = facesContext.getExternalContext().getRequestContextPath();
            facesContext.getExternalContext().redirect(contextPath + "/home");
            facesContext.responseComplete();
        } catch (IOException ignored) {
        }
    }

    private String extractSegment(String message, String startToken, String endToken) {
        if (message == null) {
            return "";
        }
        int startIndex = message.indexOf(startToken);
        if (startIndex < 0) {
            return "";
        }
        startIndex += startToken.length();
        int endIndex = endToken == null ? message.length() : message.indexOf(endToken, startIndex);
        if (endIndex < 0) {
            endIndex = message.length();
        }
        return message.substring(startIndex, endIndex).trim();
    }

    public static class FeedbackInboxRow implements Serializable {
        private final String createdByUserName;
        private final Date createdAt;
        private final String subject;
        private final String organizationName;
        private final String message;

        public FeedbackInboxRow(String createdByUserName, Date createdAt, String subject,
                                String organizationName, String message) {
            this.createdByUserName = createdByUserName;
            this.createdAt = createdAt;
            this.subject = subject;
            this.organizationName = organizationName;
            this.message = message;
        }

        public String getCreatedByUserName() {
            return createdByUserName;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public String getSubject() {
            return subject;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public String getMessage() {
            return message;
        }
    }
}
