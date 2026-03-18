package com.module.coretix.coretix.impl;

import com.module.coretix.coretix.IApplicationThemeService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationTheme;
import com.persist.coretix.modal.coretix.dao.IApplicationThemeDAO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named
@Transactional(readOnly = true)
public class ApplicationThemeService implements IApplicationThemeService {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    private IApplicationThemeDAO applicationThemeDAO;
    @Inject
    private UserActivityDAO userActivityDAO;


    public GeneralConstants addApplicationTheme(UserActivityTO userActivityTO, ApplicationTheme applicationTheme) {
        GeneralConstants generalConstants = applicationThemeDAO.addApplicationTheme(applicationTheme);
        userActivityTO.setActivityDescription("Add Theme - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public GeneralConstants updateApplicationTheme(UserActivityTO userActivityTO, ApplicationTheme applicationTheme) {
        GeneralConstants generalConstants = applicationThemeDAO.updateApplicationTheme(applicationTheme);
        userActivityTO.setActivityDescription("Edit Theme - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public ApplicationTheme getApplicationThemeByUserid(int userid) {
        return applicationThemeDAO.getApplicationThemeByUserid(userid);
    }

    public void addUserActivity(UserActivityTO userActivityTO) {
        // Logging all the values from userActivityTO
        logger.debug("User Activity - UserId: " + userActivityTO.getUserId());
        logger.debug("User Activity - UserName: " + userActivityTO.getUserName());
        logger.debug("User Activity - DeviceInfo: " + userActivityTO.getDeviceInfo());
        logger.debug("User Activity - IpAddress: " + userActivityTO.getIpAddress());
        logger.debug("User Activity - ActivityType: " + userActivityTO.getActivityType());
        logger.debug("User Activity - ActivityDescription: " + userActivityTO.getActivityDescription());
        logger.debug("User Activity - CreatedAt: " + userActivityTO.getCreatedAt());

        UserActivities useractivity = new UserActivities();
        useractivity.setUserId(userActivityTO.getUserId());
        useractivity.setUserName(userActivityTO.getUserName());
        useractivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        useractivity.setIpAddress(userActivityTO.getIpAddress());
        useractivity.setLocationInfo(userActivityTO.getLocationInfo());
        useractivity.setActivityType(userActivityTO.getActivityType());
        useractivity.setActivityDescription(userActivityTO.getActivityDescription());
        useractivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        userActivityDAO.addUserActivity(useractivity);
    }



    public IApplicationThemeDAO getApplicationThemeDAO() {
        return applicationThemeDAO;
    }

    public void setApplicationThemeDAO(IApplicationThemeDAO applicationThemeDAO) {
        this.applicationThemeDAO = applicationThemeDAO;
    }

    public UserActivityDAO getUserActivityDAO() {
        return userActivityDAO;
    }

    public void setUserActivityDAO(UserActivityDAO userActivityDAO) {
        this.userActivityDAO = userActivityDAO;
    }
}
