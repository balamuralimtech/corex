/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.web.coretix.usermanagement;

import java.util.List;

/**
 *
 * @author balamurali
 */
public class RoleModuleBean {

    private String module;
    private List<RoleSubModuleBean> roleSubModuleBean;
    
    
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
    public List<RoleSubModuleBean> getRoleSubModuleBean() {
        return roleSubModuleBean;
    }

    /**
     * @param roleSubModuleBean the roleSubModuleBean to set
     */
    public void setRoleSubModuleBean(List<RoleSubModuleBean> roleSubModuleBean) {
        this.roleSubModuleBean = roleSubModuleBean;
    }

}
