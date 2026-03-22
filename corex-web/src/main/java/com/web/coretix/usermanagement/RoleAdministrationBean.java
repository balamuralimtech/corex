/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.web.coretix.usermanagement;

import com.module.coretix.commonto.RoleUsageCountTO;
import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.persist.coretix.modal.usermanagement.Roles;
import com.module.coretix.usermanagement.IModulePrivilegeService;
import com.module.coretix.usermanagement.IRoleAdministrationService;
import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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

    private String roleName;
    private List<Roles> rolesList = new ArrayList<>();

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    private int rolesUsedCount;
    private int rolesNotUsedCount;

    private Roles selectedRole = new Roles();

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

    private void populateRoleDefintionDBList() {

        List<ModulePrivileges> modulePrivileges = modulePrivilegeService.getModulePrivilegesList();

        populateDBModulePrivilegesList(modulePrivileges);

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
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.CLIENT_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + ClientManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(ClientManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ClientManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.INVENTORY_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + InventoryManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(InventoryManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(InventoryManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.QUOTE_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + QuoteManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(QuoteManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(QuoteManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.SHIPMENT_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + CoreAppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + ShipmentManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(ShipmentManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ShipmentManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
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
                List<String> rolePrivilegeBeanList = new ArrayList<>();
                List<String> availablePrivilegeList = new ArrayList<>();
                for (String string : innerEntry.getValue()) {
                    String[] parts = string.split("/");
                    availablePrivilegeList.add(parts[1]);
                }
                logger.debug("availablePrivilegeList : " + availablePrivilegeList);
                rolePrivilegeBeanList.addAll(availablePrivilegeList);

                roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                roleSubModuleBeanList.add(roleSubModuleBean);
            }
            bean.setRoleSubModuleBean(roleSubModuleBeanList);
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
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.CLIENT_MANAGEMENT.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + ClientManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(ClientManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ClientManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(privileges.getId() + "/" + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.INVENTORY_MANAGEMENT.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + InventoryManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(InventoryManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(InventoryManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(privileges.getId() + "/" + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.QUOTE_MANAGEMENT.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + QuoteManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(QuoteManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(QuoteManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(privileges.getId() + "/" + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                }
            } else if (CoreAppModule.getById(privileges.getModuleId()).getValue().equalsIgnoreCase(CoreAppModule.SHIPMENT_MANAGEMENT.getValue())) {
                logger.debug("I" + privileges.getId() + " M : " + CoreAppModule.getById(privileges.getModuleId()).getValue()
                        + " S : " + ShipmentManagementModule.getById(privileges.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(CoreAppModule.getById(privileges.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(CoreAppModule.getById(privileges.getModuleId()).getValue());
                innerMap.putIfAbsent(ShipmentManagementModule.getById(privileges.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ShipmentManagementModule.getById(privileges.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(privileges.getId() + "/" + RolePrivilegeConstants.getById(privileges.getPrivilegeId()).getValue());
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
                List<String> rolePrivilegeBeanList = new ArrayList<>();
                List<String> selectedPrivilegeList = new ArrayList<>();
                for (String string : innerEntry.getValue()) {
                    String[] parts = string.split("/");
                    if(Boolean.parseBoolean(parts[1]))
                    {
                        selectedPrivilegeList.add(parts[0]);
                    }
                    rolePrivilegeBeanList.add(parts[0]);
                }

                logger.debug("selectedPrivilegeList : "+selectedPrivilegeList);
                logger.debug("rolePrivilegeBeanList : "+rolePrivilegeBeanList);
                
                roleSubModuleBean.setSelectedPrivilegeList(selectedPrivilegeList);
                roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                roleSubModuleBeanList.add(roleSubModuleBean);
            }
            bean.setRoleSubModuleBean(roleSubModuleBeanList);
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
        populateRoleDefintionDBList();
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
        editOrganization();
    }

    private void editOrganization() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedOrganization.getId() : " + getSelectedRole().getId());

        roleName = getSelectedRole().getRoleName();

        populateEditDBModulePrivilegesList(selectedRole.getRolePrivileges());

        logger.debug("roleName : " + roleName);
    }

    public void saveRole() {

        logger.debug("Inside save organization method ");
        logger.debug("roleName : " + roleName);
        logger.debug("isAddOperation : " + isAddOperation);

        Roles role = new Roles();
        role.setRoleName(roleName);
        List<RolePrivileges> rolePrivilegesList = new ArrayList<>();
        for (RoleModuleBean roleModuleBean : roleModuleList) {

            logger.debug("roleModuleBean getModule : " + roleModuleBean.getModule());

            for (RoleSubModuleBean roleSubModuleBean : roleModuleBean.getRoleSubModuleBean()) {

                logger.debug("roleModuleBean getSubModule : " + roleSubModuleBean.getSubModule());
                logger.debug("roleSubModuleBean.getRolePrivilegeBeanList() : " + roleSubModuleBean.getRolePrivilegeBeanList());
                logger.debug("roleSubModuleBean.getSelectedPrivilegeList() : " + roleSubModuleBean.getSelectedPrivilegeList());

                // List to hold the results
                List<String> isSelectedRolePrivilegeBeanList = new ArrayList<>();

                // Iterate over list1 and check if each element is present in list2
                for (String item1 : roleSubModuleBean.getRolePrivilegeBeanList()) {
                    boolean isMatched = false;

                    // Check if item1 is in list2
                    for (String item2 : roleSubModuleBean.getSelectedPrivilegeList()) {
                        if (item1.equals(item2)) {
                            isMatched = true;
                            break; // break as soon as a match is found
                        }
                    }
                    // Append true/false to the results list based on whether a match was found
                    isSelectedRolePrivilegeBeanList.add(item1 + "/" + isMatched);
                }

                // Output the result
                logger.debug("Final db list: " + isSelectedRolePrivilegeBeanList);

                for (String privilege : isSelectedRolePrivilegeBeanList) {
                    RolePrivileges rolePrivileges = new RolePrivileges();
                    rolePrivileges.setRoles(role);
                    rolePrivileges.setModuleId(CoreAppModule.getByValue(roleModuleBean.getModule()).getId());

                    if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.USER_MANAGEMENT.getValue())) {
                        rolePrivileges.setSubmoduleId(UserManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    } else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.SYSTEM_MANAGEMENT.getValue())) {
                        rolePrivileges.setSubmoduleId(SystemManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    } else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.LICENCE.getValue())) {
                        rolePrivileges.setSubmoduleId(LicenseManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    } else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.SERVER_AND_DB.getValue())) {
                        rolePrivileges.setSubmoduleId(ServerAndDBModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.CLIENT_MANAGEMENT.getValue())) {
                        rolePrivileges.setSubmoduleId(ClientManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.INVENTORY_MANAGEMENT.getValue())) {
                        rolePrivileges.setSubmoduleId(InventoryManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.QUOTE_MANAGEMENT.getValue())) {
                        rolePrivileges.setSubmoduleId(QuoteManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(CoreAppModule.SHIPMENT_MANAGEMENT.getValue())) {
                        rolePrivileges.setSubmoduleId(ShipmentManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }
                    String[] parts = privilege.split("/");
                    
                    rolePrivileges.setIsSelected(Boolean.valueOf(parts[1]));
                    rolePrivileges.setPrivilegeId(RolePrivilegeConstants.getByValue(parts[0]).getId());
                    rolePrivilegesList.add(rolePrivileges);
                }
            }
        }

        role.setRolePrivileges(rolePrivilegesList);

        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");

            roleAdministrationService.addRole(role);

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Added"));
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedOrganization.getId() : " + getSelectedRole().getId());
            role.setId(getSelectedRole().getId());
            roleAdministrationService.updateRole(getSelectedRole());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Updated"));
        }

        fetchRolesList();
        PrimeFaces.current().executeScript("PF('manageOrgDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:datatablePanelId");
        logger.debug("End of save action !!!!");
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
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Removed"));
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

}
