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
package com.module.coretix.systemmanagement.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.dao.ICountryDAO;
import com.module.coretix.systemmanagement.ICountryService;

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
public class CountryService implements ICountryService {

    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);
    @Inject
    private ICountryDAO countryDAO;

    @Inject
    private UserActivityDAO userActivityDAO;


    @Transactional(readOnly = false)
    public GeneralConstants addCountry(UserActivityTO userActivityTO, Countries country) {
        logger.debug("inside CountryService addCountry");
        GeneralConstants generalConstants = getCountryDAO().addCountry(country);
        userActivityTO.setActivityDescription("Add Country - ("+country.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteCountry(UserActivityTO userActivityTO,Countries country) {
        GeneralConstants generalConstants = getCountryDAO().deleteCountry(country);
        userActivityTO.setActivityDescription("Delete organization - ("+country.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;    }

    @Transactional(readOnly = false)
    public GeneralConstants updateCountry(UserActivityTO userActivityTO,Countries country) {
        GeneralConstants generalConstants = getCountryDAO().updateCountry(country);
        userActivityTO.setActivityDescription("Edit organization - ("+country.getName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;    }

    public Countries getCountryById(int id) {
        return getCountryDAO().getCountry(id);
    }
    
    public Countries getCountryEntityByCountryName(String countryName) {
        return getCountryDAO().getCountryEntityByCountryName(countryName);
    }

    public List<Countries> getCountriesList() {
        return getCountryDAO().getCountriesList();
    }

    /**
     * @return the countryDAO
     */
    public ICountryDAO getCountryDAO() {
        return countryDAO;
    }

    /**
     * @param countryDAO the countryDAO to set
     */
    public void setCountryDAO(ICountryDAO countryDAO) {
        this.countryDAO = countryDAO;
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




