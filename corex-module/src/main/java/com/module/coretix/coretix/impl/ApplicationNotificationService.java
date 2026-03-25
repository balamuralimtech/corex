package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.persist.coretix.modal.coretix.dao.IApplicationNotificationDAO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class ApplicationNotificationService implements IApplicationNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNotificationService.class);

    @Inject
    private IApplicationNotificationDAO applicationNotificationDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addApplicationNotification(UserActivityTO userActivityTO, ApplicationNotification applicationNotification) {
        GeneralConstants result = applicationNotificationDAO.addApplicationNotification(applicationNotification);
        userActivityTO.setActivityDescription("Application notification broadcast - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updateApplicationNotification(UserActivityTO userActivityTO, ApplicationNotification applicationNotification) {
        GeneralConstants result = applicationNotificationDAO.updateApplicationNotification(applicationNotification);
        userActivityTO.setActivityDescription("Application notification updated - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteApplicationNotification(UserActivityTO userActivityTO, ApplicationNotification applicationNotification) {
        GeneralConstants result = applicationNotificationDAO.deleteApplicationNotification(applicationNotification);
        userActivityTO.setActivityDescription("Application notification deleted - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public List<ApplicationNotification> getRecentNotifications(int maxResults) {
        return applicationNotificationDAO.getRecentNotifications(maxResults);
    }

    @Override
    public ApplicationNotification getApplicationNotificationById(int id) {
        return applicationNotificationDAO.getApplicationNotificationById(id);
    }

    @Override
    public int getUnreadNotificationCountForUser(int userId) {
        return applicationNotificationDAO.getUnreadNotificationCountForUser(userId);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants markAllNotificationsAsSeen(int userId) {
        return applicationNotificationDAO.markAllNotificationsAsSeen(userId);
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        logger.debug("User Activity - UserId: {}", userActivityTO.getUserId());
        logger.debug("User Activity - UserName: {}", userActivityTO.getUserName());
        logger.debug("User Activity - DeviceInfo: {}", userActivityTO.getDeviceInfo());
        logger.debug("User Activity - IpAddress: {}", userActivityTO.getIpAddress());
        logger.debug("User Activity - ActivityType: {}", userActivityTO.getActivityType());
        logger.debug("User Activity - ActivityDescription: {}", userActivityTO.getActivityDescription());

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
}
