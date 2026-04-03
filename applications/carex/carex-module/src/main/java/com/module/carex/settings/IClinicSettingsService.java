package com.module.carex.settings;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IClinicSettingsService {

    GeneralConstants saveClinicSettings(UserActivityTO userActivityTO, ClinicSettings clinicSettings);

    ClinicSettings getClinicSettingsByOrganizationId(Integer organizationId);
}
