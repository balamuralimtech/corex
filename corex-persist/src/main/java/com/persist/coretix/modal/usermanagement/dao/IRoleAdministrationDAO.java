/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.usermanagement.dao;

import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.persist.coretix.modal.usermanagement.Roles;
import java.util.List;
import java.util.Map;

/**
 *
 * @author balamurali
 */
public interface IRoleAdministrationDAO {

    public void addRole(Roles role);

    public void updateRole(Roles role);

    public void deleteRole(Roles role);

    public Roles getRole(int id);

    public List<RolePrivileges> getRolePrivilegesByModuleAndSubModule(int roleId, int moduleId, int subModuleId);
    
    public Roles getRoleEntityByRoleName(String roleName);

    public List<Roles> getRolesList();

    public Map<String, Integer> getCountOfRolesUsedAndNotUsed();

    public List<Integer> getModulesByRoleId(int roleId);

    public List<Integer> getSubmodulesByRoleandModuleId(int roleId, int moduleId);
}
