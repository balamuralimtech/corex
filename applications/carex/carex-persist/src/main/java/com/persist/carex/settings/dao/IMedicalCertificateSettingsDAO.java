package com.persist.carex.settings.dao;

import com.persist.carex.settings.MedicalCertificateSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IMedicalCertificateSettingsDAO {
    GeneralConstants saveMedicalCertificateSettings(MedicalCertificateSettings medicalCertificateSettings);
    MedicalCertificateSettings getMedicalCertificateSettingsByOrganizationId(Integer organizationId);
}
