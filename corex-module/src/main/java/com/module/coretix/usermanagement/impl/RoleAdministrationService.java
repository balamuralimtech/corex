/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author balamurali
 */
@Named
@Transactional(readOnly = true)
public class RoleAdministrationService implements IRoleAdministrationService {
    
    private final Logger logger = Logger.getLogger(getClass());
    
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
