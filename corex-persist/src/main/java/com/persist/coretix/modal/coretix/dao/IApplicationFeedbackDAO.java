package com.persist.coretix.modal.coretix.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationFeedback;

import java.util.List;

public interface IApplicationFeedbackDAO {

    GeneralConstants addApplicationFeedback(ApplicationFeedback applicationFeedback);

    List<ApplicationFeedback> getRecentFeedback(int maxResults);
}
