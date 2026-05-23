package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IApplicationFeedbackService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationFeedback;
import com.persist.coretix.modal.coretix.dao.IApplicationFeedbackDAO;
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
public class ApplicationFeedbackService implements IApplicationFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationFeedbackService.class);

    @Inject
    private IApplicationFeedbackDAO applicationFeedbackDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addApplicationFeedback(UserActivityTO userActivityTO, ApplicationFeedback applicationFeedback) {
        GeneralConstants result = applicationFeedbackDAO.addApplicationFeedback(applicationFeedback);
        userActivityTO.setActivityDescription("Application feedback submitted - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public List<ApplicationFeedback> getRecentFeedback(int maxResults) {
        return applicationFeedbackDAO.getRecentFeedback(maxResults);
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        logger.debug("User Activity - UserId: {}", userActivityTO.getUserId());
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
