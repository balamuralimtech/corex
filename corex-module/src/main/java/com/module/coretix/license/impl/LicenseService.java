/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.module.coretix.license.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.license.ILicenseService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.license.dao.ILicenseDAO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class LicenseService implements ILicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    @Inject
    private ILicenseDAO licenseDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Transactional(readOnly = false)
    public GeneralConstants addLicense(UserActivityTO userActivityTO, Licenses license) {
        GeneralConstants generalConstants = licenseDAO.addLicense(license);
        userActivityTO.setActivityDescription("Add license - (" + license.getOrganization().getOrganizationName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants updateLicense(UserActivityTO userActivityTO, Licenses license) {
        GeneralConstants generalConstants = licenseDAO.updateLicense(license);
        userActivityTO.setActivityDescription("Edit license - (" + license.getOrganization().getOrganizationName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteLicense(UserActivityTO userActivityTO, Licenses license) {
        GeneralConstants generalConstants = licenseDAO.deleteLicense(license);
        userActivityTO.setActivityDescription("Delete license - (" + license.getOrganization().getOrganizationName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public Licenses getLicenseById(int id) {
        return licenseDAO.getLicense(id);
    }

    public Licenses getLicenseByOrganizationId(int organizationId) {
        return licenseDAO.getLicenseByOrganizationId(organizationId);
    }

    public List<Licenses> getLicenseList() {
        return licenseDAO.getLicenseList();
    }

    public boolean isLicenseActiveForOrganization(int organizationId) {
        return licenseDAO.isLicenseActiveForOrganization(organizationId);
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        logger.debug("User Activity - UserId: " + userActivityTO.getUserId());
        logger.debug("User Activity - UserName: " + userActivityTO.getUserName());

        UserActivities useractivity = new UserActivities();
        useractivity.setUserId(userActivityTO.getUserId());
        useractivity.setUserName(userActivityTO.getUserName());
        useractivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        useractivity.setIpAddress(userActivityTO.getIpAddress());
        useractivity.setLocationInfo(userActivityTO.getLocationInfo());
        useractivity.setActivityType(userActivityTO.getActivityType());
        useractivity.setActivityDescription(userActivityTO.getActivityDescription());
        useractivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        userActivityDAO.addUserActivity(useractivity);
    }
}





