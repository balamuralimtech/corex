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
import com.module.coretix.systemmanagement.IBankDetailsService;
import com.persist.coretix.modal.constants.GeneralConstants;

import com.persist.coretix.modal.systemmanagement.BankDetails;

import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import com.persist.coretix.modal.systemmanagement.dao.IBankDetailsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class BankDetailsService implements IBankDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(BankDetailsService.class);
    @Inject
    private IBankDetailsDAO bankDetailsDAO;


    @Inject
    private UserActivityDAO userActivityDAO;



    @Transactional(readOnly = false)
    public GeneralConstants addBankDetails(UserActivityTO userActivityTO, BankDetails bankDetails) {
        logger.debug("inside BankDetailservice add TermsandConditions");
        logger.debug("Bank Details and  OrgName:" + bankDetails.getOrganization().getOrganizationName());
        logger.debug("Bank Details and  OrgId:" + bankDetails.getOrganization().getId());
        GeneralConstants generalConstants = getBankDetailsDAO().addBankDetails(bankDetails);
        userActivityTO.setActivityDescription("Add BankDetails for - (" + bankDetails.getOrganization().getOrganizationName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants deleteBankDetails(UserActivityTO userActivityTO, BankDetails bankDetails) {
        GeneralConstants generalConstants = getBankDetailsDAO().deleteBankDetails(bankDetails);
        userActivityTO.setActivityDescription("Delete bankDetails of  - (" + bankDetails.getOrganization().getOrganizationName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants updateBankDetails(UserActivityTO userActivityTO, BankDetails bankDetails) {
        GeneralConstants generalConstants = getBankDetailsDAO().updateBankDetails(bankDetails);
        userActivityTO.setActivityDescription("Edit bankDetails of - (" + bankDetails.getOrganization().getOrganizationName() + ") - " + generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public BankDetails getBankDetailsById(int id) {
        return getBankDetailsDAO().getBankDetails(id);
    }

    public BankDetails getBankDetailsByOrgId(int orgId) {
        return getBankDetailsDAO().getBankDetailsByOrgId(orgId);
    }

    public List<BankDetails> getBankDetailsList() {
        logger.debug("inside getBankDetailsList method !!!");
        return getBankDetailsDAO().getBankDetailsList();
    }

    public List<BankDetails> getBankDetailsListByOrgId(int orgId) {
        logger.debug("inside getBankDetailsListByOrgId method !!!");
        return getBankDetailsDAO().getBankDetailsListByOrgId(orgId);
    }


    /**
     * @return the bankDetailsDAO
     */
    public IBankDetailsDAO getBankDetailsDAO() {
        return bankDetailsDAO;
    }

    /**
     * @param bankDetailsDAO the bankDetailsDAO to set
     */
    public void setBankDetailsDAO(IBankDetailsDAO bankDetailsDAO) {
        this.bankDetailsDAO = bankDetailsDAO;
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






