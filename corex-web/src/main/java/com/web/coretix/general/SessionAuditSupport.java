package com.web.coretix.general;

import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

public final class SessionAuditSupport {

    private static final Logger logger = LoggerFactory.getLogger(SessionAuditSupport.class);

    private SessionAuditSupport() {
    }

    public static void touchSession(HttpSession session) {
        if (session == null) {
            return;
        }

        session.setAttribute(SessionAttributes.LAST_ACTIVITY_AT.getName(), System.currentTimeMillis());

        Integer userId = (Integer) session.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());
        if (userId == null) {
            return;
        }

        try {
            getUserAdministrationService(session.getServletContext()).touchUserSession(userId, session.getId());
        } catch (Exception ex) {
            logger.error("Unable to update last seen for session {}", session.getId(), ex);
        }
    }

    public static boolean auditSessionTermination(HttpSession session, LoginConstants status, String terminationReason,
                                                  boolean invalidateSession) {
        if (session == null) {
            return false;
        }

        if (Boolean.TRUE.equals(session.getAttribute(SessionAttributes.SESSION_AUDIT_COMPLETED.getName()))) {
            SessionListeners.removeSessionFromSessionMap(session.getId());
            return false;
        }

        Integer userId = (Integer) session.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());
        String userName = (String) session.getAttribute(SessionAttributes.USERNAME.getName());
        if (userId == null || userName == null) {
            SessionListeners.removeSessionFromSessionMap(session.getId());
            return false;
        }

        try {
            UserActivities userActivity = buildTerminationActivity(session, terminationReason);
            getUserActivityService(session.getServletContext()).addUserActivity(userActivity);
            getUserAdministrationService(session.getServletContext()).markLogout(userId, status.getId(), session.getId());
            session.setAttribute(SessionAttributes.SESSION_AUDIT_COMPLETED.getName(), Boolean.TRUE);
            session.setAttribute(SessionAttributes.SESSION_TERMINATION_REASON.getName(), terminationReason);
        } catch (Exception ex) {
            logger.error("Unable to persist session termination audit for session {}", session.getId(), ex);
        } finally {
            SessionListeners.removeSessionFromSessionMap(session.getId());
        }

        if (invalidateSession) {
            try {
                session.invalidate();
            } catch (IllegalStateException ex) {
                logger.debug("Session {} was already invalidated", session.getId(), ex);
            }
        }

        return true;
    }

    private static UserActivities buildTerminationActivity(HttpSession session, String terminationReason) {
        UserActivities userActivity = new UserActivities();
        userActivity.setUserId((Integer) session.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
        userActivity.setUserName((String) session.getAttribute(SessionAttributes.USERNAME.getName()));
        userActivity.setActivityType("Logout");
        userActivity.setActivityDescription("User session ended: " + terminationReason);
        userActivity.setIpAddress((String) session.getAttribute(SessionAttributes.MACHINE_IP.getName()));
        userActivity.setDeviceInfo((String) session.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
        userActivity.setLocationInfo((String) session.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        userActivity.setSessionId(session.getId());
        userActivity.setTerminationReason(terminationReason);
        userActivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return userActivity;
    }

    private static IUserAdministrationService getUserAdministrationService(ServletContext servletContext) {
        return (IUserAdministrationService) getApplicationContext(servletContext).getBean("userAdministrationService");
    }

    private static IUserActivityService getUserActivityService(ServletContext servletContext) {
        return (IUserActivityService) getApplicationContext(servletContext).getBean("userActivityService");
    }

    private static WebApplicationContext getApplicationContext(ServletContext servletContext) {
        return WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }
}
