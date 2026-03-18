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
