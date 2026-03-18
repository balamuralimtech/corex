/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.usermanagement.impl;

import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import com.persist.coretix.modal.usermanagement.dao.IModulePrivilegeDAO;
import com.module.coretix.usermanagement.IModulePrivilegeService;
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
public class ModulePrivilegeService implements IModulePrivilegeService{
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Inject
    private IModulePrivilegeDAO iModulePrivilegeDAO;

    public void addModulePrivileges(List<ModulePrivileges> organization) {
        getiModulePrivilegeDAO().addModulePrivileges(organization);
    }

    public void updateModulePrivileges(List<ModulePrivileges> organization) {
        getiModulePrivilegeDAO().updateModulePrivileges(organization);
    }

    public void deleteModulePrivileges(List<ModulePrivileges> organization) {
        getiModulePrivilegeDAO().deleteModulePrivileges(organization);
    }
    
    public void deleteAllUserActivities() {
        getiModulePrivilegeDAO().deleteAllUserActivities();
    }

    public ModulePrivileges getModulePrivilegeById(int id) {
        return getiModulePrivilegeDAO().getModulePrivilege(id);
    }

    public List<ModulePrivileges> getModulePrivilegesList() {
        return getiModulePrivilegeDAO().getModulePrivilegesList();
    }

    /**
     * @return the iModulePrivilegeDAO
     */
    public IModulePrivilegeDAO getiModulePrivilegeDAO() {
        return iModulePrivilegeDAO;
    }

    /**
     * @param iModulePrivilegeDAO the iModulePrivilegeDAO to set
     */
    public void setiModulePrivilegeDAO(IModulePrivilegeDAO iModulePrivilegeDAO) {
        this.iModulePrivilegeDAO = iModulePrivilegeDAO;
    }
    
   

    
}
