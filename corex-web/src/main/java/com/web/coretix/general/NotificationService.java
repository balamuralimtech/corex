package com.web.coretix.general;

import com.web.coretix.constants.SessionAttributes;

import javax.servlet.http.HttpSession;

public class NotificationService {

    public static void sendGrowlMessageToAll(String message) {
        for (HttpSession session : SessionListeners.getActiveSessions()) {
            session.setAttribute(SessionAttributes.APPLICATION_NOTIFICATION_GROWL.getName(), message);
        }
    }

}
