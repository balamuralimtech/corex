/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.systemmanagement.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Cities;
import com.persist.coretix.modal.systemmanagement.dao.ICityDAO;
import com.module.coretix.systemmanagement.ICityService;

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
public class CityService implements ICityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    @Inject
    private ICityDAO cityDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Transactional(readOnly = false)
    public GeneralConstants addCity(UserActivityTO userActivityTO, Cities city) {
        GeneralConstants generalConstants = getCityDAO().addCity(city);
        userActivityTO.setActivityDescription("Add City - (" + city.getName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteCity(UserActivityTO userActivityTO, Cities city) {
        GeneralConstants generalConstants = getCityDAO().deleteCity(city);
        userActivityTO.setActivityDescription("Delete City - (" + city.getName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants updateCity(UserActivityTO userActivityTO, Cities city) {
        GeneralConstants generalConstants = getCityDAO().updateCity(city);
        logger.debug("PragaLog Editing City"+city);
        userActivityTO.setActivityDescription("Edit city - (" + city.getName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    public Cities getCityById(int id) {
        return getCityDAO().getCity(id);
    }

    public Cities getCityEntityByCityName(String cityName) {
        return getCityDAO().getCityEntityByCityName(cityName);
    }

    public List<Cities> getCitiesList() {
        return getCityDAO().getCitiesList();
    }

    public List<Cities> getCitiesListByCountryIdAndStateId(int countryId, int stateId) {
        return getCityDAO().getCitiesListByCountryIdAndStateId(countryId, stateId);
    }

    /**
     * @return the cityDAO
     */
    public ICityDAO getCityDAO() {
        return cityDAO;
    }

    /**
     * @param cityDAO the cityDAO to set
     */
    public void setCityDAO(ICityDAO cityDAO) {
        this.cityDAO = cityDAO;
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

