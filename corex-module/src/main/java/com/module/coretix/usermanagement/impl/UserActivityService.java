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

import com.module.coretix.commonto.UserActivitiesCountTO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
import com.module.coretix.usermanagement.IUserActivityService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class UserActivityService  implements IUserActivityService{
    
        private static final Logger logger = LoggerFactory.getLogger(UserActivityService.class);
   @Inject
    private IUserActivityDAO userActivityDAO;
   
    public List<UserActivities> getUserActivitiesList() {
        return getUserActivityDAO().getUserActivitiesList();
    }

    @Transactional(readOnly = false)
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



