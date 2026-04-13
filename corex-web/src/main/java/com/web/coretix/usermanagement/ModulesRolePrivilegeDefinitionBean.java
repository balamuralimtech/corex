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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ModulesRolePrivilegeDefinitionBean.class);
    private List<RoleModuleBean> roleModuleList = new ArrayList<>();

    private boolean isDataAvailable;
    private boolean disableTabPanel;

    private final String viewPrivilegeValue = RolePrivilegeConstants.VIEW.getValue();

    @Inject
    private transient IModulePrivilegeService iModulePrivilegeService;

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
            bean.setRoleSubModuleBeanList(roleSubModuleBeanList);
            roleModuleList.add(bean);
        }
    }

    private void populateUIModulePrivilegesList() {
        List<String> defViewPrivilegeBeanList = new ArrayList<>();
        defViewPrivilegeBeanList.add(getViewPrivilegeValue());
        for (CoreAppModule module : RoleModuleCatalog.getRoleModules()) {
            RoleModuleBean bean = new RoleModuleBean();
            bean.setModule(module.getValue());
            logger.debug(" module : " + module.getValue());
            List<RoleSubModuleBean> roleSubModuleBeanList = new ArrayList<>();
            for (String subModule : RoleModuleCatalog.getSubmoduleValues(module)) {
                RoleSubModuleBean roleSubModuleBean = new RoleSubModuleBean();
                roleSubModuleBean.setSubModule(subModule);
                List<String> rolePrivilegeBeanList = new ArrayList<>();
                roleSubModuleBean.setSelectedPrivilegeList(new ArrayList<>(defViewPrivilegeBeanList));
                rolePrivilegeBeanList.addAll(RolePrivilegeConstants.getAllValues());
                roleSubModuleBean.setRolePrivilegeBeanList(rolePrivilegeBeanList);
                roleSubModuleBeanList.add(roleSubModuleBean);
            }
            bean.setRoleSubModuleBeanList(roleSubModuleBeanList);
            roleModuleList.add(bean);
        }
    }

    public void saveRoleDefinition() {
        logger.debug("inside saveRoleDefinition !!!!");

        List<ModulePrivileges> roleModuleIdList = new ArrayList<>();

        for (RoleModuleBean roleModuleBean : getRoleModuleList()) {

            logger.debug("roleModuleBean getModule : " + roleModuleBean.getModule());

            for (RoleSubModuleBean roleSubModuleBean : roleModuleBean.getRoleSubModuleBeanList()) {

                logger.debug("roleModuleBean getSubModule : " + roleSubModuleBean.getSubModule());
                logger.debug("roleSubModuleBean.getSelectedPrivilegeList() : " + roleSubModuleBean.getSelectedPrivilegeList());

                for (String privilege : roleSubModuleBean.getSelectedPrivilegeList()) {
                    ModulePrivileges modulePrivileges = new ModulePrivileges();

                    modulePrivileges.setModuleId(CoreAppModule.getByValue(roleModuleBean.getModule()).getId());
                    modulePrivileges.setSubmoduleId(RoleModuleCatalog.resolveSubmoduleId(
                            roleModuleBean.getModule(), roleSubModuleBean.getSubModule()));

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



