package com.module.carex.settings.impl;

import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.ClinicSettings;
import com.persist.carex.settings.dao.IClinicSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named
@Transactional(readOnly = true)
public class ClinicSettingsService implements IClinicSettingsService {

    @Inject
    private IClinicSettingsDAO clinicSettingsDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    public GeneralConstants saveClinicSettings(UserActivityTO userActivityTO, ClinicSettings clinicSettings) {
        GeneralConstants result = clinicSettingsDAO.saveClinicSettings(clinicSettings);
        userActivityTO.setActivityDescription("Save clinic settings - (" + resolveOrganizationName(clinicSettings) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public ClinicSettings getClinicSettingsByOrganizationId(Integer organizationId) {
        return clinicSettingsDAO.getClinicSettingsByOrganizationId(organizationId);
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

    private String resolveOrganizationName(ClinicSettings clinicSettings) {
        if (clinicSettings == null || clinicSettings.getOrganization() == null || clinicSettings.getOrganization().getOrganizationName() == null) {
            return "Unknown Organization";
        }
        return clinicSettings.getOrganization().getOrganizationName();
    }
}
