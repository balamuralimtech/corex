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
package com.module.coretix.commonto;

public class UserActivitiesCountTO {
    private int loginCount;
    private int logoutCount;
    private int addCount;
    private int updateCount;
    private int deleteCount;


    public int getLoginCount() {
        return loginCount;
    }
    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }
    public int getLogoutCount() {
        return logoutCount;
    }
    public void setLogoutCount(int logoutCount) {
        this.logoutCount = logoutCount;
    }
    public int getAddCount() {
        return addCount;
    }
    public void setAddCount(int addCount) {
        this.addCount = addCount;
    }
    public int getUpdateCount() {
        return updateCount;
    }
    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }
    public int getDeleteCount() {
        return deleteCount;
    }
    public void setDeleteCount(int deleteCount) {
        this.deleteCount = deleteCount;
    }
}




