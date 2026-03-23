/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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

import java.io.Serializable;
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
import org.apache.log4j.Logger;
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
    private final Logger logger = Logger.getLogger(getClass());

    private List<RoleModuleBean> roleModuleList = new ArrayList<>();

    private final String viewPrivilegeValue = RolePrivilegeConstants.VIEW.getValue();

    @Inject
    private IModulePrivilegeService modulePrivilegeService;

    @Inject
    private IRoleAdministrationService roleAdministrationService;

    @Inject
    private IUserAdministrationService userAdministrationService;

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

        roleModuleList.add(buildRoleModuleBean(CoreAppModule.USER_MANAGEMENT.getValue(),
                UserManagementModule.getAllValues()));
        roleModuleList.add(buildRoleModuleBean(CoreAppModule.SYSTEM_MANAGEMENT.getValue(),
                SystemManagementModule.getAllValues()));
        roleModuleList.add(buildRoleModuleBean(CoreAppModule.LICENCE.getValue(),
                LicenseManagementModule.getAllValues()));
        roleModuleList.add(buildRoleModuleBean(CoreAppModule.SERVER_AND_DB.getValue(),
                ServerAndDBModule.getAllValues()));

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

        logger.debug("roleSubModuleBean.getSubModule() : " + roleSubModuleBean.getSubModule());
        logger.debug("roleSubModuleBean.getRolePrivilegeBeanList() size : "
                + roleSubModuleBean.getRolePrivilegeBeanList().size());

        return roleSubModuleBean;
    }

    private void populateDBModulePrivilegesList(List<ModulePrivileges> modulePrivileges) {

        logger.debug("entered into populateDBModulePrivilegesList !!!");
        Map<String, Map<String, List<String>>> outerMap = new LinkedHashMap<>();

        for (ModulePrivileges modulePrivilege : modulePrivileges) {

            if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.USER_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + UserManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());

                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(UserManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(UserManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.SYSTEM_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + SystemManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(SystemManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(SystemManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.LICENCE.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + LicenseManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(LicenseManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(LicenseManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.SERVER_AND_DB.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + ServerAndDBModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(ServerAndDBModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ServerAndDBModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
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

            if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.USER_MANAGEMENT.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + UserManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+" Is : "+privileges.getIsSelected());

                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(UserManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(UserManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+"/"+privileges.getIsSelected());
                }
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.SYSTEM_MANAGEMENT.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + SystemManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+" Is : "+privileges.getIsSelected());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(SystemManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(SystemManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+"/"+privileges.getIsSelected());
                }
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.LICENCE.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + LicenseManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+" Is : "+privileges.getIsSelected());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(LicenseManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(LicenseManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+"/"+privileges.getIsSelected());
                }
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.SERVER_AND_DB.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + ServerAndDBModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+" Is : "+privileges.getIsSelected());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(ServerAndDBModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ServerAndDBModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue()+"/"+privileges.getIsSelected());
                }
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
        if (CollectionUtils.isNotEmpty(roleModuleList)) {
            logger.debug("inside  organizationList clear");
            roleModuleList.clear();
        }
        populateRoleDefintionNewAddList();
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
        if (moduleName.equalsIgnoreCase(CoreAppModule.USER_MANAGEMENT.getValue())) {
            return UserManagementModule.getByValue(subModuleName).getId();
        } else if (moduleName.equalsIgnoreCase(CoreAppModule.SYSTEM_MANAGEMENT.getValue())) {
            return SystemManagementModule.getByValue(subModuleName).getId();
        } else if (moduleName.equalsIgnoreCase(CoreAppModule.LICENCE.getValue())) {
            return LicenseManagementModule.getByValue(subModuleName).getId();
        } else if (moduleName.equalsIgnoreCase(CoreAppModule.SERVER_AND_DB.getValue())) {
            return ServerAndDBModule.getByValue(subModuleName).getId();
        }

        throw new IllegalArgumentException("Unsupported module name: " + moduleName);
    }

    private void persistRoleChanges(boolean forceLogoutActiveUsers) {
        Roles role = new Roles();
        role.setRoleName(roleName.trim());
        role.setRolePrivileges(buildRolePrivileges(role));
        if (isAddOperation) {
            logger.debug("if (isAddOperation) {");
            roleAdministrationService.addRole(role);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role added successfully"));
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedRole.getId() : " + getSelectedRole().getId());
            role.setId(getSelectedRole().getId());

            if (forceLogoutActiveUsers) {
                notifyUsersForRoleUpdate(role.getId());
            }

            roleAdministrationService.updateRole(role);
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

        logger.debug("roleName : " + roleName);

        roleAdministrationService.deleteRole(getSelectedRole());
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

}
