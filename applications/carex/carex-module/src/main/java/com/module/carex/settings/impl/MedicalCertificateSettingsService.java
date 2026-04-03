package com.module.carex.settings.impl;

import com.module.carex.settings.IMedicalCertificateSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.MedicalCertificateSettings;
import com.persist.carex.settings.dao.IMedicalCertificateSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named
@Transactional(readOnly = true)
public class MedicalCertificateSettingsService implements IMedicalCertificateSettingsService {

    @Inject
    private IMedicalCertificateSettingsDAO medicalCertificateSettingsDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    public GeneralConstants saveMedicalCertificateSettings(UserActivityTO userActivityTO,
                                                           MedicalCertificateSettings medicalCertificateSettings) {
        GeneralConstants result = medicalCertificateSettingsDAO.saveMedicalCertificateSettings(medicalCertificateSettings);
        userActivityTO.setActivityDescription("Save medical certificate settings - ("
                + resolveOrganizationName(medicalCertificateSettings) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public MedicalCertificateSettings getMedicalCertificateSettingsByOrganizationId(Integer organizationId) {
        return medicalCertificateSettingsDAO.getMedicalCertificateSettingsByOrganizationId(organizationId);
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        UserActivities userActivity = new UserActivities();
        userActivity.setUserId(userActivityTO.getUserId());
        userActivity.setUserName(userActivityTO.getUserName());
        userActivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        userActivity.setIpAddress(userActivityTO.getIpAddress());
        userActivity.setLocationInfo(userActivityTO.getLocationInfo());
        userActivity.setActivityType(userActivityTO.getActivityType());
        userActivity.setActivityDescription(userActivityTO.getActivityDescription());
        userActivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userActivityDAO.addUserActivity(userActivity);
    }

    private String resolveOrganizationName(MedicalCertificateSettings medicalCertificateSettings) {
        if (medicalCertificateSettings == null || medicalCertificateSettings.getOrganization() == null
                || medicalCertificateSettings.getOrganization().getOrganizationName() == null) {
            return "Unknown Organization";
        }
        return medicalCertificateSettings.getOrganization().getOrganizationName();
    }
}
