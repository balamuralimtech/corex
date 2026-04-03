package com.persist.carex.settings.dao;

import com.persist.carex.settings.PrescriptionSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IPrescriptionSettingsDAO {

    GeneralConstants savePrescriptionSettings(PrescriptionSettings prescriptionSettings);

    PrescriptionSettings getPrescriptionSettingsByOrganizationId(Integer organizationId);
}
