/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.systemmanagement.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.NotificationSettings;
import com.persist.coretix.modal.systemmanagement.dao.INotificationSettingDAO;
import com.module.coretix.systemmanagement.INotificationSettingService;

import java.sql.Timestamp;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author pragadeesh
 */
@Named
@Transactional(readOnly = true)



public class NotificationSettingService implements INotificationSettingService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSettingService.class);
    @Inject
    private INotificationSettingDAO notificationSettingDAO;
    @Inject
    private UserActivityDAO userActivityDAO;
    @Transactional(readOnly = false)
    public GeneralConstants addNotificationSetting(UserActivityTO userActivityTO,NotificationSettings notificationSetting) {

        GeneralConstants generalConstants = getNotificationSettingDAO().addNotificationSetting(notificationSetting);
        userActivityTO.setActivityDescription("Add organization - ("+notificationSetting.getEmailId()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteNotificationSetting(UserActivityTO userActivityTO,NotificationSettings notificationSetting) {

        GeneralConstants generalConstants = getNotificationSettingDAO().deleteNotificationSetting(notificationSetting);
        userActivityTO.setActivityDescription("Delete organization - ("+notificationSetting.getEmailId()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants updateNotificationSetting(UserActivityTO userActivityTO,NotificationSettings notificationSetting) {

        GeneralConstants generalConstants = getNotificationSettingDAO().updateNotificationSetting(notificationSetting);
        userActivityTO.setActivityDescription("Edit organization - ("+notificationSetting.getEmailId()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    public NotificationSettings getNotificationSettingById(int id) {
        return getNotificationSettingDAO().getNotificationSetting(id);
    }

    public NotificationSettings getNotificationSettingByOrganizationId(int orgId) {
        return getNotificationSettingDAO().getNotificationSettingByOrganizationId(orgId);
    }

    public List<NotificationSettings> getNotificationSettingsList() {
        return getNotificationSettingDAO().getNotificationSettingsList();
    }

    /**
     * @return the notificationSettingDAO
     */
    public INotificationSettingDAO getNotificationSettingDAO() {
        return notificationSettingDAO;
    }

    /**
     * @param notificationSettingDAO the notificationSettingDAO to set
     */
    public void setNotificationSettingDAO(INotificationSettingDAO notificationSettingDAO) {
        this.notificationSettingDAO = notificationSettingDAO;
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


}

