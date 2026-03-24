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
import com.persist.coretix.modal.systemmanagement.Branches;
import com.persist.coretix.modal.systemmanagement.dao.IBranchDAO;
import com.module.coretix.systemmanagement.IBranchService;

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
public class  BranchService implements IBranchService {

    private static final Logger logger = LoggerFactory.getLogger(BranchService.class);
    @Inject
    private IBranchDAO branchDAO;

    @Inject
    private UserActivityDAO userActivityDAO;


    @Transactional(readOnly = false)

    public GeneralConstants addBranch(UserActivityTO userActivityTO, Branches branch) {
        logger.debug("entered into addBranch");
        GeneralConstants generalConstants = getBranchDAO().addBranch(branch);
        userActivityTO.setActivityDescription("Add Branch - ("+branch.getBranchName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants updateBranch(UserActivityTO userActivityTO, Branches branch) {
        GeneralConstants generalConstants = getBranchDAO().updateBranch(branch);
        userActivityTO.setActivityDescription("Edit branch - ("+branch.getBranchName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }
    @Transactional(readOnly = false)
    public GeneralConstants deleteBranch(UserActivityTO userActivityTO, Branches branch) {
        GeneralConstants generalConstants = getBranchDAO().deleteBranch(branch);
        userActivityTO.setActivityDescription("Delete branch - ("+branch.getBranchName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }
    public Branches getBranchById(int id) {
        return getBranchDAO().getBranch(id);
    }

    public Branches getBranchEntityByBranchName(String branchName) {
        return getBranchDAO().getBranchEntityByBranchName(branchName);
    }

    public List<Branches> getBranchesList() {
        return getBranchDAO().getBranchesList();
    }

    public List<Branches> getBranchesListByOrgId(int orgId) {
        return getBranchDAO().getBranchesListByOrgId(orgId);
    }

    /**
     * @return the branchDAO
     */
    public IBranchDAO getBranchDAO() {
        return branchDAO;
    }

    /**
     * @param branchDAO the branchDAO to set
     */
    public void setBranchDAO(IBranchDAO branchDAO) {
        this.branchDAO = branchDAO;
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





