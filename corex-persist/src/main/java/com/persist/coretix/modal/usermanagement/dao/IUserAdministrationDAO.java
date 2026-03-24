/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
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

    public void updateUserPassword(int userId, String newPassword);

    public boolean isUserValid(String username, String password);

    public int getCountOfUsersLoggedOut();

    public int getCountOfUsersLoggedIn();

    public int getCountOfUsersNeverLoggedIn();

    public void updateUserStatus(int userId, int newStatus);

    public void markLoginSuccess(int userId, String sessionId);

    public void markLogout(int userId, int newStatus, String sessionId);

    public void touchUserSession(int userId, String sessionId);
}

