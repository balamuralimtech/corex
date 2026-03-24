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
package com.web.coretix.usermanagement;

import com.module.coretix.commonto.UserActivitiesCountTO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.module.coretix.usermanagement.IUserActivityService;

import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 * @author admin
 */
@Named("userActivityBean")
@Scope("session")
public class UserActivityBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(UserActivityBean.class);
    private List<UserActivities> userActivityList = new ArrayList<>();


    private int userId;
    private String activityType;
    private String activityDescription;
    private String ipAddress;
    private String deviceInfo;
    private String locationInfo;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private boolean datatableRendered;
    private int recordsCount;

    private int loginCount;
    private int logoutCount;
    private int addCount;
    private int updateCount;
    private int deleteCount;

    private Timestamp currentDateTime = new Timestamp(System.currentTimeMillis());

    @Inject
    private IUserActivityService userActivityService;

    /**
     * @return the userActivityList
     */
    public List<UserActivities> getUserActivityList() {
        return userActivityList;
    }


    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        setDatatableRendered(false);
        setRecordsCount(0);

        if (CollectionUtils.isNotEmpty(userActivityList)) {
            logger.debug("inside  userActivityList clear");
            userActivityList.clear();
        }

        PrimeFaces.current().ajax().update("form:usrActMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }


    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchUserActivityList();
        logger.debug("end of searchButtonAction !!!");
    }


    private void fetchUserActivityList() {
        loginCount = 0;
        logoutCount = 0;
        addCount = 0;
        updateCount = 0;
        deleteCount = 0;
        logger.debug("entered into fetchUserActivityList !!!");

        setDatatableRendered(false);
        logger.debug("inside fetchUserActivityList ");
        if (CollectionUtils.isNotEmpty(userActivityList)) {
            logger.debug("inside fetchUserActivityList clear");
            userActivityList.clear();
        }
        userActivityList.addAll(userActivityService.getUserActivitiesList());

        if (CollectionUtils.isNotEmpty(userActivityList)) {
            logger.debug("userActivityList.size() : " + userActivityList.size());
            setDatatableRendered(true);
            setRecordsCount(userActivityList.size());

            UserActivitiesCountTO userActivitiesCountTO = userActivityService.getActivityTypeCounts();

            loginCount = userActivitiesCountTO.getLoginCount();
            logoutCount = userActivitiesCountTO.getLogoutCount();
            addCount = userActivitiesCountTO.getAddCount();
            updateCount = userActivitiesCountTO.getUpdateCount();
            deleteCount = userActivitiesCountTO.getDeleteCount();

            logger.debug("loginCount "+loginCount);
            logger.debug("logoutCount "+logoutCount);
            logger.debug("addCount "+addCount);
            logger.debug("updateCount "+updateCount);
            logger.debug("deleteCount "+deleteCount);

        }
    }


    /**
     * @return the datatableRendered
     */
    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    /**
     * @param datatableRendered the datatableRendered to set
     */
    public void setDatatableRendered(boolean datatableRendered) {
        this.datatableRendered = datatableRendered;
    }

    /**
     * @return the recordsCount
     */
    public int getRecordsCount() {
        return recordsCount;
    }

    /**
     * @param recordsCount the recordsCount to set
     */
    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(Timestamp currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public int getLogoutCount() {
        return logoutCount;
    }

    public void setLogoutCount(int logoutCount) {
        this.logoutCount = logoutCount;
    }

    public int getAddCount() {
        return addCount;
    }

    public void setAddCount(int addCount) {
        this.addCount = addCount;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public void setDeleteCount(int deleteCount) {
        this.deleteCount = deleteCount;
    }

}




