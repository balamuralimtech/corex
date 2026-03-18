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
public class RoleSubModuleBean {

    private String subModule;
    private List<String> rolePrivilegeBeanList;
    private List<String> selectedPrivilegeList;
    /**
     * @return the subModule
     */
    public String getSubModule() {
        return subModule;
    }

    /**
     * @param subModule the subModule to set
     */
    public void setSubModule(String subModule) {
        this.subModule = subModule;
    }

    /**
     * @return the rolePrivilegeBeanList
     */
    public List<String> getRolePrivilegeBeanList() {
        return rolePrivilegeBeanList;
    }

    /**
     * @param rolePrivilegeBeanList the rolePrivilegeBeanList to set
     */
    public void setRolePrivilegeBeanList(List<String> rolePrivilegeBeanList) {
        this.rolePrivilegeBeanList = rolePrivilegeBeanList;
    }

    /**
     * @return the selectedPrivilegeList
     */
    public List<String> getSelectedPrivilegeList() {
        return selectedPrivilegeList;
    }

    /**
     * @param selectedPrivilegeList the selectedPrivilegeList to set
     */
    public void setSelectedPrivilegeList(List<String> selectedPrivilegeList) {
        this.selectedPrivilegeList = selectedPrivilegeList;
    }

}
