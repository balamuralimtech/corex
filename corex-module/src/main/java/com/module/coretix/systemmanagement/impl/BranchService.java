/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Pragadeesh
 */
@Named
@Transactional(readOnly = true)
public class  BranchService implements IBranchService {

    private final Logger logger = Logger.getLogger(getClass());
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

