package com.module.coretix.commonto;

public class UsersStatusCountTO {
    private int usersLoggedInCount;
    private int usersLoggedOutCount;
    private int usersNeverLoggedInCount;


    public int getUsersLoggedInCount() {
        return usersLoggedInCount;
    }

    public void setUsersLoggedInCount(int usersLoggedInCount) {
        this.usersLoggedInCount = usersLoggedInCount;
    }

    public int getUsersLoggedOutCount() {
        return usersLoggedOutCount;
    }

    public void setUsersLoggedOutCount(int usersLoggedOutCount) {
        this.usersLoggedOutCount = usersLoggedOutCount;
    }

    public int getUsersNeverLoggedInCount() {
        return usersNeverLoggedInCount;
    }

    public void setUsersNeverLoggedInCount(int usersNeverLoggedInCount) {
        this.usersNeverLoggedInCount = usersNeverLoggedInCount;
    }
}
