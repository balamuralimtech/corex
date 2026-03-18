/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.NotificationSettings;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface INotificationSettingService 
{
    public GeneralConstants addNotificationSetting(UserActivityTO userActivityTO, NotificationSettings notificationSetting);

    public GeneralConstants updateNotificationSetting(UserActivityTO userActivityTO,NotificationSettings notificationSetting);

    public GeneralConstants deleteNotificationSetting(UserActivityTO userActivityTO,NotificationSettings notificationSetting);

    public NotificationSettings getNotificationSettingById(int id);

    public NotificationSettings getNotificationSettingByOrganizationId(int organizationId);

    public List<NotificationSettings> getNotificationSettingsList();
    
}