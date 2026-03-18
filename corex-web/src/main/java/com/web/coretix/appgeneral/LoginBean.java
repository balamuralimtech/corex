package com.web.coretix.appgeneral;

import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.SessionListeners;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.web.coretix.constants.AccessRightConstants;
import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.SessionAttributes;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Named("loginBean")
@Scope("session")
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private final Logger logger = Logger.getLogger(getClass());

    private String username;
    private String password;
    private String year;

    @Inject
    private IUserAdministrationService userAdministrationService;

    @Inject
    private IUserActivityService userActivityService;

    public String navigateToLoginPage() {
        logger.debug("entered into LoginBean navigateToLoginPage !!!");
        logger.debug("end of LoginBean navigateToLoginPage !!!");
        return "login";
    }

    public String login() {
        logger.debug("entered into LoginBean login !!!");


        username = username.trim();
        password = password.trim();
        logger.debug("username : " + username);
        logger.debug("password : " + password);
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            logger.debug("if ((username != null || !username.isEmpty()) && (password != null || !password.isEmpty()) ) {");
            boolean isUserValid = userAdministrationService.isUserValid(username, password);
            logger.debug("isUserValid : " + isUserValid);
            UserActivities userActivities = new UserActivities();
            userActivities.setActivityType(UserActivityConstants.L0GIN.getValue());
            userActivities.setIpAddress(getMachineIP());
            userActivities.setDeviceInfo(getMachineName());
            userActivities.setLocationInfo(getBrowserClientInfo());
            userActivities.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            if (isUserValid) {
                logger.debug("if (isUserValid) {");
                int userId = getUserDetailsByUsername(username).getUserId();
                userActivities.setUserId(userId);
                userActivities.setUserName(getUserDetailsByUsername(username).getUserName());
                userActivities.setActivityDescription(LoginConstants.SUCCESSFUL_LOGIN.getValue());
                userActivityService.addUserActivity(userActivities);
                userAdministrationService.updateUserStatus(userId, LoginConstants.SUCCESSFUL_LOGIN.getId());
                setSessionAttributes();

                logger.debug("SessionListeners.getActiveSessionIds() : " + SessionListeners.getActiveSessionIds());
                logger.debug("End of login - User login successful");
                return "homepage";

            } else {
                logger.debug("else inside  isUserValid");
                userActivities.setUserName(username);
                userActivities.setActivityDescription(LoginConstants.FAILED_LOGIN.getValue());
                userActivityService.addUserActivity(userActivities);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Username/Password is wrong!"));
                PrimeFaces.current().ajax().update("form:messages","form:username","form:password");

            }
        } else {
            logger.debug("user name/password is empty or null");
            if (username == null || username.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Username is empty!"));
            }
            if (password == null || password.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Password is empty!"));
            }
            PrimeFaces.current().ajax().update("form:messages","form:username","form:password");
        }

        logger.debug("end of LoginBean login !!!");
        return null;
    }

    public void onIdle() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "No activity.", "User is idle"));
    }

    public void onActive() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Activity detected", "User is active"));
    }

    private UserDetails getUserDetailsByUsername(String username) {
        return userAdministrationService.getUserDetailEntityByUserName(username);
    }

    // Method to get machine IP and name
    public static String getMachineIP() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "IP address not available";
        }
    }

    public static String getMachineName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Machine name not available";
        }
    }

    // Method to get browser client info from HTTP request
    public static String getBrowserClientInfo() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        String browserDetails = request.getHeader("User-Agent");
        String userAgent = browserDetails.toLowerCase();

        String browser = "";

        // Detecting Browser
        if (userAgent.contains("msie") || userAgent.contains("trident")) {
            browser = "IE";
        } else if (userAgent.contains("edge")) {
            browser = "Edge";
        } else if (userAgent.contains("safari") && userAgent.contains("version")) {
            browser = "Safari";
        } else if (userAgent.contains("opr") || userAgent.contains("opera")) {
            browser = "Opera";
        } else if (userAgent.contains("chrome") && !userAgent.contains("edge")) {
            browser = "Chrome";
        } else if (userAgent.contains("firefox")) {
            browser = "Firefox";
        } else if ((userAgent.contains("mozilla/7.0")) || (userAgent.contains("netscape6")) || (userAgent.contains("mozilla/4.7")) || (userAgent.contains("mozilla/4.78")) || (userAgent.contains("mozilla/4.08")) || (userAgent.contains("mozilla/3"))) {
            browser = "Netscape";
        } else {
            browser = "Unknown, More-Info: " + browserDetails;
        }

        return browser;
    }

    private void setSessionAttributes() {
        logger.debug("entered into LoginBean setSessionAttributes !!!");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute(SessionAttributes.USERNAME.getName(), username);
        logger.debug("Session Attribute USERNAME set to: " + username);
        session.setAttribute(SessionAttributes.USER_ACCOUNT_ID.getName(), getUserDetailsByUsername(username).getUserId());
        logger.debug("Session Attribute USER_ACCOUNT_ID set to: " + getUserDetailsByUsername(username).getUserId());
        session.setAttribute(SessionAttributes.LOGIN_STATUS.getName(), LoginConstants.SUCCESSFUL_LOGIN.getValue());
        logger.debug("Session Attribute LOGIN_STATUS set to: " + LoginConstants.SUCCESSFUL_LOGIN.getValue());
        session.setAttribute(SessionAttributes.LOGIN_TIME.getName(), System.currentTimeMillis());
        logger.debug("Session Attribute LOGIN_TIME set to: " + System.currentTimeMillis());
        session.setAttribute(SessionAttributes.ROLE_ID.getName(), getUserDetailsByUsername(username).getRole().getId());
        logger.debug("Session Attribute ROLE_ID set to: " + getUserDetailsByUsername(username).getRole().getId());
        session.setAttribute(SessionAttributes.ROLE.getName(), getUserDetailsByUsername(username).getRole().getRoleName());
        logger.debug("Session Attribute ROLE set to: " + getUserDetailsByUsername(username).getRole().getRoleName());
        session.setAttribute(SessionAttributes.ACCESS_RIGHT.getName(), AccessRightConstants.getById(getUserDetailsByUsername(username).getAccessRight()).getValue());
        logger.debug("Session Attribute ACCESS_RIGHT set to: " + AccessRightConstants.getById(getUserDetailsByUsername(username).getAccessRight()).getValue());
        session.setAttribute(SessionAttributes.ACCESS_RIGHT_ID.getName(), getUserDetailsByUsername(username).getAccessRight());
        logger.debug("Session Attribute ACCESS_RIGHT_ID set to: " + getUserDetailsByUsername(username).getAccessRight());
        session.setAttribute(SessionAttributes.ORGANIZATION_ID.getName(), getUserDetailsByUsername(username).getOrganization().getId());
        logger.debug("Session Attribute ORGANIZATION_ID set to: " + getUserDetailsByUsername(username).getOrganization().getId());
        session.setAttribute(SessionAttributes.ORGANIZATION_NAME.getName(), getUserDetailsByUsername(username).getOrganization().getOrganizationName());
        logger.debug("Session Attribute ORGANIZATION_NAME set to: " + getUserDetailsByUsername(username).getOrganization().getOrganizationName());
        session.setAttribute(SessionAttributes.MACHINE_IP.getName(), getMachineIP());
        logger.debug("Session Attribute MACHINE_IP set to: " + getMachineIP());
        session.setAttribute(SessionAttributes.MACHINE_NAME.getName(), getMachineName());
        logger.debug("Session Attribute MACHINE_NAME set to: " + getMachineName());
        session.setAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName(), getBrowserClientInfo());
        logger.debug("Session Attribute BROWSER_CLIENT_INFO set to: " + getBrowserClientInfo());
        session.setAttribute(SessionAttributes.COUNTRY_ID.getName(), getUserDetailsByUsername(username).getCountry().getId());
        logger.debug("Session Attribute COUNTRY_ID set to: " + getUserDetailsByUsername(username).getCountry().getId());
        SessionListeners.updateSessionMap(session);

        logger.debug("SessionListeners.getNoActiveSessions() : " + SessionListeners.getNoActiveSessions());
        logger.debug("getActiveSessionIds !!!"+SessionListeners.getActiveSessionIds());
        logger.debug("fetchActiveUserNameList !!!"+SessionListeners.fetchActiveUserNameList());
        logger.debug("get active sessions : "+SessionListeners.getActiveSessions());

        logger.debug("end of login setSessionAttributes");
    }

    public void initializePageAttributes() {
        logger.debug("entered into LoginBean initializePageAttributes !!!");

        username = null;
        password = null;

        year = String.valueOf(java.time.Year.now());

        logger.debug("year : " + year);

        logger.debug("end of LoginBean initializePageAttributes !!!");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
