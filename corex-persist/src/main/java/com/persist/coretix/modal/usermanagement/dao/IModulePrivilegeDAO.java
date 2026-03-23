/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.usermanagement.dao;

import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface IModulePrivilegeDAO {
    
    public void addModulePrivileges(List<ModulePrivileges> modulePrivileges);

    public void updateModulePrivileges(List<ModulePrivileges> modulePrivileges);

    public void deleteModulePrivileges(List<ModulePrivileges> modulePrivileges);
    
    public void deleteAllUserActivities();

    public ModulePrivileges getModulePrivilege(int id);

    public List<ModulePrivileges> getModulePrivilegesList();
    
}

