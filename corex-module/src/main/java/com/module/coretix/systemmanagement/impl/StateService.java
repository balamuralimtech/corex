/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.systemmanagement.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.States;
import com.persist.coretix.modal.systemmanagement.dao.IStateDAO;
import com.module.coretix.systemmanagement.IStateService;

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
public class StateService implements IStateService {

    private static final Logger logger = LoggerFactory.getLogger(StateService.class);
    @Inject
    private IStateDAO stateDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Transactional(readOnly = false)
    public GeneralConstants addState(UserActivityTO userActivityTO, States state) {
        logger.debug("inside StateService addState");
        GeneralConstants generalConstants = getStateDAO().addState(state);
        userActivityTO.setActivityDescription("Add State - ("+state.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteState(UserActivityTO userActivityTO,States state) {
        GeneralConstants generalConstants = getStateDAO().deleteState(state);
        userActivityTO.setActivityDescription("Delete State - ("+state.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;    }

    @Transactional(readOnly = false)
    public GeneralConstants updateState(UserActivityTO userActivityTO,States state) {
        GeneralConstants generalConstants = getStateDAO().updateState(state);
        userActivityTO.setActivityDescription("Edit State - ("+state.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;      }

    public States getStateById(int id) {
        return getStateDAO().getState(id);
    }

    public States getStateEntityByStateName(String stateName) {
        return getStateDAO().getStateEntityByStateName(stateName);
    }

    public List<States> getStatesList() {
        return getStateDAO().getStatesList();
    }
    
    public List<States> getStatesListByCountryId(int countryId) {
        return getStateDAO().getStatesListByCountryId(countryId);
    }

    /**
     * @return the stateDAO
     */
    public IStateDAO getStateDAO() {
        return stateDAO;
    }

    /**
     * @param stateDAO the stateDAO to set
     */
    public void setStateDAO(IStateDAO stateDAO) {
        this.stateDAO = stateDAO;
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

