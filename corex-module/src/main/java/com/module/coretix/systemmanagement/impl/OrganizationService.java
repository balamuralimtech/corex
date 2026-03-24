/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
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
 * Project: `app.name`
 */
package com.module.coretix.systemmanagement.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.systemmanagement.dao.IOrganizationDAO;
import com.module.coretix.systemmanagement.IOrganizationService;

import java.sql.Timestamp;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author balamurali
 */
@Named
@Transactional(readOnly = true)
public class OrganizationService implements IOrganizationService{
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);
    
    @Inject
    private IOrganizationDAO organizationDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    public GeneralConstants addOrganization(UserActivityTO userActivityTO, Organizations organization) {
        GeneralConstants generalConstants = getOrganizationDAO().addOrganization(organization);
        userActivityTO.setActivityDescription("Add organization - ("+organization.getOrganizationName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
       return generalConstants;
    }

    public GeneralConstants updateOrganization(UserActivityTO userActivityTO, Organizations organization) {
        GeneralConstants generalConstants = getOrganizationDAO().updateOrganization(organization);
        userActivityTO.setActivityDescription("Edit organization - ("+organization.getOrganizationName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public GeneralConstants deleteOrganization(UserActivityTO userActivityTO, Organizations organization) {
        GeneralConstants generalConstants = getOrganizationDAO().deleteOrganization(organization);
        userActivityTO.setActivityDescription("Delete organization - ("+organization.getOrganizationName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public Organizations getOrganizationById(int id) {
        return getOrganizationDAO().getOrganization(id);
    }

    public List<Organizations> getOrganizationsList() {
        return getOrganizationDAO().getOrganizationsList();
    }
    
   
    public Organizations getOrganizationsEntityByOrganizationName(String organizationName){
        return getOrganizationDAO().getOrganizationsEntityByOrganizationName(organizationName);
    }

    /**
     * @return the organizationDAO
     */
    public IOrganizationDAO getOrganizationDAO() {
        return organizationDAO;
    }

    /**
     * @param organizationDAO the organizationDAO to set
     */
    public void setOrganizationDAO(IOrganizationDAO organizationDAO) {
        this.organizationDAO = organizationDAO;
    }

    public void addUserActivity(UserActivityTO userActivityTO) {
        // Logging all the values from userActivityTO
        logger.debug("User Activity - UserId: " + userActivityTO.getUserId());
        logger.debug("User Activity - UserName: " + userActivityTO.getUserName());
        logger.debug("User Activity - DeviceInfo: " + userActivityTO.getDeviceInfo());
        logger.debug("User Activity - IpAddress: " + userActivityTO.getIpAddress());
        logger.debug("User Activity - ActivityType: " + userActivityTO.getActivityType());
        logger.debug("User Activity - ActivityDescription: " + userActivityTO.getActivityDescription());
        logger.debug("User Activity - CreatedAt: " + userActivityTO.getCreatedAt());

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




