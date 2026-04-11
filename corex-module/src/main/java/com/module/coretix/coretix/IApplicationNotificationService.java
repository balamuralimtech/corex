package com.module.coretix.coretix;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;

import java.util.List;

public interface IApplicationNotificationService {

    GeneralConstants addApplicationNotification(UserActivityTO userActivityTO, ApplicationNotification applicationNotification);

    GeneralConstants updateApplicationNotification(UserActivityTO userActivityTO, ApplicationNotification applicationNotification);

    GeneralConstants deleteApplicationNotification(UserActivityTO userActivityTO, ApplicationNotification applicationNotification);

    List<ApplicationNotification> getRecentNotifications(int maxResults);

    ApplicationNotification getApplicationNotificationById(int id);

    int getUnreadNotificationCountForUser(int userId);

    GeneralConstants markAllNotificationsAsSeen(int userId);

    GeneralConstants addPublicApplicationNotification(ApplicationNotification applicationNotification);
}
