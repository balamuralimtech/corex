
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.systemmanagement.impl;


import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import com.persist.coretix.modal.systemmanagement.dao.ICurrencyDetailsDAO;
import com.module.coretix.systemmanagement.ICurrencyDetailsService;

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



public class CurrencyDetailsService implements ICurrencyDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyDetailsService.class);
    @Inject
    private ICurrencyDetailsDAO currencyDetailsDAO;
    @Inject
    private UserActivityDAO userActivityDAO;
    @Transactional(readOnly = false)
    public GeneralConstants addCurrencyDetails(UserActivityTO userActivityTO,CurrencyDetails currencyDetail) {
        logger.debug("inside CurrencyDetails addCurrencyDetails");

        GeneralConstants generalConstants = getCurrencyDetailsDAO().addCurrencyDetails(currencyDetail);
        userActivityTO.setActivityDescription("Add Currency - ("+currencyDetail.getCurrencyName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteCurrencyDetails(UserActivityTO userActivityTO,CurrencyDetails currencyDetail) {

        GeneralConstants generalConstants = getCurrencyDetailsDAO().deleteCurrencyDetails(currencyDetail);
        userActivityTO.setActivityDescription("Delete Currency - ("+currencyDetail.getCurrencyName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;

    }

    @Transactional(readOnly = false)
    public GeneralConstants updateCurrencyDetails(UserActivityTO userActivityTO,CurrencyDetails currencyDetail) {

        GeneralConstants generalConstants = getCurrencyDetailsDAO().updateCurrencyDetails(currencyDetail);
        userActivityTO.setActivityDescription("Edit Currency - ("+currencyDetail.getCurrencyName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public CurrencyDetails getCurrencyDetailsById(int id) {
        return getCurrencyDetailsDAO().getCurrencyDetailsById(id);
    }

    public CurrencyDetails getCurrencyDetailsEntityByCurrencyName(String currencyName){
        return getCurrencyDetailsDAO().getCurrencyDetailsEntityByCurrencyName(currencyName);
    }

    public List<CurrencyDetails> getCurrencyDetailsList() {
        return getCurrencyDetailsDAO().getCurrencyDetailsList();
    }

    /**
     * @return the currencyDetailsDAO
     */
    public ICurrencyDetailsDAO getCurrencyDetailsDAO() {
        return currencyDetailsDAO;
    }

    /**
     * @param currencyDetailsDAO the currencyDetailsDAO to set
     */
    public void setCurrencyDetailsDAO(ICurrencyDetailsDAO currencyDetailsDAO) {
        this.currencyDetailsDAO = currencyDetailsDAO;
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

