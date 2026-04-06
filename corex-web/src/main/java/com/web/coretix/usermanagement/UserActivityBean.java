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
import com.module.coretix.systemmanagement.IOrganizationService;
import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.coretix.appgeneral.GenericManagedBean;

import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 * @author admin
 */
@Named("userActivityBean")
@Scope("session")
public class UserActivityBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(UserActivityBean.class);
    private static final Integer ALL_ORGANIZATIONS_ID = -1;
    private List<UserActivities> allUserActivityList = new ArrayList<>();
    private List<UserActivities> userActivityList = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();
    private List<UserDetails> filteredUserList = new ArrayList<>();
    private Integer selectedOrganizationId;
    private Integer selectedUserId;


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

    @Inject
    private IUserAdministrationService userAdministrationService;

    @Inject
    private IOrganizationService organizationService;

    /**
     * @return the userActivityList
     */
    public List<UserActivities> getUserActivityList() {
        return userActivityList;
    }


    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        initializeFilters();
        fetchUserActivityList();
        PrimeFaces.current().ajax().update("form:usrActMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }


    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchUserActivityList();
        logger.debug("end of searchButtonAction !!!");
    }


    private void fetchUserActivityList() {
        logger.debug("entered into fetchUserActivityList !!!");
        initializeFilters();
        allUserActivityList.clear();
        allUserActivityList.addAll(userActivityService.getUserActivitiesList());
        allUserActivityList.sort(Comparator.comparing(UserActivities::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        applyFilters();
    }

    public void organizationChanged() {
        if (!isApplicationAdmin()) {
            selectedOrganizationId = fetchCurrentOrganizationId();
        }
        selectedUserId = null;
        refreshFilteredUsers();
        applyFilters();
    }

    public void userChanged() {
        applyFilters();
    }

    private void initializeFilters() {
        organizationList = getAccessibleOrganizations(organizationService);
        if (isApplicationAdmin()) {
            if (selectedOrganizationId == null) {
                selectedOrganizationId = ALL_ORGANIZATIONS_ID;
            }
        } else {
            selectedOrganizationId = fetchCurrentOrganizationId();
        }
        refreshFilteredUsers();
        if (selectedUserId != null && filteredUserList.stream().noneMatch(user -> user.getUserId() == selectedUserId)) {
            selectedUserId = null;
        }
    }

    private void refreshFilteredUsers() {
        filteredUserList.clear();
        List<UserDetails> accessibleUsers = resolveAccessibleUsers();
        Integer effectiveOrganizationId = resolveEffectiveOrganizationId();
        for (UserDetails user : accessibleUsers) {
            Integer userOrganizationId = user.getOrganization() == null ? null : user.getOrganization().getId();
            if (effectiveOrganizationId == null || Objects.equals(userOrganizationId, effectiveOrganizationId)) {
                filteredUserList.add(user);
            }
        }
        filteredUserList.sort(Comparator.comparing(UserDetails::getUserName, String.CASE_INSENSITIVE_ORDER));
    }

    private List<UserDetails> resolveAccessibleUsers() {
        List<UserDetails> users = userAdministrationService.getUserDetailsList();
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }
        if (isApplicationAdmin()) {
            return new ArrayList<>(users);
        }
        Integer currentOrganizationId = fetchCurrentOrganizationId();
        return users.stream()
                .filter(user -> user.getOrganization() != null
                        && currentOrganizationId != null
                        && currentOrganizationId.equals(user.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    private void applyFilters() {
        loginCount = 0;
        logoutCount = 0;
        addCount = 0;
        updateCount = 0;
        deleteCount = 0;
        setDatatableRendered(false);
        setRecordsCount(0);
        userActivityList.clear();

        Integer effectiveOrganizationId = resolveEffectiveOrganizationId();
        Map<Integer, UserDetails> accessibleUsersById = resolveAccessibleUsers().stream()
                .collect(Collectors.toMap(UserDetails::getUserId, user -> user, (left, right) -> left));

        for (UserActivities activity : allUserActivityList) {
            UserDetails activityUser = accessibleUsersById.get(activity.getUserId());
            if (activityUser == null) {
                continue;
            }

            Integer userOrganizationId = activityUser.getOrganization() == null ? null : activityUser.getOrganization().getId();
            if (effectiveOrganizationId != null && !Objects.equals(effectiveOrganizationId, userOrganizationId)) {
                continue;
            }

            if (selectedUserId != null && activity.getUserId() != selectedUserId) {
                continue;
            }

            userActivityList.add(activity);
            incrementCounters(activity.getActivityType());
        }

        if (CollectionUtils.isNotEmpty(userActivityList)) {
            setDatatableRendered(true);
            setRecordsCount(userActivityList.size());
        }
    }

    private Integer resolveEffectiveOrganizationId() {
        if (isApplicationAdmin() && Objects.equals(selectedOrganizationId, ALL_ORGANIZATIONS_ID)) {
            return null;
        }
        return resolveAccessibleOrganizationId(selectedOrganizationId);
    }

    private void incrementCounters(String activityTypeValue) {
        String activityTypeUpper = activityTypeValue == null ? "" : activityTypeValue.trim().toUpperCase(Locale.ENGLISH);
        if (activityTypeUpper.contains("LOGIN")) {
            loginCount++;
        } else if (activityTypeUpper.contains("LOGOUT")) {
            logoutCount++;
        } else if (activityTypeUpper.contains("ADD") || activityTypeUpper.contains("CREATE")) {
            addCount++;
        } else if (activityTypeUpper.contains("EDIT") || activityTypeUpper.contains("UPDATE")) {
            updateCount++;
        } else if (activityTypeUpper.contains("DELETE") || activityTypeUpper.contains("REMOVE")) {
            deleteCount++;
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

    public int getUniqueUserCount() {
        if (CollectionUtils.isEmpty(userActivityList)) {
            return 0;
        }
        return (int) userActivityList.stream()
                .map(UserActivities::getUserName)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .count();
    }

    public int getUniqueIpCount() {
        if (CollectionUtils.isEmpty(userActivityList)) {
            return 0;
        }
        return (int) userActivityList.stream()
                .map(UserActivities::getIpAddress)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .count();
    }

    public int getSecurityEventCount() {
        return loginCount + logoutCount;
    }

    public int getChangeEventCount() {
        return addCount + updateCount + deleteCount;
    }

    public String getLatestActivityLabel() {
        if (CollectionUtils.isEmpty(userActivityList) || userActivityList.get(0).getCreatedAt() == null) {
            return "No activity recorded";
        }
        return userActivityList.get(0).getCreatedAt().toLocalDateTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getMostActiveUser() {
        if (CollectionUtils.isEmpty(userActivityList)) {
            return "-";
        }

        Map<String, Long> activityCounts = userActivityList.stream()
                .filter(activity -> activity.getUserName() != null && !activity.getUserName().trim().isEmpty())
                .collect(Collectors.groupingBy(UserActivities::getUserName, LinkedHashMap::new, Collectors.counting()));

        return activityCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("-");
    }

    public String getMostUsedActivityType() {
        if (CollectionUtils.isEmpty(userActivityList)) {
            return "-";
        }

        Map<String, Long> activityCounts = userActivityList.stream()
                .filter(activity -> activity.getActivityType() != null && !activity.getActivityType().trim().isEmpty())
                .collect(Collectors.groupingBy(activity -> activity.getActivityType().trim(),
                        LinkedHashMap::new, Collectors.counting()));

        return activityCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("-");
    }

    public List<UserActivities> getRecentActivities() {
        if (CollectionUtils.isEmpty(userActivityList)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userActivityList.subList(0, Math.min(6, userActivityList.size())));
    }

    public String getActivityMixSummary() {
        if (CollectionUtils.isEmpty(userActivityList)) {
            return "Load user activities to inspect the latest usage profile.";
        }

        return getSecurityEventCount() + " security events, "
                + getChangeEventCount() + " data-change events, across "
                + getUniqueUserCount() + " users and " + getUniqueIpCount() + " IP addresses.";
    }

    public String activityTone(String activityType) {
        if (activityType == null) {
            return "info";
        }

        String normalized = activityType.trim().toUpperCase(Locale.ENGLISH);
        if (normalized.contains("DELETE") || normalized.contains("REMOVE")) {
            return "danger";
        }
        if (normalized.contains("LOGIN") || normalized.contains("LOGOUT")) {
            return "info";
        }
        if (normalized.contains("ADD") || normalized.contains("CREATE")) {
            return "success";
        }
        if (normalized.contains("EDIT") || normalized.contains("UPDATE")) {
            return "warning";
        }
        return "secondary";
    }

    public String safeValue(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    public String getBrowserChartData() {
        Map<String, Long> browserCounts = new LinkedHashMap<>();
        for (UserActivities activity : userActivityList) {
            String browserName = resolveBrowserName(activity.getLocationInfo());
            browserCounts.put(browserName, browserCounts.getOrDefault(browserName, 0L) + 1L);
        }

        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, Long> entry : browserCounts.entrySet()) {
            if (!first) {
                builder.append(",");
            }
            builder.append("{name:'")
                    .append(escapeForJavascript(entry.getKey()))
                    .append("',y:")
                    .append(entry.getValue())
                    .append("}");
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }

    public String getActivityTimelineCategories() {
        Map<String, Long> hourlyCounts = new TreeMap<>();
        for (UserActivities activity : userActivityList) {
            if (activity.getCreatedAt() == null) {
                continue;
            }
            String label = activity.getCreatedAt().toLocalDateTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM HH:mm"));
            hourlyCounts.put(label, hourlyCounts.getOrDefault(label, 0L) + 1L);
        }

        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (String label : hourlyCounts.keySet()) {
            if (!first) {
                builder.append(",");
            }
            builder.append("'").append(escapeForJavascript(label)).append("'");
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }

    public String getActivityTimelineSeries() {
        Map<String, Long> hourlyCounts = new TreeMap<>();
        for (UserActivities activity : userActivityList) {
            if (activity.getCreatedAt() == null) {
                continue;
            }
            String label = activity.getCreatedAt().toLocalDateTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM HH:mm"));
            hourlyCounts.put(label, hourlyCounts.getOrDefault(label, 0L) + 1L);
        }

        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Long count : hourlyCounts.values()) {
            if (!first) {
                builder.append(",");
            }
            builder.append(count);
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }

    private String resolveBrowserName(String locationInfoValue) {
        String normalized = safeValue(locationInfoValue).toLowerCase(Locale.ENGLISH);
        if ("-".equals(normalized)) {
            return "Unknown";
        }
        if (normalized.contains("chrome")) {
            return "Chrome";
        }
        if (normalized.contains("edge")) {
            return "Edge";
        }
        if (normalized.contains("firefox")) {
            return "Firefox";
        }
        if (normalized.contains("safari")) {
            return "Safari";
        }
        if (normalized.contains("opera")) {
            return "Opera";
        }
        if (normalized.contains("internet explorer") || normalized.contains("trident") || normalized.contains("msie")) {
            return "Internet Explorer";
        }
        return "Other";
    }

    private String escapeForJavascript(String value) {
        return safeValue(value)
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", " ");
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public List<UserDetails> getFilteredUserList() {
        return filteredUserList;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = selectedOrganizationId;
    }

    public Integer getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(Integer selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public Integer getAllOrganizationsId() {
        return ALL_ORGANIZATIONS_ID;
    }

}




