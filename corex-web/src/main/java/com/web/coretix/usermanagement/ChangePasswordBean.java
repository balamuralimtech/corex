package com.web.coretix.usermanagement;

import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.web.coretix.appgeneral.GenericManagedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("changePasswordBean")
@Scope("session")
public class ChangePasswordBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordBean.class);

    private String username;
    private String password;
    private String newPassword;
    private String confirmPassword;
    private Integer userId;

    private boolean currentPasswordError = false;
    private boolean newPasswordError = false;
    private boolean confirmPasswordError = false;

    @Inject
    private IUserAdministrationService userAdministrationService;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");

        username = "";
        password = "";
        newPassword = "";
        confirmPassword = "";
        userId = null;
        resetErrorFlags();


        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        String sessionUsername = (String) session.getAttribute("username");
        logger.debug("sessionUsername : " + sessionUsername);
        if (sessionUsername != null) {
            username = sessionUsername;
//            logger.debug("username : " + username);
        }

        userId = (Integer) session.getAttribute("userAccountId");
        logger.debug("userId : " + userId);
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetErrorFlags() {
        currentPasswordError = false;
        newPasswordError = false;
        confirmPasswordError = false;
    }

    public String changePassword() {

        logger.debug("sessionUsername : " + username);
        logger.debug("entered into changePassword !!!");
        logger.debug("username : " + username);
        logger.debug("userId : " + userId);
        logger.debug("end of changePassword !!!");

        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            currentPasswordError = true;
            hasErrors = true;
            errorFieldIds.add("form:oldPassword");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Current password is required"));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            newPasswordError = true;
            hasErrors = true;
            errorFieldIds.add("form:newPassword");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New password is required"));
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            confirmPasswordError = true;
            hasErrors = true;
            errorFieldIds.add("form:confirmPassword");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Confirm password is required"));
        }

        if (hasErrors) {
            org.primefaces.PrimeFaces.current().executeScript(
                "highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return null;
        }

        // Get user details and verify current password using BCrypt
        UserDetails userDetails =
            userAdministrationService.getUserDetailEntityByUserName(username);

        boolean isUserValid = false;
        if (userDetails != null && userDetails.getPassword() != null) {
            // Verify current password using BCrypt
            isUserValid = verifyPassword(password, userDetails.getPassword());
        }

        if (isUserValid) {
            logger.debug("isUserValid : " + isUserValid);

            if (password.equals(newPassword)) {
                logger.debug("New Password cannot be old Password");
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New Password cannot be same as old Password"));
            } else if (!newPassword.equals(confirmPassword)) {
                logger.debug("New Password and Confirm Password do not match!");
                logger.error("New Password and Confirm Password do not match!");
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New Password and Confirm Password do not match!"));
            } else if (newPassword.equals(confirmPassword)) {
                logger.debug("New Password and Confirm Password match!");
                logger.debug("userId : " + userId);

                // Hash the new password using BCrypt before storing
                String hashedPassword = hashPassword(newPassword);
                logger.debug("New password hashed successfully");

                userAdministrationService.updateUserPassword(userId, hashedPassword);

                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Password changed successfully"));
            }
        } else {
            logger.debug("isUserValid : " + isUserValid);
            logger.error("Username and Password do not match!");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Current password is incorrect!"));
            return null;
        }

        return null;
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isCurrentPasswordError() {
        return currentPasswordError;
    }

    public boolean isNewPasswordError() {
        return newPasswordError;
    }

    public boolean isConfirmPasswordError() {
        return confirmPasswordError;
    }

}

