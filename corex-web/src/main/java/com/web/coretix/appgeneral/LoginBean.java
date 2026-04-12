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
package com.web.coretix.appgeneral;

import com.module.coretix.license.ILicenseService;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.SessionListeners;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.web.coretix.constants.AccessRightConstants;
import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserTypeConstants;
import com.web.coretix.general.SessionAuditSupport;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Named("loginBean")
@Scope("session")
public class LoginBean extends GenericManagedBean implements Serializable  {

    private static final long serialVersionUID = 13543439334535435L;
    private static final Logger logger = LoggerFactory.getLogger(LoginBean.class);

    private String username;
    private String password;
    private String encryptedPassword;
    private String loginEncryptionPublicKey;
    private String year;
    private boolean bootstrapRequired;
    private transient PrivateKey loginEncryptionPrivateKey;

    @Inject
    private transient IUserAdministrationService userAdministrationService;

    @Inject
    private transient IUserActivityService userActivityService;

    @Inject
    private transient ILicenseService licenseService;

    public String navigateToLoginPage() {
        logger.debug("entered into LoginBean navigateToLoginPage !!!");
        logger.debug("end of LoginBean navigateToLoginPage !!!");
        return "login";
    }

    public String login() throws Exception {
        if (isBootstrapRequired()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Setup Required", "Create the first application admin before logging in."));
            PrimeFaces.current().ajax().update("form:messages");
            return null;
        }

        username = username == null ? null : username.trim();
        password = resolveSubmittedPassword();

        logger.debug("username : " + username);

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            logger.debug("username and password is not empty");

            // Get user details by username first
            UserDetails userDetails = getUserDetailsByUsername(username);

            // Verify password using BCrypt
            boolean isUserValid = false;
            if (userDetails != null && userDetails.getPassword() != null) {
                // Verify the entered password against the hashed password from database
                isUserValid = verifyPassword(password, userDetails.getPassword());
                logger.debug("Password verification result: " + isUserValid);
            }

            UserActivities userActivities = new UserActivities();
            userActivities.setActivityType(UserActivityConstants.L0GIN.getValue());
            userActivities.setIpAddress(getMachineIP());
            userActivities.setDeviceInfo(getMachineName());
            userActivities.setLocationInfo(getBrowserClientInfo());
            userActivities.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

            if (isUserValid && userDetails != null) {
                String licenseValidationMessage = getLicenseValidationMessage(userDetails);
                if (licenseValidationMessage != null) {
                    userActivities.setUserId(userDetails.getUserId());
                    userActivities.setUserName(userDetails.getUserName());
                    userActivities.setActivityDescription(LoginConstants.LICENSE_EXPIRED.getValue());
                    userActivityService.addUserActivity(userActivities);
                    userAdministrationService.updateUserStatus(userDetails.getUserId(), LoginConstants.LICENSE_EXPIRED.getId());
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "License Error", licenseValidationMessage));
                    PrimeFaces.current().ajax().update("form:messages","form:username","form:password");
                    return null;
                }

                logger.debug("User login successful !");
                userActivities.setUserId(userDetails.getUserId());
                userActivities.setUserName(userDetails.getUserName());
                userActivities.setActivityDescription(LoginConstants.SUCCESSFUL_LOGIN.getValue());
                setSessionAttributes(userDetails);
                userActivities.setSessionId(getCurrentSessionId());
                userActivityService.addUserActivity(userActivities);
                userAdministrationService.markLoginSuccess(userDetails.getUserId(), getCurrentSessionId());

