/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.systemmanagement.impl;


import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Regions;
import com.persist.coretix.modal.systemmanagement.dao.IRegionDAO;
import com.module.coretix.systemmanagement.IRegionService;

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



public class  RegionService implements IRegionService {

    private static final Logger logger = LoggerFactory.getLogger(RegionService.class);
    @Inject
    private IRegionDAO regionDAO;

    @Inject
    private UserActivityDAO userActivityDAO;
    @Transactional(readOnly = false)
    public GeneralConstants addRegion(UserActivityTO userActivityTO,Regions region) {
        logger.debug("inside RegionService addRegion");
        GeneralConstants generalConstants = getRegionDAO().addRegion(region);
        userActivityTO.setActivityDescription("Add Region - ("+region.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteRegion(UserActivityTO userActivityTO,Regions region) {

        GeneralConstants generalConstants = getRegionDAO().deleteRegion(region);
        userActivityTO.setActivityDescription("Delete organization - ("+region.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants updateRegion(UserActivityTO userActivityTO,Regions region) {
        GeneralConstants generalConstants = getRegionDAO().updateRegion(region);
        userActivityTO.setActivityDescription("Edit organization - ("+region.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    public Regions getRegionById(int id) {
        return getRegionDAO().getRegion(id);
    }

    public Regions getRegionByRegionName(String regionName) {
        return getRegionDAO().getRegionByRegionName(regionName);
    }

    public List<Regions> getRegionsList() {
        return getRegionDAO().getRegionsList();
    }

    /**
     * @return the regionDAO
     */
    public IRegionDAO getRegionDAO() {
        return regionDAO;
    }

    /**
     * @param regionDAO the regionDAO to set
     */
    public void setRegionDAO(IRegionDAO regionDAO) {
        this.regionDAO = regionDAO;
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


