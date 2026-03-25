package com.persist.coretix.modal.coretix.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;

import java.util.List;

public interface IApplicationNotificationDAO {

    GeneralConstants addApplicationNotification(ApplicationNotification applicationNotification);

    GeneralConstants updateApplicationNotification(ApplicationNotification applicationNotification);

    GeneralConstants deleteApplicationNotification(ApplicationNotification applicationNotification);

    List<ApplicationNotification> getRecentNotifications(int maxResults);

    ApplicationNotification getApplicationNotificationById(int id);

    int getUnreadNotificationCountForUser(int userId);

    GeneralConstants markAllNotificationsAsSeen(int userId);
}
