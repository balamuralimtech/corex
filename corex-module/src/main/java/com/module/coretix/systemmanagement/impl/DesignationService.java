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
import com.persist.coretix.modal.systemmanagement.Designations;
import com.persist.coretix.modal.systemmanagement.dao.IDesignationDAO;
import com.module.coretix.systemmanagement.IDesignationService;

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
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class  DesignationService implements IDesignationService {

    private static final Logger logger = LoggerFactory.getLogger(DesignationService.class);
    @Inject
    private IDesignationDAO designationDAO;
    @Inject
    private UserActivityDAO userActivityDAO;


    @Transactional(readOnly = false)
    public GeneralConstants addDesignation(UserActivityTO userActivityTO, Designations designation) {
        logger.debug("inside DesignationService addDesignation");
        GeneralConstants generalConstants = getDesignationDAO().addDesignation(designation);
        userActivityTO.setActivityDescription("Add Designation - ("+designation.getDesignationName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
        }

    @Transactional(readOnly = false)
    public GeneralConstants deleteDesignation(UserActivityTO userActivityTO,Designations designation) {
        GeneralConstants generalConstants = getDesignationDAO().deleteDesignation(designation);
        userActivityTO.setActivityDescription("Delete designation - ("+designation.getDesignationName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants updateDesignation(UserActivityTO userActivityTO,Designations designation) {
        GeneralConstants generalConstants = getDesignationDAO().updateDesignation(designation);
        userActivityTO.setActivityDescription("Edit designation - ("+designation.getDesignationName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public Designations getDesignationById(int id) {
        return getDesignationDAO().getDesignation(id);
    }

    public List<Designations> getDesignationsList() {
        return getDesignationDAO().getDesignationsList();
    }

    /**
     * @return the designationDAO
     */
    public IDesignationDAO getDesignationDAO() {
        return designationDAO;
    }

    /**
     * @param designationDAO the designationDAO to set
     */
    public void setDesignationDAO(IDesignationDAO designationDAO) {
        this.designationDAO = designationDAO;
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





