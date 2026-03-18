/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class  DesignationService implements IDesignationService {

    private final Logger logger = Logger.getLogger(getClass());
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

