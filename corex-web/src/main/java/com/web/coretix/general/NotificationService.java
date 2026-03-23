package com.web.coretix.general;

import com.web.coretix.constants.SessionAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private static final int MAX_TOPBAR_MESSAGES = 10;

    public static void sendGrowlMessageToAll(String message) {
        for (HttpSession session : SessionListeners.getActiveSessions()) {
            storeNotification(session, message);
        }
    }

    public static void sendGrowlMessageToOrganization(Integer organizationId, String message) {
        if (organizationId == null || message == null || message.trim().isEmpty()) {
            return;
        }

        for (HttpSession session : SessionListeners.getActiveSessions()) {
            Object sessionOrganizationId = session.getAttribute(SessionAttributes.ORGANIZATION_ID.getName());
            if (organizationId.equals(sessionOrganizationId)) {
                storeNotification(session, message);
            }
        }
    }

    private static void storeNotification(HttpSession session, String message) {
        session.setAttribute(SessionAttributes.APPLICATION_NOTIFICATION_GROWL.getName(), message);
        session.setAttribute(SessionAttributes.APPLICATION_NOTIFICATION_MESSAGES.getName(),
                buildUpdatedNotificationMessages(session, message));
        session.setAttribute(SessionAttributes.APPLICATION_NOTIFICATION_UNREAD_COUNT.getName(),
                resolveUnreadCount(session) + 1);
    }

    @SuppressWarnings("unchecked")
    private static List<String> buildUpdatedNotificationMessages(HttpSession session, String message) {
        List<String> existingMessages =
                (List<String>) session.getAttribute(SessionAttributes.APPLICATION_NOTIFICATION_MESSAGES.getName());
        List<String> updatedMessages = existingMessages == null ? new ArrayList<>() : new ArrayList<>(existingMessages);
        updatedMessages.add(0, message);

        while (updatedMessages.size() > MAX_TOPBAR_MESSAGES) {
            updatedMessages.remove(updatedMessages.size() - 1);
        }

        return updatedMessages;
    }

    private static int resolveUnreadCount(HttpSession session) {
        Object unreadCount = session.getAttribute(SessionAttributes.APPLICATION_NOTIFICATION_UNREAD_COUNT.getName());
        return unreadCount instanceof Integer ? (Integer) unreadCount : 0;
    }

}
