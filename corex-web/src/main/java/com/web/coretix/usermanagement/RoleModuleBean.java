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
package com.web.coretix.usermanagement;

import java.util.List;

/**
 *
 * @author balamurali
 */
public class RoleModuleBean {

    private String module;
    private List<RoleSubModuleBean> roleSubModuleBeanList;
    
    
    /**
     * @return the module
     */
    public String getModule() {
        return module;
    }

    /**
     * @param module the module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * @return the roleSubModuleBean
     */
    public List<RoleSubModuleBean> getRoleSubModuleBeanList() {
        return roleSubModuleBeanList;
    }

    /**
     * @param roleSubModuleBeanList the roleSubModuleBean to set
     */
    public void setRoleSubModuleBeanList(List<RoleSubModuleBean> roleSubModuleBeanList) {
        this.roleSubModuleBeanList = roleSubModuleBeanList;
    }

}



