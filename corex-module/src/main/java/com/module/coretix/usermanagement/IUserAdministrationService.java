/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.usermanagement;

import com.module.coretix.commonto.UsersStatusCountTO;
import com.persist.coretix.modal.usermanagement.UserDetails;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IUserAdministrationService {
    

    public void addUserDetail(UserDetails userdetail);

    public void updateUserDetail(UserDetails userdetail);

    public void deleteUserDetail(UserDetails userdetail);

    public UserDetails getUserDetailById(int id);
    
    public UserDetails getUserDetailEntityByUserName(String userName);

    public List<UserDetails> getUserDetailsList();

    public void updateUserPassword(int userId, String newPassword);

    public boolean isUserValid(String username, String password);

    public void updateUserStatus(int userId, int newStatus);

    public UsersStatusCountTO populateUsersStatusCount();
}