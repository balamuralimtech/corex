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

import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.usermanagement.Roles;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.web.coretix.constants.AccessRightConstants;
import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.UserTypeConstants;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("setupBean")
@Scope("session")
public class SetupBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(SetupBean.class);
    private static final String APPLICATION_ADMIN_ROLE = "Application Admin";

    @Inject
    private IUserAdministrationService userAdministrationService;

    @Inject
    private IRoleAdministrationService roleAdministrationService;

    private String username;
    private String password;
    private String confirmPassword;
    private String emailId;
    private String contact;

    public void initializePageAttributes() throws Exception {
        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }

        if (!isBootstrapRequired()) {
            redirectToLogin();
            return;
        }

        username = null;
        password = null;
        confirmPassword = null;
        emailId = null;
        contact = null;
    }

    public void createInitialApplicationAdmin() {
        try {
            if (!isBootstrapRequired()) {
                redirectToLogin();
                return;
            }

            validateInput();
            Roles role = ensureApplicationAdminRole();

            UserDetails userDetails = new UserDetails();
            userDetails.setUserName(username.trim());
            userDetails.setPassword(GenericManagedBean.hashPassword(password));
            userDetails.setEmailId(emailId.trim());
            userDetails.setContact(StringUtils.isBlank(contact) ? null : contact.trim());
            userDetails.setRole(role);
            userDetails.setOrganization(null);
            userDetails.setBranch(null);
            userDetails.setCountry(null);
            userDetails.setState(null);
            userDetails.setCity(null);
            userDetails.setAddress(null);
            userDetails.setAccessRight(AccessRightConstants.Organization.getId());
            userDetails.setStatus(LoginConstants.NEVER_LOGIN_BEFORE.getId());
            userDetails.setUserType(UserTypeConstants.APPLICATION_ADMIN.getValue());

            userAdministrationService.addUserDetail(userDetails);
            logger.info("Initial application admin created: {}", userDetails.getUserName());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Setup Complete", "Initial application admin created. You can now sign in."));
            PrimeFaces.current().ajax().update("setupForm:messages");
            redirectToLogin();
        } catch (Exception exception) {
            logger.error("Unable to create initial application admin", exception);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Setup Failed", exception.getMessage()));
            PrimeFaces.current().ajax().update("setupForm:messages");
        }
    }

    public boolean isBootstrapRequired() {
        return userAdministrationService.getUserCount() == 0;
    }

    private void validateInput() {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (StringUtils.isBlank(emailId)) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and confirm password must match.");
        }
        if (userAdministrationService.getUserDetailEntityByUserName(username.trim()) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }
    }

    private Roles ensureApplicationAdminRole() {
        Roles existingRole = null;
        try {
            existingRole = roleAdministrationService.getRoleEntityByRoleName(APPLICATION_ADMIN_ROLE);
        } catch (Exception exception) {
            logger.debug("Application admin role not found yet");
        }

        if (existingRole != null) {
            return existingRole;
        }

        Roles role = new Roles();
        role.setRoleName(APPLICATION_ADMIN_ROLE);
        roleAdministrationService.addRole(role);
        return role;
    }

    private void redirectToLogin() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getFlash().setKeepMessages(true);
        String contextPath = facesContext.getExternalContext().getRequestContextPath();
        facesContext.getExternalContext().redirect(contextPath + "/login");
        facesContext.responseComplete();
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
