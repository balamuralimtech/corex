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




