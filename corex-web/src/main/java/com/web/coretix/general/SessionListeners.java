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
package com.web.coretix.general;

import com.web.coretix.constants.SessionAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.web.coretix.constants.LoginConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @since BMSolutions
 * @author balamurali
 */
public class SessionListeners implements HttpSessionListener
{
    private static final Logger logger = LoggerFactory.getLogger(SessionListeners.class);

    private static final Map<String, HttpSession> sessionMap = new java.util.concurrent.ConcurrentHashMap<>();

    /** Creates a new instance of SessionLifeCycleListener */
    public SessionListeners()
    {
    }

    /**
     * This method is sessionCreated method of HttpSessionListener interface
     * @param httpSessionEvent
     */
     @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent)
    {        
        logger.info("Session has been created in the server and its id: " + httpSessionEvent.getSession().getId());
    }

    /**
     * This method is the sessionDestroyed method of HttpSessionListener interface
     * @param httpSessionEvent
     */
     @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent)
    {
        HttpSession httpSession = httpSessionEvent.getSession();
        if (httpSession != null) {
            String userLoginName = (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName());
            if (userLoginName != null) {
                logger.info("The user session for {} is being destroyed. The session id is {}",
                        userLoginName, httpSession.getId());
            }
            SessionAuditSupport.auditSessionTermination(httpSession, LoginConstants.SESSION_TIMEOUT,
                    "SESSION_TIMEOUT", false);
        }
    }

    /**
     * This method is used to get the size of session map
     * @return size of sessionMap
     */
    public static int getNoActiveSessions()
    {
        return sessionMap.size();
    }

    public static void removeSessionFromSessionMap(String sessionId)
    {
        sessionMap.remove(sessionId);
    }

    /**
     * This method is used to get active sessions
     * @return Set of active session Id's
     */
    public static Set<String> getActiveSessionIds()
    {
        return sessionMap.keySet();
    }

    /**
     * This method is used to getPlatformDelegate a session is active or not ?
     * @param sessionId
     * @return a session is active or not?
     */
    public static boolean isActive(String sessionId)
    {
        return sessionMap.containsKey(sessionId);
    }

    /**
     * Retrieves a list of usernames corresponding to active user sessions.
     * This method fetches the usernames from the current active session data
     * and compiles them into a list for further processing or display.
     *
     * @return A list of strings representing usernames of active users.
     */
    public static List<String> fetchActiveUserNameList()
    {
        List<String> userNameList = new ArrayList<>();

        if (sessionMap != null && !sessionMap.isEmpty())
        {
            for (String sessionId : sessionMap.keySet()) {
                HttpSession session = sessionMap.get(sessionId);
                if (session != null) {
                    userNameList.add((String) session.getAttribute(SessionAttributes.USERNAME.getName()));
                }
            }
        }

        return userNameList;
    }

    public static List<HttpSession> getActiveSessions()
    {
        List<HttpSession> activeSessions = new ArrayList<>();
        if (sessionMap != null && !sessionMap.isEmpty())
        {
            for (String sessionId : sessionMap.keySet()) {
                HttpSession session = sessionMap.get(sessionId);
                if (session != null) {
                    activeSessions.add(session);
                }
            }
        }
        return activeSessions;
    }

    /**
     * This method is used to get the associated session object for a session Id
     * @param sessionId is the unique id for the session
     * @return HttpSession object
     */
    public static HttpSession getAssociatedSession(String sessionId)
    {
        HttpSession httpSession = null;
        if (isActive(sessionId))
        {
            httpSession = sessionMap.get(sessionId);
        }
        return httpSession;
    }

    public static boolean hasOtherActiveSessionsForUser(String userName, String excludedSessionId) {
        return findAnotherActiveSessionIdForUser(userName, excludedSessionId) != null;
    }

    public static String findAnotherActiveSessionIdForUser(String userName, String excludedSessionId) {
        if (userName == null || userName.trim().isEmpty()) {
            return null;
        }

        Collection<HttpSession> activeSessions = sessionMap.values();
        for (HttpSession activeSession : activeSessions) {
            if (activeSession == null) {
                continue;
            }

            if (excludedSessionId != null && excludedSessionId.equals(activeSession.getId())) {
                continue;
            }

            Object sessionUserName = activeSession.getAttribute(SessionAttributes.USERNAME.getName());
            if (userName.equals(sessionUserName)) {
                return activeSession.getId();
            }
        }

        return null;
    }
    
    public static void updateSessionMap(HttpSession session)
    {
        sessionMap.put(session.getId(), session);
    }

    /**
     * This method is used to formate a session in a particular pattern
     * <p>
     * @param httpSession
     * @return formatted session string
     */
    public final static String formateSession(HttpSession httpSession)
    {
        return httpSession == null ? "<null session" : "[ID=" + httpSession.getId() + "Created = "
                + new Date(httpSession.getCreationTime()) + ", Last = "
                + new Date(httpSession.getLastAccessedTime()) + ", TMO = "
                + httpSession.getMaxInactiveInterval() + ", User Name : "
                + httpSession.getAttribute(SessionAttributes.USERNAME.getName());

    }
    
    
}




