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
package com.module.coretix.usermanagement.impl;

import com.module.coretix.commonto.RoleUsageCountTO;
import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.persist.coretix.modal.usermanagement.Roles;
import com.persist.coretix.modal.usermanagement.dao.IRoleAdministrationDAO;
import com.module.coretix.usermanagement.IRoleAdministrationService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author balamurali
 */
@Named
@Transactional(readOnly = true)
public class RoleAdministrationService implements IRoleAdministrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleAdministrationService.class);
    
    @Inject
    private IRoleAdministrationDAO roleAdministrationDAO;

    @Transactional(readOnly = false)
    public void addRole(Roles role) {
        getRoleAdministrationDAO().addRole(role);
    }

    @Transactional(readOnly = false)
    public void updateRole(Roles role) {
        getRoleAdministrationDAO().updateRole(role);
    }

    @Transactional(readOnly = false)
    public void deleteRole(Roles role) {
        getRoleAdministrationDAO().deleteRole(role);
    }

    public Roles getRoleById(int id) {
        return getRoleAdministrationDAO().getRole(id);
    }

    public List<RolePrivileges> getRolePrivilegesByModuleAndSubModule(int roleId, int moduleId, int subModuleId){
        return getRoleAdministrationDAO().getRolePrivilegesByModuleAndSubModule(roleId, moduleId, subModuleId);
    }

    public List<Integer> getModulesByRoleId(int roleId) {
        return getRoleAdministrationDAO().getModulesByRoleId(roleId);
    }

    public List<Integer> getSubmodulesByRoleandModuleId(int roleId, int moduleId){
        return getRoleAdministrationDAO().getSubmodulesByRoleandModuleId(roleId, moduleId);
    }

    public List<Roles> getRolesList() {
        return getRoleAdministrationDAO().getRolesList();
    }
    
    public Roles getRoleEntityByRoleName(String roleName){
        return getRoleAdministrationDAO().getRoleEntityByRoleName(roleName);
    }

    public RoleUsageCountTO getCountOfRolesUsedAndNotUsed() {
        RoleUsageCountTO roleUsageCountTO = new RoleUsageCountTO();
        roleUsageCountTO.setRoleUsedCount(getRoleAdministrationDAO().getCountOfRolesUsedAndNotUsed().get("usedRoles"));
        roleUsageCountTO.setRoleUnusedCount(getRoleAdministrationDAO().getCountOfRolesUsedAndNotUsed().get("notUsedRoles"));
        return roleUsageCountTO;
    }

    /**
     * @return the roleAdministrationDAO
     */
    public IRoleAdministrationDAO getRoleAdministrationDAO() {
        return roleAdministrationDAO;
    }

    /**
     * @param roleAdministrationDAO the roleAdministrationDAO to set
     */
    public void setRoleAdministrationDAO(IRoleAdministrationDAO roleAdministrationDAO) {
        this.roleAdministrationDAO = roleAdministrationDAO;
    }

    
}




