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

import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.web.coretix.appgeneral.LoginBean;
import com.web.coretix.applicationConstants.ApplicationSessionAttributes;
import com.web.coretix.constants.SessionAttributes;

import java.util.*;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.UserActivityConstants;
import org.apache.log4j.Logger;


/**
 * @since BMSolutions
 * @author balamurali
 */
public class SessionListeners implements HttpSessionListener
{
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    private IUserAdministrationService userAdministrationService;

    @Inject
    private IUserActivityService userActivityService;

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
        if (httpSession != null)
        {
            String userLoginName = (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName());
            int userId = (int) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.USER_ACCOUNT_ID.getName());

            if(userLoginName != null)
            {
                logger.info("The user session for " + userLoginName + " is "
                        + "going to invalidate now. The session id is " + httpSession.getId());
                UserActivities userActivityTO = populateUserActivityTO();
                userActivityTO.setActivityType(UserActivityConstants.LOGOUT.getValue());
                userActivityTO.setActivityDescription("User Logged Out");
                userActivityTO.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                userActivityService.addUserActivity(userActivityTO);

                userAdministrationService.updateUserStatus(userId, LoginConstants.LOGOUT_SUCCESSFUL.getId());
            }
           sessionMap.remove(httpSession.getId());
        }
    }

    public UserActivities populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivities userActivityTO = new UserActivities();

        if (httpSession != null) {
            logger.debug("httpSession.getId() : " + httpSession.getId());
            logger.debug("#############################################################################");
            logger.debug((Integer) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.USER_ACCOUNT_ID.getName()));
            logger.debug((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.USERNAME.getName()));
            logger.debug((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.MACHINE_IP.getName()));
            logger.debug((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.MACHINE_NAME.getName()));
            logger.debug("#############################################################################");

            userActivityTO.setUserId((Integer) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.USERNAME.getName()));
            // Assuming appropriate keys for the following attributes
            userActivityTO.setIpAddress((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(com.web.coretix.constants.SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
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
