/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
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
 * Project: `app.name`
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




