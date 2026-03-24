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



