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

import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import com.persist.coretix.modal.usermanagement.dao.IModulePrivilegeDAO;
import com.module.coretix.usermanagement.IModulePrivilegeService;
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
public class ModulePrivilegeService implements IModulePrivilegeService{
    
    private static final Logger logger = LoggerFactory.getLogger(ModulePrivilegeService.class);
    
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




