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
package com.persist.coretix.modal.usermanagement.dao;
import com.persist.coretix.modal.usermanagement.UserDetails;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IUserAdministrationDAO {
    
    public void addUserDetail(UserDetails userDetail);

    public void updateUserDetail(UserDetails userDetail);

    public void deleteUserDetail(UserDetails userDetail);

    public UserDetails getUserDetail(int id);
    
    public UserDetails getUserDetailEntityByUserName(String userName);

    public List<UserDetails> getUserDetailsList();

    public long getUserCount();

    public void updateUserPassword(int userId, String newPassword);

    public void updateUserAccountControls(int userId, boolean accountDisabled, boolean accountLocked);

    public boolean isUserValid(String username, String password);

    /**
     * Optimized method to get user counts by status in a single query
     * @return Map with status as key and count as value
     */
    public java.util.Map<Integer, Long> getUserCountsByStatus();

    public int getCountOfUsersLoggedOut();

    public int getCountOfUsersLoggedIn();

    public int getCountOfUsersNeverLoggedIn();

    public void updateUserStatus(int userId, int newStatus);

    public void markLoginSuccess(int userId, String sessionId);

    public void markLogout(int userId, int newStatus, String sessionId);

    public void touchUserSession(int userId, String sessionId);
}



