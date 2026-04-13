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
package com.web.coretix.usermanagement;

import com.module.coretix.commonto.RoleUsageCountTO;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.persist.coretix.modal.usermanagement.Roles;
import com.module.coretix.usermanagement.IModulePrivilegeService;
import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.web.coretix.general.SessionListeners;
import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.*;

import com.web.coretix.general.NotificationService;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author admin
 */
@Named("roleAdministrationBean")
@Scope("session")
public class RoleAdministrationBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(RoleAdministrationBean.class);
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");

    private List<RoleModuleBean> roleModuleList = new ArrayList<>();

    private final String viewPrivilegeValue = RolePrivilegeConstants.VIEW.getValue();

    @Inject
    private transient IModulePrivilegeService modulePrivilegeService;

    @Inject
    private transient IRoleAdministrationService roleAdministrationService;

    @Inject
    private transient IUserAdministrationService userAdministrationService;

    private String roleName;
    private List<Roles> rolesList = new ArrayList<>();

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    private int rolesUsedCount;
    private int rolesNotUsedCount;

    private Roles selectedRole = new Roles();
    private List<String> impactedActiveRoleUsers = new ArrayList<>();

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        roleName = "";
        selectedRole = new Roles();
        impactedActiveRoleUsers.clear();

        if (CollectionUtils.isNotEmpty(rolesList)) {
            logger.debug("inside  organizationList clear");
            rolesList.clear();
        }
        if (CollectionUtils.isNotEmpty(roleModuleList)) {
            logger.debug("inside  roleModuleList clear");
            roleModuleList.clear();
        }

        PrimeFaces.current().ajax().update("form:orgMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void populateRoleDefintionNewAddList() {

        logger.debug("entered into populateRoleDefintionDBList !!!");

        for (CoreAppModule module : RoleModuleCatalog.getRoleModules()) {
            roleModuleList.add(buildRoleModuleBean(module.getValue(),
                    RoleModuleCatalog.getSubmoduleValues(module)));
        }

        logger.debug("roleModuleList size : " + roleModuleList.size());
        logger.debug("end of populateRoleDefintionDBList !!!");

    }

    private RoleModuleBean buildRoleModuleBean(String moduleName, List<String> subModuleNames) {
        RoleModuleBean roleModuleBean = new RoleModuleBean();
        roleModuleBean.setModule(moduleName);
        roleModuleBean.setRoleSubModuleBeanList(new ArrayList<>());

        logger.debug("roleModuleBean.getModule() : " + roleModuleBean.getModule());

        for (String subModuleName : subModuleNames) {
            roleModuleBean.getRoleSubModuleBeanList().add(buildRoleSubModuleBean(subModuleName));
        }
        return roleModuleBean;
    }

    private RoleSubModuleBean buildRoleSubModuleBean(String subModuleName) {
        RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
        roleSubModuleBean.setSubModule(subModuleName);
        roleSubModuleBean.setRolePrivilegeBeanList(
                new ArrayList<>(PrivilegeMatrix.getAllowedPrivilegeValues(subModuleName)));
        roleSubModuleBean.setSelectedPrivilegeList(new ArrayList<>());

        logger.debug("roleSubModuleBean.getSubModule() : " + roleSubModuleBean.getSubModule());
        logger.debug("roleSubModuleBean.getRolePrivilegeBeanList() size : "
                + roleSubModuleBean.getRolePrivilegeBeanList().size());

        return roleSubModuleBean;
    }

    private void populateDBModulePrivilegesList(List<ModulePrivileges> modulePrivileges) {

        logger.debug("entered into populateDBModulePrivilegesList !!!");
        Map<String, Map<String, List<String>>> outerMap = new LinkedHashMap<>();

        for (ModulePrivileges modulePrivilege : modulePrivileges) {
            CoreAppModule module = CoreAppModule.getById(modulePrivilege.getModuleId());
            String moduleName = module.getValue();
            String submoduleName = RoleModuleCatalog.resolveSubmoduleName(modulePrivilege.getModuleId(), modulePrivilege.getSubmoduleId());

            logger.debug("I{} M : {} S : {} P : {}", modulePrivilege.getId(), moduleName, submoduleName,
                    RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());

            outerMap.putIfAbsent(moduleName, new LinkedHashMap<>());
            Map<String, List<String>> innerMap = outerMap.get(moduleName);
            innerMap.putIfAbsent(submoduleName, new ArrayList<>());
            List<String> privilegeList = innerMap.get(submoduleName);
            if (privilegeList != null) {
                privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
            }
        }

        List<String> defViewPrivilegeBeanList = new ArrayList<>();
        defViewPrivilegeBeanList.add(getViewPrivilegeValue());
        // Print the outer map (for demonstration purposes)
        for (Map.Entry<String, Map<String, List<String>>> outerEntry : outerMap.entrySet()) {
            logger.debug("AppModule: " + outerEntry.getKey());
            RoleModuleBean bean = new RoleModuleBean();
            bean.setModule(outerEntry.getKey());
            List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
            for (Map.Entry<String, List<String>> innerEntry : outerEntry.getValue().entrySet()) {
                logger.debug("  Submodule: " + innerEntry.getKey() + " -> Privileges: " + innerEntry.getValue());

                RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                roleSubModuleBean.setSubModule(innerEntry.getKey());
                List<String> availablePrivilegeList = new ArrayList<>();
                for (String string : innerEntry.getValue()) {
                    String[] parts = string.split("/");
                    if (PrivilegeMatrix.isPrivilegeAllowedForPage(innerEntry.getKey(),
                            RolePrivilegeConstants.getByValue(parts[1]))) {
                        availablePrivilegeList.add(parts[1]);
                    }
                }
                logger.debug("availablePrivilegeList : " + availablePrivilegeList);

                roleSubModuleBean.setRolePrivilegeBeanList(new ArrayList<>(
                        PrivilegeMatrix.getAllowedPrivilegeValues(innerEntry.getKey())));
                roleSubModuleBeanList.add(roleSubModuleBean);
            }
            bean.setRoleSubModuleBeanList(roleSubModuleBeanList);
            getRoleModuleList().add(bean);
        }

        logger.debug("end of populateDBModulePrivilegesList");
    }

    private void populateEditDBModulePrivilegesList(List<RolePrivileges> rolePrivileges) {
        Map<String, Map<String, List<String>>> outerMap = new LinkedHashMap<>();

        for (RolePrivileges privileges : rolePrivileges) {
            CoreAppModule module = CoreAppModule.getById(privileges.getModuleId());
            String moduleName = module.getValue();
            String submoduleName = RoleModuleCatalog.resolveSubmoduleName(privileges.getModuleId(), privileges.getSubmoduleId());

            logger.debug("I{} M : {} S : {} P : {} Is : {}", privileges.getId(), moduleName, submoduleName,
                    RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue(), privileges.getIsSelected());

            outerMap.putIfAbsent(moduleName, new LinkedHashMap<>());
            Map<String, List<String>> innerMap = outerMap.get(moduleName);
            innerMap.putIfAbsent(submoduleName, new ArrayList<>());
            List<String> privilegeList = innerMap.get(submoduleName);
            if (privilegeList != null) {
                privilegeList.add(RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue() + "/" + privileges.getIsSelected());
            }

        }

        // Print the outer map (for demonstration purposes)
        for (Map.Entry<String, Map<String, List<String>>> outerEntry : outerMap.entrySet()) {
            logger.debug("AppModule: " + outerEntry.getKey());
            RoleModuleBean bean = new RoleModuleBean();
            bean.setModule(outerEntry.getKey());
            List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
            for (Map.Entry<String, List<String>> innerEntry : outerEntry.getValue().entrySet()) {
                logger.debug("  Submodule: " + innerEntry.getKey() + " -> Privileges: " + innerEntry.getValue());

                RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                roleSubModuleBean.setSubModule(innerEntry.getKey());
                List<String> rolePrivilegeBeanList = new ArrayList<>(
                        PrivilegeMatrix.getAllowedPrivilegeValues(innerEntry.getKey()));
                List<String> selectedPrivilegeList = new ArrayList<>();
                for (String string : innerEntry.getValue()) {
                    String[] parts = string.split("/");
                    if (!PrivilegeMatrix.isPrivilegeAllowedForPage(innerEntry.getKey(),
                            RolePrivilegeConstants.getByValue(parts[0]))) {
                        continue;
                    }

                    if(Boolean.parseBoolean(parts[1]))
                    {
                        selectedPrivilegeList.add(parts[0]);
                    }
                }

                logger.debug("selectedPrivilegeList : "+selectedPrivilegeList);
                logger.debug("rolePrivilegeBeanList : "+rolePrivilegeBeanList);
                
                roleSubModuleBean.setSelectedPrivilegeList(selectedPrivilegeList);
                roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                roleSubModuleBeanList.add(roleSubModuleBean);
            }
            bean.setRoleSubModuleBeanList(roleSubModuleBeanList);
            roleModuleList.add(bean);
        }

        logger.debug("end of populateEditDBModulePrivilegesList !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");
        roleName = "";

    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
        selectedRole = new Roles();
        impactedActiveRoleUsers.clear();
        if (CollectionUtils.isNotEmpty(roleModuleList)) {
            logger.debug("inside  organizationList clear");
            roleModuleList.clear();
        }
        populateRoleDefintionNewAddList();
        PrimeFaces.current().resetInputs("form:addEditOrgPanelId");
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchRolesList();
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        if (CollectionUtils.isNotEmpty(roleModuleList)) {
            logger.debug("inside  organizationList clear");
            roleModuleList.clear();
        }
        editRole();
    }

    private void editRole() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedRole.getId() : " + getSelectedRole().getId());

        roleName = getSelectedRole().getRoleName();

        populateEditDBModulePrivilegesList(selectedRole.getRolePrivileges());

        logger.debug("roleName : " + roleName);
    }

    public void saveRole() {

        logger.debug("Inside save role method ");
        logger.debug("roleName : " + roleName);
        logger.debug("isAddOperation : " + isAddOperation);

        if (roleName == null || roleName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Role name is required"));
            PrimeFaces.current().ajax().update("form:messages", "form:addEditOrgPanelId");
            return;
        }

        if (isRoleNameDuplicate()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Role name already exists"));
            PrimeFaces.current().ajax().update("form:messages", "form:addEditOrgPanelId");
            return;
        }

        if (!isAddOperation) {
            impactedActiveRoleUsers = findActiveUsersForSelectedRole();

            if (CollectionUtils.isNotEmpty(impactedActiveRoleUsers)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Active Users Found",
                                "These users will be forced logged out after role update confirmation."));
                PrimeFaces.current().ajax().update("form:messages", "form:roleUpdateConfirmPanelId");
                PrimeFaces.current().executeScript("PF('roleUpdateConfirmDialog').show()");
                return;
            }
        }

        persistRoleChanges(false);
    }

    public void confirmRoleUpdateAndLogout() {
        persistRoleChanges(true);
    }

    private List<RolePrivileges> buildRolePrivileges(Roles role) {
        List<RolePrivileges> rolePrivilegesList = new ArrayList<>();

        for (RoleModuleBean roleModuleBean : roleModuleList) {
            logger.debug("roleModuleBean getModule : " + roleModuleBean.getModule());

            for (RoleSubModuleBean roleSubModuleBean : roleModuleBean.getRoleSubModuleBeanList()) {
                logger.debug("roleModuleBean getSubModule : " + roleSubModuleBean.getSubModule());
                logger.debug("roleSubModuleBean.getSelectedPrivilegeList() : "
                        + roleSubModuleBean.getSelectedPrivilegeList());

                List<String> allowedPrivileges = PrivilegeMatrix.getAllowedPrivilegeValues(
                        roleSubModuleBean.getSubModule());
                Set<String> selectedPrivileges = new HashSet<>();
                if (CollectionUtils.isNotEmpty(roleSubModuleBean.getSelectedPrivilegeList())) {
                    for (String selectedPrivilege : roleSubModuleBean.getSelectedPrivilegeList()) {
                        if (allowedPrivileges.contains(selectedPrivilege)) {
                            selectedPrivileges.add(selectedPrivilege);
                        }
                    }
                }

                logger.debug("allowedPrivileges : " + allowedPrivileges);
                logger.debug("filteredSelectedPrivileges : " + selectedPrivileges);

                for (String privilegeValue : allowedPrivileges) {
                    rolePrivilegesList.add(buildRolePrivilege(role, roleModuleBean.getModule(),
                            roleSubModuleBean.getSubModule(), privilegeValue,
                            selectedPrivileges.contains(privilegeValue)));
                }
            }
        }

        return rolePrivilegesList;
    }

    private RolePrivileges buildRolePrivilege(Roles role, String moduleName, String subModuleName,
            String privilegeValue, boolean isSelected) {
        RolePrivileges rolePrivileges = new RolePrivileges();
        rolePrivileges.setRoles(role);
        rolePrivileges.setModuleId(CoreAppModule.getByValue(moduleName).getId());
        rolePrivileges.setSubmoduleId(resolveSubModuleId(moduleName, subModuleName));
        rolePrivileges.setPrivilegeId(RolePrivilegeConstants.getByValue(privilegeValue).getId());
        rolePrivileges.setIsSelected(isSelected);
        return rolePrivileges;
    }

    private int resolveSubModuleId(String moduleName, String subModuleName) {
        return RoleModuleCatalog.resolveSubmoduleId(moduleName, subModuleName);
    }

    private void persistRoleChanges(boolean forceLogoutActiveUsers) {
        Roles role = new Roles();
        String normalizedRoleName = roleName.trim();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        role.setRoleName(normalizedRoleName);
        role.setRolePrivileges(buildRolePrivileges(role));
        Integer organizationId = resolveCurrentOrganizationId();
        if (isAddOperation) {
            logger.debug("if (isAddOperation) {");
            role.setCreatedAt(currentTimestamp);
            role.setUpdatedAt(currentTimestamp);
            roleAdministrationService.addRole(role);
            notifyActiveOrganizationUsers(organizationId,
                    buildRoleChangeNotification(normalizedRoleName, "added"));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role added successfully"));
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedRole.getId() : " + getSelectedRole().getId());
            Roles existingRole = roleAdministrationService.getRoleById(getSelectedRole().getId());
            role.setId(getSelectedRole().getId());
            role.setCreatedAt(existingRole != null ? existingRole.getCreatedAt() : currentTimestamp);
            role.setUpdatedAt(currentTimestamp);

            if (forceLogoutActiveUsers) {
                notifyUsersForRoleUpdate(role.getId());
            }

            roleAdministrationService.updateRole(role);
            notifyActiveOrganizationUsers(organizationId,
                    buildRoleChangeNotification(normalizedRoleName, "edited"));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role updated successfully"));
        }

        impactedActiveRoleUsers.clear();

        searchButtonAction();
        PrimeFaces.current().executeScript("PF('roleUpdateConfirmDialog').hide()");
        PrimeFaces.current().executeScript("PF('manageRoleDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:orgMainPanelId");
        logger.debug("End of save action !!!!");
    }

    private List<String> findActiveUsersForSelectedRole() {
        Set<String> activeUserNames = new LinkedHashSet<>();

        for (HttpSession session : SessionListeners.getActiveSessions()) {
            Object sessionRoleId = session.getAttribute(SessionAttributes.ROLE_ID.getName());
            Object sessionUserName = session.getAttribute(SessionAttributes.USERNAME.getName());

            if (sessionRoleId instanceof Integer
                    && selectedRole.getId() != null
                    && selectedRole.getId().equals(sessionRoleId)
                    && sessionUserName instanceof String) {
                UserDetails userDetails = userAdministrationService.getUserDetailEntityByUserName(
                        (String) sessionUserName);
                if (userDetails != null && userDetails.getRole() != null
                        && selectedRole.getId().equals(userDetails.getRole().getId())) {
                    activeUserNames.add(userDetails.getUserName());
                }
            }
        }

        return new ArrayList<>(activeUserNames);
    }

    private void notifyUsersForRoleUpdate(Integer roleId) {
        for (HttpSession session : SessionListeners.getActiveSessions()) {
            Object sessionRoleId = session.getAttribute(SessionAttributes.ROLE_ID.getName());
            if (roleId != null && roleId.equals(sessionRoleId)) {
                session.setAttribute(SessionAttributes.ROLE_UPDATE_LOGOUT_NOTIFICATION.getName(),
                        "Your role is getting updated. You will be redirected to the login page in 10 seconds.");
            }
        }
    }

    public void confirmDeleteRole() {
        deleteRole();
    }

    private void deleteRole() {
        logger.debug("inside delete organization ");
        logger.debug("selectedRole.getId() : " + getSelectedRole().getId());

        roleName = getSelectedRole().getRoleName();
        String deletedRoleName = roleName;
        Integer organizationId = resolveCurrentOrganizationId();

        logger.debug("roleName : " + roleName);

        roleAdministrationService.deleteRole(getSelectedRole());
        notifyActiveOrganizationUsers(organizationId,
                buildRoleChangeNotification(deletedRoleName, "deleted"));
        fetchRolesList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role removed successfully"));
        PrimeFaces.current().ajax().update("form:messages", "form:orgDataTableId");
    }

    private void fetchRolesList() {
        datatableRendered = false;

        RoleUsageCountTO roleUsageCountTO = roleAdministrationService.getCountOfRolesUsedAndNotUsed();
        rolesUsedCount = roleUsageCountTO.getRoleUsedCount();
        rolesNotUsedCount = roleUsageCountTO.getRoleUnusedCount();

        logger.debug("rolesUsedCount : " + rolesUsedCount);
        logger.debug("rolesNotUsedCount : " + rolesNotUsedCount);

        logger.debug("inside fetchOrganizationList ");
        if (CollectionUtils.isNotEmpty(rolesList)) {
            logger.debug("inside fetchOrganizationList clear");
            rolesList.clear();
        }
        rolesList.addAll(roleAdministrationService.getRolesList());

        if (CollectionUtils.isNotEmpty(rolesList)) {
            logger.debug("organizationList.size() : " + rolesList.size());
            datatableRendered = true;
            recordsCount = rolesList.size();
        }
    }

    /**
     * @return the datatableRendered
     */
    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    /**
     * @param datatableRendered the datatableRendered to set
     */
    public void setDatatableRendered(boolean datatableRendered) {
        this.datatableRendered = datatableRendered;
    }

    /**
     * @return the recordsCount
     */
    public int getRecordsCount() {
        return recordsCount;
    }

    /**
     * @param recordsCount the recordsCount to set
     */
    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    /**
     * @return the viewPrivilegeValue
     */
    public String getViewPrivilegeValue() {
        return viewPrivilegeValue;
    }

    /**
     * @return the roleModuleList
     */
    public List<RoleModuleBean> getRoleModuleList() {
        return roleModuleList;
    }

    /**
     * @param roleModuleList the roleModuleList to set
     */
    public void setRoleModuleList(List<RoleModuleBean> roleModuleList) {
        this.roleModuleList = roleModuleList;
    }

    /**
     * @return the rolesList
     */
    public List<Roles> getRolesList() {
        return rolesList;
    }

    /**
     * @param rolesList the rolesList to set
     */
    public void setRolesList(List<Roles> rolesList) {
        this.rolesList = rolesList;
    }

    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @param roleName the roleName to set
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * @return the selectedRole
     */
    public Roles getSelectedRole() {
        return selectedRole;
    }

    /**
     * @param selectedRole the selectedRole to set
     */
    public void setSelectedRole(Roles selectedRole) {
        this.selectedRole = selectedRole;
    }

    public List<String> getImpactedActiveRoleUsers() {
        return impactedActiveRoleUsers;
    }

    public void setImpactedActiveRoleUsers(List<String> impactedActiveRoleUsers) {
        this.impactedActiveRoleUsers = impactedActiveRoleUsers;
    }

    public int getRolesUsedCount() {
        return rolesUsedCount;
    }

    public void setRolesUsedCount(int rolesUsedCount) {
        this.rolesUsedCount = rolesUsedCount;
    }

    public int getRolesNotUsedCount() {
        return rolesNotUsedCount;
    }

    public void setRolesNotUsedCount(int rolesNotUsedCount) {
        this.rolesNotUsedCount = rolesNotUsedCount;
    }

    private boolean isRoleNameDuplicate() {
        List<Roles> existingRoles = roleAdministrationService.getRolesList();
        if (CollectionUtils.isEmpty(existingRoles)) {
            return false;
        }

        for (Roles role : existingRoles) {
            if (role.getRoleName() != null
                    && role.getRoleName().equalsIgnoreCase(roleName.trim())
                    && (isAddOperation || !role.getId().equals(getSelectedRole().getId()))) {
                return true;
            }
        }
        return false;
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Object session = facesContext.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return null;
        }

        Object organizationId = ((HttpSession) session).getAttribute(SessionAttributes.ORGANIZATION_ID.getName());
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private void notifyActiveOrganizationUsers(Integer organizationId, String message) {
        NotificationService.sendGrowlMessageToOrganization(organizationId, message);
    }

    private String buildRoleChangeNotification(String changedRoleName, String action) {
        String actorUserName = resolveCurrentUserName();
        String formattedDateTime = LocalDateTime.now().format(NOTIFICATION_DATE_TIME_FORMATTER);
        return "Role '" + changedRoleName + "' was " + action + " by " + actorUserName + " on " + formattedDateTime + ".";
    }

    private String resolveCurrentUserName() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return "Unknown user";
        }

        Object session = facesContext.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return "Unknown user";
        }

        Object username = ((HttpSession) session).getAttribute(SessionAttributes.USERNAME.getName());
        return username instanceof String && !((String) username).trim().isEmpty()
                ? ((String) username).trim()
                : "Unknown user";
    }

}


