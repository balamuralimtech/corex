/*
 * SessionListeners.java
 *
 * Created on Aug 29, 2020, 11:04:57 AM
 *
 * Copyright © 2013-2014 BMSolutions.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * BMSolutions.("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with BMSolutions.
 */

package com.web.coretix.general;

import com.web.coretix.constants.SessionAttributes;

import java.util.*;
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

    private static Map sessionMap = new HashMap();

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
    public static Set getActiveSessionIds()
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
            for (Object sessionId : sessionMap.keySet()) {
                HttpSession session = (HttpSession) sessionMap.get(sessionId);
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
            for (Object sessionId : sessionMap.keySet()) {
                HttpSession session = (HttpSession) sessionMap.get(sessionId);
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
            httpSession = (HttpSession) sessionMap.get(sessionId);
        }
        return httpSession;
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

