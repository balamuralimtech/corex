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
package com.module.coretix.usermanagement.impl;
import com.module.coretix.commonto.UsersStatusCountTO;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.usermanagement.dao.IUserAdministrationDAO;

import com.module.coretix.usermanagement.IUserAdministrationService;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class UserAdministrationService implements IUserAdministrationService {
    
 private static final Logger logger = LoggerFactory.getLogger(UserAdministrationService.class);
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

    public long getUserCount() {
        return getUserDetailDAO().getUserCount();
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
    public void markLoginSuccess(int userId, String sessionId) {
        getUserDetailDAO().markLoginSuccess(userId, sessionId);
    }

    @Transactional(readOnly = false)
    public void markLogout(int userId, int newStatus, String sessionId) {
        getUserDetailDAO().markLogout(userId, newStatus, sessionId);
    }

    @Transactional(readOnly = false)
    public void touchUserSession(int userId, String sessionId) {
        getUserDetailDAO().touchUserSession(userId, sessionId);
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





