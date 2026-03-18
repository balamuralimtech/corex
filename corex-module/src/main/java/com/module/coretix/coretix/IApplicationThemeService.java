package com.module.coretix.coretix;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationTheme;

public interface IApplicationThemeService {
    public GeneralConstants addApplicationTheme(UserActivityTO userActivityTO, ApplicationTheme applicationTheme);

    public GeneralConstants updateApplicationTheme(UserActivityTO userActivityTO, ApplicationTheme applicationTheme);

    public ApplicationTheme getApplicationThemeByUserid(int userid);
}
