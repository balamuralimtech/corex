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
import com.persist.coretix.modal.systemmanagement.Subregions;
import com.persist.coretix.modal.systemmanagement.dao.ISubRegionDAO;
import com.module.coretix.systemmanagement.ISubRegionService;

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



public class  SubRegionService implements ISubRegionService {

    private static final Logger logger = LoggerFactory.getLogger(SubRegionService.class);
    @Inject
    private ISubRegionDAO subRegionDAO;
    @Inject
    private UserActivityDAO userActivityDAO;
    @Transactional(readOnly = false)
    public GeneralConstants addSubRegion(UserActivityTO userActivityTO,Subregions subRegion) {
        logger.debug("inside SubRegionService addSubRegion");
        getSubRegionDAO().addSubRegion(subRegion);

        GeneralConstants generalConstants = getSubRegionDAO().addSubRegion(subRegion);
        userActivityTO.setActivityDescription("Add SubRegion - ("+subRegion.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteSubRegion(UserActivityTO userActivityTO,Subregions subRegion) {
        getSubRegionDAO().deleteSubRegion(subRegion);

        GeneralConstants generalConstants = getSubRegionDAO().deleteSubRegion(subRegion);
        userActivityTO.setActivityDescription("Delete SubRegion - ("+subRegion.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants updateSubRegion(UserActivityTO userActivityTO,Subregions subRegion) {
        getSubRegionDAO().updateSubRegion(subRegion);

        GeneralConstants generalConstants = getSubRegionDAO().updateSubRegion(subRegion);
        userActivityTO.setActivityDescription("Edit SubRegion - ("+subRegion.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    public Subregions getSubregionBySubregionName(String subregionName) {
        return getSubRegionDAO().getSubregionBySubregionName(subregionName);
    }
    
    public Subregions getSubRegionById(int id) {
        return getSubRegionDAO().getSubRegion(id);
    }

    public List<Subregions> getSubRegionsList() {
        return getSubRegionDAO().getSubRegionsList();
    }

    /**
     * @return the SubRegionDAO
     */
    public ISubRegionDAO getSubRegionDAO() {
        return subRegionDAO;
    }

    /**
     * @param subRegionDAO the SubRegionDAO to set
     */
    public void setSubRegionDAO(ISubRegionDAO subRegionDAO) {
        this.subRegionDAO = subRegionDAO;
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




