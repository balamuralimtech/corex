package com.module.carex.settings.impl;

import com.module.carex.settings.IPrescriptionSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.PrescriptionSettings;
import com.persist.carex.settings.dao.IPrescriptionSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named
@Transactional(readOnly = true)
public class PrescriptionSettingsService implements IPrescriptionSettingsService {

    @Inject
    private IPrescriptionSettingsDAO prescriptionSettingsDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    public GeneralConstants savePrescriptionSettings(UserActivityTO userActivityTO, PrescriptionSettings prescriptionSettings) {
        GeneralConstants result = prescriptionSettingsDAO.savePrescriptionSettings(prescriptionSettings);
        userActivityTO.setActivityDescription("Save prescription settings - (" + resolveOrganizationName(prescriptionSettings) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public PrescriptionSettings getPrescriptionSettingsByOrganizationId(Integer organizationId) {
        return prescriptionSettingsDAO.getPrescriptionSettingsByOrganizationId(organizationId);
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

    private String resolveOrganizationName(PrescriptionSettings prescriptionSettings) {
        if (prescriptionSettings == null || prescriptionSettings.getOrganization() == null
                || prescriptionSettings.getOrganization().getOrganizationName() == null) {
            return "Unknown Organization";
        }
        return prescriptionSettings.getOrganization().getOrganizationName();
    }
}
