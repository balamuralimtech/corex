/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.usermanagement.impl;
import com.module.coretix.commonto.UsersStatusCountTO;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.usermanagement.dao.IUserAdministrationDAO;

import com.module.coretix.usermanagement.IUserAdministrationService;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class UserAdministrationService implements IUserAdministrationService {
    
 private final Logger logger = Logger.getLogger(getClass());
    @Inject
    private IUserAdministrationDAO userAdministrationDAO;

    @Transactional(readOnly = false)
    public void addUserDetail(UserDetails userDetail) {
        logger.debug("inside CountryService addCountry");
        getUserDetailDAO().addUserDetail(userDetail);
    }

    @Transactional(readOnly = false)
    public void deleteUserDetail(UserDetails userDetail) {
        getUserDetailDAO().deleteUserDetail(userDetail);
    }

    @Transactional(readOnly = false)
    public void updateUserDetail(UserDetails userDetail) {
        getUserDetailDAO().updateUserDetail(userDetail);
    }

    public UserDetails getUserDetailById(int id) {
        return getUserDetailDAO().getUserDetail(id);
    }
    
    public UserDetails getUserDetailEntityByUserName(String userName) {
        return getUserDetailDAO().getUserDetailEntityByUserName(userName);
    }

    public List<UserDetails> getUserDetailsList() {
        return getUserDetailDAO().getUserDetailsList();
    }

    @Transactional(readOnly = false)
    public void updateUserPassword(int userId, String newPassword){
        getUserDetailDAO().updateUserPassword(userId, newPassword);
    }

    public boolean isUserValid(String username, String password){
        return getUserDetailDAO().isUserValid(username, password);
    }

    @Transactional(readOnly = false)
    public void updateUserStatus(int userId, int newStatus){
        getUserDetailDAO().updateUserStatus(userId, newStatus);
    }

    @Transactional(readOnly = false)
    public UsersStatusCountTO populateUsersStatusCount()
    {
        UsersStatusCountTO usersStatusCountTO = new UsersStatusCountTO();
        usersStatusCountTO.setUsersLoggedInCount(getUserDetailDAO().getCountOfUsersLoggedIn());
        usersStatusCountTO.setUsersLoggedOutCount(getUserDetailDAO().getCountOfUsersLoggedOut());
        usersStatusCountTO.setUsersNeverLoggedInCount(getUserDetailDAO().getCountOfUsersNeverLoggedIn());

        return usersStatusCountTO;
    }

    /**
     * @return the userAdministrationDAO
     */
    public IUserAdministrationDAO getUserDetailDAO() {
        return userAdministrationDAO;
    }

    /**
     * @param userAdministrationDAO the userAdministrationDAO to set
     */
    public void setUserAdministrationDAO(IUserAdministrationDAO userAdministrationDAO) {
        this.userAdministrationDAO = userAdministrationDAO;
    }

}

