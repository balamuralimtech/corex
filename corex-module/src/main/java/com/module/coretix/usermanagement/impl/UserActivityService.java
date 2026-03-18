/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.usermanagement.impl;

import com.module.coretix.commonto.UserActivitiesCountTO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
import com.module.coretix.usermanagement.IUserActivityService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class UserActivityService  implements IUserActivityService{
    
        private final Logger logger = Logger.getLogger(getClass());
   @Inject
    private IUserActivityDAO userActivityDAO;
   
    public List<UserActivities> getUserActivitiesList() {
        return getUserActivityDAO().getUserActivitiesList();
    }
        public void addUserActivity(UserActivities userActivity) {
        getUserActivityDAO().addUserActivity(userActivity);
    }

    public UserActivitiesCountTO getActivityTypeCounts(){
        UserActivitiesCountTO userActivitiesCountTO = new UserActivitiesCountTO();
        userActivitiesCountTO.setLoginCount(getUserActivityDAO().getActivityTypeCounts().get("login"));
        userActivitiesCountTO.setLogoutCount(getUserActivityDAO().getActivityTypeCounts().get("logout"));
        userActivitiesCountTO.setAddCount(getUserActivityDAO().getActivityTypeCounts().get("add"));
        userActivitiesCountTO.setUpdateCount(getUserActivityDAO().getActivityTypeCounts().get("update"));
        userActivitiesCountTO.setDeleteCount(getUserActivityDAO().getActivityTypeCounts().get("delete"));
        return userActivitiesCountTO;
    }
 
    /**
     * @return the userActivityDAO
     */
    public IUserActivityDAO getUserActivityDAO() {
        return userActivityDAO;
    }

    /**
     * @param userActivityDAO the userActivityDAO to set
     */
    public void setUserActivityDAO(IUserActivityDAO userActivityDAO) {
        this.userActivityDAO = userActivityDAO;
    }
    
}