package com.web.coretix.usermanagement;

import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import com.module.coretix.usermanagement.IModulePrivilegeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import javax.inject.Named;

import com.web.coretix.constants.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import org.springframework.context.annotation.Scope;

/**
 *
 * Entity Backed Bean
 *
 * @author Miquel Millan
 * @version 1.0.0
 *
 */
@Named("modulesRolePrivilegeDefinitionBean")
@Scope("session")
public class ModulesRolePrivilegeDefinitionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Logger logger = Logger.getLogger(getClass());
    private List<RoleModuleBean> roleModuleList = new ArrayList<>();

    private boolean isDataAvailable;
    private boolean disableTabPanel;

    private final String viewPrivilegeValue = RolePrivilegeConstants.VIEW.getValue();

    @Inject
    private IModulePrivilegeService iModulePrivilegeService;

    public void initializePageAttributes() {
        if (CollectionUtils.isNotEmpty(roleModuleList)) {
            roleModuleList.clear();
        }
        isDataAvailable = false;
        disableTabPanel = false;
        populateRoleDefintionDBList();

    }

    public void editButtonAction() {
        logger.debug("inside editButtonAction");
        disableTabPanel = false;
    }

    public void deleteButtonAction() {
        logger.debug("inside deleteButtonAction");
        iModulePrivilegeService.deleteAllUserActivities();
        initializePageAttributes();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Definition Removed !"));
    }

    private void populateRoleDefintionDBList() {
        isDataAvailable = false;
        disableTabPanel = false;

        List<ModulePrivileges> modulePrivileges = iModulePrivilegeService.getModulePrivilegesList();

        if (CollectionUtils.isNotEmpty(modulePrivileges)) {
            isDataAvailable = true;
            disableTabPanel = true;
            logger.debug("DB List is not empty ! Populating DB Values ");
            populateDBModulePrivilegesList(modulePrivileges);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Definition available !"));
        } else {
            logger.debug("DB List is  empty ! Populating UI Values to save");
            populateUIModulePrivilegesList();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Definition not available !"));
        }
    }

    private void populateDBModulePrivilegesList(List<ModulePrivileges> modulePrivileges) {
        Map<String, Map<String, List<String>>> outerMap = new LinkedHashMap<>();

        for (ModulePrivileges modulePrivilege : modulePrivileges) {

            if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.USER_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + UserManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());

                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(UserManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(UserManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.SYSTEM_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + SystemManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(SystemManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(SystemManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.LICENCE.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + LicenseManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(LicenseManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(LicenseManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.SERVER_AND_DB.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + ServerAndDBModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(ServerAndDBModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ServerAndDBModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.CLIENT_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + ClientManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(ClientManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(ClientManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.INVENTORY_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + InventoryManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(InventoryManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(InventoryManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.QUOTE_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + QuoteManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
                innerMap.putIfAbsent(QuoteManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue(), new ArrayList<>());
                List<String> privilegeList = innerMap.get(QuoteManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue());
                if (privilegeList != null) {
                    privilegeList.add(modulePrivilege.getId() + "/" + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                }
            } else if (AppModule.getById(modulePrivilege.getModuleId()).getValue().equalsIgnoreCase(AppModule.SHIPMENT_MANAGEMENT.getValue())) {
                logger.debug("I" + modulePrivilege.getId() + " M : " + AppModule.getById(modulePrivilege.getModuleId()).getValue()
                        + " S : " + ShipmentManagementModule.getById(modulePrivilege.getSubmoduleId()).getValue()
                        + " P : " + RolePrivilegeConstants.getById(modulePrivilege.getPrivilegeId()).getValue());
                outerMap.putIfAbsent(AppModule.getById(modulePrivilege.getModuleId()).getValue(), new LinkedHashMap<>());
                Map<String, List<String>> innerMap = outerMap.get(AppModule.getById(modulePrivilege.getModuleId()).getValue());
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
                List<String> selectedRolePrivilegeBeanList = new ArrayList<>();
                for (String string : innerEntry.getValue()) {
                    String[] parts = string.split("/");
                    selectedRolePrivilegeBeanList.add(parts[1]);
                }

                roleSubModuleBean.setSelectedPrivilegeList(selectedRolePrivilegeBeanList);

                rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                roleSubModuleBeanList.add(roleSubModuleBean);
            }
            bean.setRoleSubModuleBean(roleSubModuleBeanList);
            roleModuleList.add(bean);
        }
    }

    private void populateUIModulePrivilegesList() {
        List<String> defViewPrivilegeBeanList = new ArrayList<>();
        defViewPrivilegeBeanList.add(getViewPrivilegeValue());
        for (String module : AppModule.getAllValues()) {
            RoleModuleBean bean = new RoleModuleBean();
            bean.setModule(module);
            logger.debug(" module : " + module);
            if (module.equalsIgnoreCase(AppModule.USER_MANAGEMENT.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.USER_MANAGEMENT.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : UserManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();

                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.SYSTEM_MANAGEMENT.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.SYSTEM_MANAGEMENT.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : SystemManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.LICENCE.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.LICENCE.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : LicenseManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.SERVER_AND_DB.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.LICENCE.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : ServerAndDBModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.CLIENT_MANAGEMENT.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.LICENCE.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : ClientManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.INVENTORY_MANAGEMENT.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.LICENCE.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : InventoryManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.QUOTE_MANAGEMENT.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.LICENCE.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : QuoteManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            } else if (module.equalsIgnoreCase(AppModule.SHIPMENT_MANAGEMENT.getValue())) {
                logger.debug(" if (module.equalsIgnoreCase(AppModule.LICENCE.name())) {");
                List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
                for (String subModule : ShipmentManagementModule.getAllValues()) {
                    RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                    roleSubModuleBean.setSubModule(subModule);
                    List<String> rolePrivilegeBeanList = new ArrayList<>();
                    roleSubModuleBean.setSelectedPrivilegeList(defViewPrivilegeBeanList);
                    rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());

                    roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                    roleSubModuleBeanList.add(roleSubModuleBean);
                }

                bean.setRoleSubModuleBean(roleSubModuleBeanList);
            }
            roleModuleList.add(bean);
        }
    }

    public void saveRoleDefinition() {
        logger.debug("inside saveRoleDefinition !!!!");

        List<ModulePrivileges> roleModuleIdList = new ArrayList<>();

        for (RoleModuleBean roleModuleBean : getRoleModuleList()) {

            logger.debug("roleModuleBean getModule : " + roleModuleBean.getModule());

            for (RoleSubModuleBean roleSubModuleBean : roleModuleBean.getRoleSubModuleBean()) {

                logger.debug("roleModuleBean getSubModule : " + roleSubModuleBean.getSubModule());
                logger.debug("roleSubModuleBean.getSelectedPrivilegeList() : " + roleSubModuleBean.getSelectedPrivilegeList());

                for (String privilege : roleSubModuleBean.getSelectedPrivilegeList()) {
                    ModulePrivileges modulePrivileges = new ModulePrivileges();

                    modulePrivileges.setModuleId(AppModule.getByValue(roleModuleBean.getModule()).getId());

                    if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.USER_MANAGEMENT.getValue())) {
                        modulePrivileges.setSubmoduleId(UserManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    } else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.SYSTEM_MANAGEMENT.getValue())) {
                        modulePrivileges.setSubmoduleId(SystemManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    } else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.LICENCE.getValue())) {
                        modulePrivileges.setSubmoduleId(LicenseManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    } else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.SERVER_AND_DB.getValue())) {
                        modulePrivileges.setSubmoduleId(ServerAndDBModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.CLIENT_MANAGEMENT.getValue())) {
                        modulePrivileges.setSubmoduleId(ClientManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.INVENTORY_MANAGEMENT.getValue())) {
                        modulePrivileges.setSubmoduleId(InventoryManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.QUOTE_MANAGEMENT.getValue())) {
                        modulePrivileges.setSubmoduleId(QuoteManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }else if (roleModuleBean.getModule().equalsIgnoreCase(AppModule.SHIPMENT_MANAGEMENT.getValue())) {
                        modulePrivileges.setSubmoduleId(ShipmentManagementModule.getByValue(roleSubModuleBean.getSubModule()).getId());
                    }

                    modulePrivileges.setPrivilegeId(RolePrivilegeConstants.getByValue(privilege).getId());

                    roleModuleIdList.add(modulePrivileges);
                }
            }
        }

        if (isDataAvailable) {
            iModulePrivilegeService.deleteAllUserActivities();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Exiting Role Definition Removed"));
            iModulePrivilegeService.addModulePrivileges(roleModuleIdList);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Definition Updated !"));
        } else {
            iModulePrivilegeService.addModulePrivileges(roleModuleIdList);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Role Definition Updated !"));
        }

        populateRoleDefintionDBList();

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
     * @return the viewPrivilegeValue
     */
    public String getViewPrivilegeValue() {
        return viewPrivilegeValue;
    }

    /**
     * @return the isDataAvailable
     */
    public boolean isIsDataAvailable() {
        return isDataAvailable;
    }

    /**
     * @param isDataAvailable the isDataAvailable to set
     */
    public void setIsDataAvailable(boolean isDataAvailable) {
        this.isDataAvailable = isDataAvailable;
    }

    /**
     * @return the disableTabPanel
     */
    public boolean isDisableTabPanel() {
        return disableTabPanel;
    }

    /**
     * @param disableTabPanel the disableTabPanel to set
     */
    public void setDisableTabPanel(boolean disableTabPanel) {
        this.disableTabPanel = disableTabPanel;
    }

}
