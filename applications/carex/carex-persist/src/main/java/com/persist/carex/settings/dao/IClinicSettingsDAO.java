package com.persist.carex.settings.dao;

import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IClinicSettingsDAO {

    GeneralConstants saveClinicSettings(ClinicSettings clinicSettings);

    ClinicSettings getClinicSettingsByOrganizationId(Integer organizationId);
}