                logger.info("User login successful: " + username);
                FacesContext facesContext = FacesContext.getCurrentInstance();
                String contextPath = facesContext.getExternalContext().getRequestContextPath();
                facesContext.getExternalContext().redirect(contextPath + "/home");
                facesContext.responseComplete();
                return null;
            } else {
                userActivities.setUserName(username);
                userActivities.setActivityDescription(LoginConstants.FAILED_LOGIN.getValue());
                userActivityService.addUserActivity(userActivities);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Username/Password is wrong!"));
                PrimeFaces.current().ajax().update("form:messages","form:username","form:password");
            }
        } else {
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

        return null;
    }

    private String resolveSubmittedPassword() {
        String fallbackPassword = password == null ? null : password.trim();
        try {
            if (StringUtils.isNotBlank(encryptedPassword)) {
                return decryptSubmittedPassword(encryptedPassword).trim();
            }
        } catch (Exception exception) {
            logger.warn("Unable to decrypt submitted password", exception);
            if (StringUtils.isNotBlank(fallbackPassword)) {
                logger.info("Falling back to transport-protected password submission because login encryption state is unavailable");
                return fallbackPassword;
            }
            return null;
        } finally {
            encryptedPassword = null;
        }
        return fallbackPassword;
    }

    private String decryptSubmittedPassword(String encryptedValue) throws Exception {
        if (loginEncryptionPrivateKey == null) {
            throw new IllegalStateException("Login encryption key is not initialized.");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, loginEncryptionPrivateKey, new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT));
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private void initializeLoginEncryption() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            loginEncryptionPrivateKey = keyPair.getPrivate();
            loginEncryptionPublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        } catch (Exception exception) {
            loginEncryptionPrivateKey = null;
            loginEncryptionPublicKey = null;
            logger.error("Unable to initialize login encryption keys", exception);
        }
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

    private String getLicenseValidationMessage(UserDetails userDetails) {
        if (userDetails != null
                && UserTypeConstants.APPLICATION_ADMIN == UserTypeConstants.fromValue(userDetails.getUserType())) {
            return null;
        }

        if (userDetails == null || userDetails.getOrganization() == null) {
            return "No license details found. Please contact admin for license registration.";
        }

        Licenses license = licenseService.getLicenseByOrganizationId(userDetails.getOrganization().getId());
        if (license == null) {
            return "No license details found. Please contact admin for license registration.";
        }

        if (!licenseService.isLicenseActiveForOrganization(userDetails.getOrganization().getId())) {
            return "Your license has expired. Please contact admin.";
        }

        return null;
    }

    // Method to get machine IP and name
    public static String getMachineIP() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.isNotBlank(forwardedFor)) {
                return forwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }

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

    private void setSessionAttributes(UserDetails userDetails) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession existingSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (existingSession != null) {
            try {
                existingSession.invalidate();
            } catch (IllegalStateException ex) {
                logger.debug("Anonymous session was already invalidated before login completion", ex);
            }
        }

        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60);

        // Set all attributes using the cached userDetails object
        session.setAttribute(SessionAttributes.USERNAME.getName(), username);
        session.setAttribute(SessionAttributes.USER_ACCOUNT_ID.getName(), userDetails.getUserId());
        session.setAttribute(SessionAttributes.LOGIN_STATUS.getName(), LoginConstants.SUCCESSFUL_LOGIN.getValue());
        session.setAttribute(SessionAttributes.LOGIN_TIME.getName(), System.currentTimeMillis());
        session.setAttribute(SessionAttributes.ROLE_ID.getName(), userDetails.getRole().getId());
        session.setAttribute(SessionAttributes.ROLE.getName(), userDetails.getRole().getRoleName());
        session.setAttribute(SessionAttributes.USER_TYPE.getName(), UserTypeConstants.fromValue(userDetails.getUserType()).getValue());
        session.setAttribute(SessionAttributes.ACCESS_RIGHT.getName(), AccessRightConstants.getById(userDetails.getAccessRight()).getValue());
        session.setAttribute(SessionAttributes.ACCESS_RIGHT_ID.getName(), userDetails.getAccessRight());
        session.setAttribute(SessionAttributes.ORGANIZATION_ID.getName(),
                userDetails.getOrganization() == null ? null : userDetails.getOrganization().getId());
        session.setAttribute(SessionAttributes.ORGANIZATION_NAME.getName(),
                userDetails.getOrganization() == null ? null : userDetails.getOrganization().getOrganizationName());
        session.setAttribute(SessionAttributes.MACHINE_IP.getName(), getMachineIP());
        session.setAttribute(SessionAttributes.MACHINE_NAME.getName(), getMachineName());
        session.setAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName(), getBrowserClientInfo());
        session.setAttribute(SessionAttributes.COUNTRY_ID.getName(),
                userDetails.getCountry() == null ? null : userDetails.getCountry().getId());
        session.setAttribute(SessionAttributes.SESSION_AUDIT_COMPLETED.getName(), Boolean.FALSE);

        SessionListeners.updateSessionMap(session);
        SessionAuditSupport.touchSession(session);
    }

    private String getCurrentSessionId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        return session != null ? session.getId() : null;
    }

    public void initializePageAttributes() {
        logger.debug("entered into LoginBean initializePageAttributes !!!");

        if (FacesContext.getCurrentInstance().isPostback()) {
            logger.debug("postback detected, skipping login page reset");
            return;
        }

        username = null;
        password = null;
        encryptedPassword = null;
        initializeLoginEncryption();
        bootstrapRequired = userAdministrationService.getUserCount() == 0;

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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getLoginEncryptionPublicKey() {
        return loginEncryptionPublicKey;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public boolean isBootstrapRequired() {
        return bootstrapRequired;
    }

}




