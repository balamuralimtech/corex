/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.module.coretix.systemmanagement.impl;


import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Departments;
import com.persist.coretix.modal.systemmanagement.dao.IDepartmentDAO;
import com.module.coretix.systemmanagement.IDepartmentService;

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
public class  DepartmentService implements IDepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    @Inject
    private IDepartmentDAO departmentDAO;

    @Inject
    private UserActivityDAO userActivityDAO;


    @Transactional(readOnly = false)
    public GeneralConstants addDepartment(UserActivityTO userActivityTO,Departments department) {
        logger.debug("inside DepartmentService addDepartment");
        GeneralConstants generalConstants = getDepartmentDAO().addDepartment(department);
        userActivityTO.setActivityDescription("Add Department - ("+department.getDepartmentName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }
    @Transactional(readOnly = false)
    public GeneralConstants deleteDepartment(UserActivityTO userActivityTO,Departments department) {
        GeneralConstants generalConstants = getDepartmentDAO().deleteDepartment(department);
        userActivityTO.setActivityDescription("Delete department - ("+department.getDepartmentName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    @Transactional(readOnly = false)
    public GeneralConstants updateDepartment(UserActivityTO userActivityTO,Departments department) {
        GeneralConstants generalConstants = getDepartmentDAO().updateDepartment(department);
        userActivityTO.setActivityDescription("Edit department - ("+department.getDepartmentName()+") - "+generalConstants.getName());
        addUserActivity(userActivityTO);
        return generalConstants;
    }

    public Departments getDepartmentById(int id) {
        return getDepartmentDAO().getDepartment(id);
    }

    public List<Departments> getDepartmentsList() {
        logger.debug("inside getDepartmentsList method !!!");
        return getDepartmentDAO().getDepartmentsList();
    }

    /**
     * @return the departmentDAO
     */
    public IDepartmentDAO getDepartmentDAO() {
        return departmentDAO;
    }

    /**
     * @param departmentDAO the departmentDAO to set
     */
    public void setDepartmentDAO(IDepartmentDAO departmentDAO) {
        this.departmentDAO = departmentDAO;
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


