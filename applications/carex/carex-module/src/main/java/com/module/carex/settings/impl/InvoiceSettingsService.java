package com.module.carex.settings.impl;

import com.module.carex.settings.IInvoiceSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.InvoiceSettings;
import com.persist.carex.settings.dao.IInvoiceSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named
@Transactional(readOnly = true)
public class InvoiceSettingsService implements IInvoiceSettingsService {

    @Inject
    private IInvoiceSettingsDAO invoiceSettingsDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants saveInvoiceSettings(UserActivityTO userActivityTO, InvoiceSettings invoiceSettings) {
        GeneralConstants result = invoiceSettingsDAO.saveInvoiceSettings(invoiceSettings);
        userActivityTO.setActivityDescription("Save invoice settings - (" + resolveOrganizationName(invoiceSettings) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public InvoiceSettings getInvoiceSettingsByOrganizationId(Integer organizationId) {
        return invoiceSettingsDAO.getInvoiceSettingsByOrganizationId(organizationId);
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

    private String resolveOrganizationName(InvoiceSettings invoiceSettings) {
        if (invoiceSettings == null || invoiceSettings.getOrganization() == null || invoiceSettings.getOrganization().getOrganizationName() == null) {
            return "Unknown Organization";
        }
        return invoiceSettings.getOrganization().getOrganizationName();
    }
}
