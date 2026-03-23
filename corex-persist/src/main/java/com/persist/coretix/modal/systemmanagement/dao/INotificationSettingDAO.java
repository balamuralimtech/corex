/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.NotificationSettings;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface INotificationSettingDAO {
    public GeneralConstants addNotificationSetting(NotificationSettings notificationSetting);

    public GeneralConstants updateNotificationSetting(NotificationSettings notificationSetting);

    public GeneralConstants deleteNotificationSetting(NotificationSettings notificationSetting);

    public NotificationSettings getNotificationSetting(int id);

    public NotificationSettings getNotificationSettingByOrganizationId(int organizationId);

    public List<NotificationSettings> getNotificationSettingsList();
    
}

