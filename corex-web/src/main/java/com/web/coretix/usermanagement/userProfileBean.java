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

import com.module.coretix.usermanagement.IUserActivityService;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.module.coretix.usermanagement.IUserAdministrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.web.coretix.constants.AccessRightConstants;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.general.SessionUtils;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpSession;

@Named("userProfileBean")
@Scope("session")
public class userProfileBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private static final Logger logger = LoggerFactory.getLogger(userProfileBean.class);
    
    @Inject
    private transient IUserAdministrationService userAdministrationService;

    @Inject
    private transient IUserActivityService userActivityService;

    private UserDetails userDetails;
    private String accessRight;
    private List<UserActivities> profileActivities = new ArrayList<>();

    public void initializePageAttributes() {
        HttpSession session = SessionUtils.getSession();
        Integer userId = session == null
                ? null
                : (Integer) session.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());

        if (userId == null) {
            logger.warn("No user id found in session while loading user profile");
            userDetails = null;
            accessRight = null;
            profileActivities = Collections.emptyList();
            return;
        }

        userDetails = userAdministrationService.getUserDetailById(userId);
        if (userDetails == null) {
            logger.warn("No user details found for user id {}", userId);
            accessRight = null;
            profileActivities = Collections.emptyList();
            return;
        }

        accessRight = AccessRightConstants.getById(userDetails.getAccessRight()).getValue();
        loadProfileActivities(userId);
        logger.debug("{}", userDetails.getUserName());
        logger.debug("accessRight : " + accessRight);

    }

    private void loadProfileActivities(Integer userId) {
        List<UserActivities> filteredActivities = new ArrayList<>();
        List<UserActivities> allActivities = userActivityService.getUserActivitiesList();

        if (allActivities != null) {
            for (UserActivities activity : allActivities) {
                if (activity != null && activity.getUserId() == userId) {
                    filteredActivities.add(activity);
                }
            }
        }

        filteredActivities.sort(new Comparator<UserActivities>() {
            @Override
            public int compare(UserActivities left, UserActivities right) {
                if (left.getCreatedAt() == null && right.getCreatedAt() == null) {
                    return 0;
                }
                if (left.getCreatedAt() == null) {
                    return 1;
                }
                if (right.getCreatedAt() == null) {
                    return -1;
                }
                return right.getCreatedAt().compareTo(left.getCreatedAt());
            }
        });

        if (filteredActivities.size() > 10) {
            profileActivities = new ArrayList<>(filteredActivities.subList(0, 10));
        } else {
            profileActivities = filteredActivities;
        }
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }


    public String getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(String accessRight) {
        this.accessRight = accessRight;
    };

    public List<UserActivities> getProfileActivities() {
        return profileActivities;
    }

    public String activityIcon(String activityType) {
        if (activityType == null) {
            return "pi pi-history";
        }

        String normalizedType = activityType.trim().toLowerCase();
        switch (normalizedType) {
            case "login":
                return "pi pi-sign-in";
            case "logout":
                return "pi pi-sign-out";
            case "add":
                return "pi pi-plus-circle";
            case "update":
                return "pi pi-pencil";
            case "delete":
                return "pi pi-trash";
            default:
                return "pi pi-clock";
        }
    }

    public String activityTitle(UserActivities activity) {
        if (activity == null || activity.getActivityType() == null || activity.getActivityType().trim().isEmpty()) {
            return "Activity";
        }

        String normalizedType = activity.getActivityType().trim().toLowerCase();
        switch (normalizedType) {
            case "login":
                return "Signed in";
            case "logout":
                return "Signed out";
            case "add":
                return "Created a record";
            case "update":
                return "Updated account data";
            case "delete":
                return "Deleted a record";
            default:
                return activity.getActivityType();
        }
    }

    public String activitySummary(UserActivities activity) {
        if (activity == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder();

        if (activity.getActivityDescription() != null && !activity.getActivityDescription().trim().isEmpty()) {
            summary.append(activity.getActivityDescription().trim());
        } else {
            summary.append(activityTitle(activity));
        }

        if (activity.getDeviceInfo() != null && !activity.getDeviceInfo().trim().isEmpty()) {
            summary.append(" | Device: ").append(activity.getDeviceInfo().trim());
        }

        if (activity.getIpAddress() != null && !activity.getIpAddress().trim().isEmpty()) {
            summary.append(" | IP: ").append(activity.getIpAddress().trim());
        }

        if (activity.getLocationInfo() != null && !activity.getLocationInfo().trim().isEmpty()) {
            summary.append(" | Location: ").append(activity.getLocationInfo().trim());
        }

        if (activity.getTerminationReason() != null && !activity.getTerminationReason().trim().isEmpty()) {
            summary.append(" | Reason: ").append(activity.getTerminationReason().trim());
        }

        return summary.toString();
    }

    public String getProfileImageSrc() {
        if (userDetails == null || userDetails.getProfileImage() == null || userDetails.getProfileImage().length == 0
                || userDetails.getProfileImageContentType() == null) {
            return null;
        }

        return "data:" + userDetails.getProfileImageContentType() + ";base64,"
                + Base64.getEncoder().encodeToString(userDetails.getProfileImage());
    }

    public String getOrganizationName() {
        if (userDetails == null || userDetails.getOrganization() == null) {
            return "Application-wide access";
        }
        return userDetails.getOrganization().getOrganizationName();
    }

    public String getBranchName() {
        if (userDetails == null || userDetails.getBranch() == null) {
            return "Not assigned";
        }
        return userDetails.getBranch().getBranchName();
    }

    public String getContactValue() {
        if (userDetails == null || userDetails.getContact() == null || userDetails.getContact().trim().isEmpty()) {
            return "Not provided";
        }
        return userDetails.getContact();
    }

    public String getAddressSummary() {
        if (userDetails == null) {
            return "Not provided";
        }

        List<String> parts = new ArrayList<>();
        if (userDetails.getAddress() != null && !userDetails.getAddress().trim().isEmpty()) {
            parts.add(userDetails.getAddress().trim());
        }
        if (userDetails.getCity() != null && userDetails.getCity().getName() != null && !userDetails.getCity().getName().trim().isEmpty()) {
            parts.add(userDetails.getCity().getName().trim());
        }
        if (userDetails.getState() != null && userDetails.getState().getName() != null && !userDetails.getState().getName().trim().isEmpty()) {
            parts.add(userDetails.getState().getName().trim());
        }
        if (userDetails.getCountry() != null && userDetails.getCountry().getName() != null && !userDetails.getCountry().getName().trim().isEmpty()) {
            parts.add(userDetails.getCountry().getName().trim());
        }

        return parts.isEmpty() ? "Not provided" : String.join(", ", parts);
    }

}




