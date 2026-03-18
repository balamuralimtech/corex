package com.web.coretix.usermanagement;

import com.module.coretix.usermanagement.IUserAdministrationService;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

@Named("changePasswordBean")
@Scope("session")
public class ChangePasswordBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private final Logger logger = Logger.getLogger(getClass());

    private String username;
    private String password;
    private String newPassword;
    private String confirmPassword;
    private Integer userId;

    @Inject
    private IUserAdministrationService userAdministrationService;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");

        username = "";
        password = "";
        newPassword = "";
        confirmPassword = "";
        userId = null;


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

    public String changePassword() {

//        logger.debug("sessionUsername : " + sessionUsername);
        logger.debug("sessionUsername : " + username);
        logger.debug("entered into changePassword !!!");
        logger.debug("username : " + username);
        logger.debug("userId : " + userId);
        logger.debug("password : " + password);
        logger.debug("newPassword : " + newPassword);
        logger.debug("confirmPassword : " + confirmPassword);
        logger.debug("end of changePassword !!!");

        boolean isUserValid = userAdministrationService.isUserValid(username, password);
        if (isUserValid) {

            logger.debug("isUserValid : " + isUserValid);

            if (password.equals(newPassword)) {
                logger.debug("New Password password cannot be old Password");
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New Password password cannot be same as old Password"));
            } else if (!newPassword.equals(confirmPassword)) {

                logger.debug("New Password and Confirm Password do not match!");
                logger.error("New Password and Confirm Password do not match!");
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New Password and Confirm Password do not match!"));
            } else if (newPassword.equals(confirmPassword)) {
                logger.debug("New Password and Confirm Password match!");
                logger.debug("userId : " + userId);
                logger.debug("newPassword : " + newPassword);
                userAdministrationService.updateUserPassword(userId, newPassword);

                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Password changed successfully"));
            }
        }
        else {
logger.debug("isUserValid : " + isUserValid);
// Handle case where user is not valid
            logger.error("Username and Password do not match!");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Username and Password do not match!"));
            return null;
        }

// Continue with password update if the passwords match

        //userAdministrationService.updateUserPassword();
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

}
