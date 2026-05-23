package com.module.coretix.coretix;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationFeedback;

import java.util.List;

public interface IApplicationFeedbackService {

    GeneralConstants addApplicationFeedback(UserActivityTO userActivityTO, ApplicationFeedback applicationFeedback);

    List<ApplicationFeedback> getRecentFeedback(int maxResults);
}
