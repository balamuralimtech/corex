package com.module.carex.settings;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.MedicalCertificateSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IMedicalCertificateSettingsService {
    GeneralConstants saveMedicalCertificateSettings(UserActivityTO userActivityTO, MedicalCertificateSettings medicalCertificateSettings);
    MedicalCertificateSettings getMedicalCertificateSettingsByOrganizationId(Integer organizationId);
}
