package com.module.carex.settings;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.PrescriptionSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IPrescriptionSettingsService {

    GeneralConstants savePrescriptionSettings(UserActivityTO userActivityTO, PrescriptionSettings prescriptionSettings);

    PrescriptionSettings getPrescriptionSettingsByOrganizationId(Integer organizationId);
}
