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
package com.web.coretix.home;

import com.module.coretix.commonto.CoreDashboardTO;
import com.module.coretix.coretix.ICoreDashboardService;
import com.module.coretix.license.ILicenseService;
import com.module.coretix.systemmanagement.IBranchService;
import com.module.coretix.systemmanagement.IDepartmentService;
import com.module.coretix.systemmanagement.IDesignationService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.systemmanagement.Branches;
import com.persist.coretix.modal.systemmanagement.Departments;
import com.persist.coretix.modal.systemmanagement.Designations;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.usermanagement.Roles;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.web.coretix.appgeneral.GenericManagedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Named("homePageBean")
@Scope("session")
public class HomePageBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private static final Logger logger = LoggerFactory.getLogger(HomePageBean.class);
    private static final Integer ALL_ORGANIZATIONS_ID = -1;
    private ResourceBundle resourceBundle;
    @Inject
    private transient ICoreDashboardService coreDashboardService;

    @Inject
    private transient ILicenseService licenseService;

    @Inject
    private transient IUserAdministrationService userAdministrationService;

    @Inject
    private transient IOrganizationService organizationService;

    @Inject
    private transient IBranchService branchService;

    @Inject
    private transient IDepartmentService departmentService;

    @Inject
    private transient IDesignationService designationService;

    @Inject
    private transient IRoleAdministrationService roleAdministrationService;

    @Inject
    private transient IUserActivityService userActivityService;

    private CoreDashboardTO coreDashboardTO;
    private Integer selectedOrganizationId;
    private List<Organizations> organizationList = new ArrayList<>();

    private boolean timerEnabled = true;  // Timer is enabled by default

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public void refreshForm() {
        coreDashboardTO = buildDashboardData();
        logger.info("Dashboard refreshed at: " + System.currentTimeMillis());
    }

    public void toggleTimer() {
        this.timerEnabled = !this.timerEnabled;  // Toggle the timer state
    }
//    private String memoryJson;

    public void initializePageAttributes() {
        resourceBundle = ResourceBundle.getBundle("coreAppMessages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        selectedOrganizationId = resolveDashboardOrganizationId(selectedOrganizationId);

        if (coreDashboardTO == null) {
            refreshForm();
            logger.info("Dashboard data loaded");
        }
    }

    public CoreDashboardTO getCoreDashboardTO() {
        return coreDashboardTO;
    }

    public void setCoreDashboardTO(CoreDashboardTO coreDashboardTO) {
        this.coreDashboardTO = coreDashboardTO;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = resolveDashboardOrganizationId(selectedOrganizationId);
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public String getSelectedOrganizationName() {
        if (isAllOrganizationsSelected()) {
            return "All Organizations";
        }
        if (selectedOrganizationId == null) {
            return getCurrentOrganizationName().isEmpty() ? "Organization" : getCurrentOrganizationName();
        }
        for (Organizations organization : organizationList) {
            if (organization != null && organization.getId() == selectedOrganizationId) {
                return organization.getOrganizationName();
            }
        }
        return "Organization";
    }

    public long getTotalStructureCount() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getOrganizationCount()
                + coreDashboardTO.getBranchCount()
                + coreDashboardTO.getDepartmentCount()
                + coreDashboardTO.getDesignationCount()
                + coreDashboardTO.getRoleCount();
    }

    public long getTotalGeographyCount() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getCountryCount()
                + coreDashboardTO.getStateCount()
                + coreDashboardTO.getCityCount()
                + coreDashboardTO.getCurrencyCount();
    }

    public long getTotalIdentityCount() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getUserCount()
                + coreDashboardTO.getUserActivityCount();
    }

    public int getTotalTrackedActivities() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getLoginCount()
                + coreDashboardTO.getLogoutCount()
                + coreDashboardTO.getAddCount()
                + coreDashboardTO.getUpdateCount()
                + coreDashboardTO.getDeleteCount();
    }

    public int getChangeActivityCount() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getAddCount()
                + coreDashboardTO.getUpdateCount()
                + coreDashboardTO.getDeleteCount();
    }

    public int getAuthenticationActivityCount() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getLoginCount() + coreDashboardTO.getLogoutCount();
    }

    public int getUserStatusTotal() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return coreDashboardTO.getUsersLoggedInCount()
                + coreDashboardTO.getUsersLoggedOutCount()
                + coreDashboardTO.getUsersNeverLoggedinCount();
    }

    public double getActiveUserRate() {
        return percentage(coreDashboardTO == null ? 0 : coreDashboardTO.getUsersLoggedInCount(), getUserStatusTotal());
    }

    public double getLoggedOutUserRate() {
        return percentage(coreDashboardTO == null ? 0 : coreDashboardTO.getUsersLoggedOutCount(), getUserStatusTotal());
    }

    public double getNeverLoggedInRate() {
        return percentage(coreDashboardTO == null ? 0 : coreDashboardTO.getUsersNeverLoggedinCount(), getUserStatusTotal());
    }

    public double getRoleUsageRate() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return percentage(coreDashboardTO.getRolesUsedCount(),
                coreDashboardTO.getRolesUsedCount() + coreDashboardTO.getRolesNotUsedCount());
    }

    public double getAuthenticationShare() {
        return percentage(getAuthenticationActivityCount(), getTotalTrackedActivities());
    }

    public double getChangeShare() {
        return percentage(getChangeActivityCount(), getTotalTrackedActivities());
    }

    public double getAverageBranchesPerOrganization() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getBranchCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getOrganizationCount());
    }

    public double getAverageDepartmentsPerOrganization() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getDepartmentCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getOrganizationCount());
    }

    public double getAverageDesignationsPerDepartment() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getDesignationCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getDepartmentCount());
    }

    public double getAverageUsersPerOrganization() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getUserCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getOrganizationCount());
    }

    public double getAverageUsersPerRole() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getUserCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getRoleCount());
    }

    public double getAverageActivitiesPerUser() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getUserActivityCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getUserCount());
    }

    public double getAverageStatesPerCountry() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getStateCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getCountryCount());
    }

    public double getAverageCitiesPerState() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getCityCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getStateCount());
    }

    public double getAverageActivitiesPerRole() {
        return ratio(coreDashboardTO == null ? 0 : coreDashboardTO.getUserActivityCount(),
                coreDashboardTO == null ? 0 : coreDashboardTO.getRoleCount());
    }

    public String getEntityDistributionJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d]]",
                jsLabel("organizationsLabel"),
                coreDashboardTO.getOrganizationCount(),
                jsLabel("branchesLabel"),
                coreDashboardTO.getBranchCount(),
                jsLabel("departmentsLabel"),
                coreDashboardTO.getDepartmentCount(),
                jsLabel("designationsLabel"),
                coreDashboardTO.getDesignationCount(),
                jsLabel("countriesLabel"),
                coreDashboardTO.getCountryCount(),
                jsLabel("statesLabel"),
                coreDashboardTO.getStateCount(),
                jsLabel("citiesLabel"),
                coreDashboardTO.getCityCount(),
                jsLabel("currenciesLabel"),
                coreDashboardTO.getCurrencyCount(),
                jsLabel("rolesLabel"),
                coreDashboardTO.getRoleCount(),
                jsLabel("usersLabel"),
                coreDashboardTO.getUserCount(),
                jsLabel("activitiesLabel"),
                coreDashboardTO.getUserActivityCount());
    }

    public String getUserStatusJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['%s', %d], ['%s', %d], ['%s', %d]]",
                jsLabel("chartLoggedInLabel"),
                coreDashboardTO.getUsersLoggedInCount(),
                jsLabel("chartLoggedOutLabel"),
                coreDashboardTO.getUsersLoggedOutCount(),
                jsLabel("neverLoggedInLabel"),
                coreDashboardTO.getUsersNeverLoggedinCount());
    }

    public String getRoleUsageJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['%s', %d], ['%s', %d]]",
                jsLabel("chartUsedRolesLabel"),
                coreDashboardTO.getRolesUsedCount(),
                jsLabel("chartUnusedRolesLabel"),
                coreDashboardTO.getRolesNotUsedCount());
    }

    public String getActivityMixJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d], ['%s', %d]]",
                jsLabel("loginLabel"),
                coreDashboardTO.getLoginCount(),
                jsLabel("logoutLabel"),
                coreDashboardTO.getLogoutCount(),
                jsLabel("addLabel"),
                coreDashboardTO.getAddCount(),
                jsLabel("updateLabel"),
                coreDashboardTO.getUpdateCount(),
                jsLabel("deleteLabel"),
                coreDashboardTO.getDeleteCount());
    }

    public String getPortfolioMixJson() {
        return String.format(Locale.US,
                "[['%s', %d], ['%s', %d], ['%s', %d]]",
                jsLabel("chartStructureLabel"),
                getTotalStructureCount(),
                jsLabel("chartGeographyLabel"),
                getTotalGeographyCount(),
                jsLabel("chartIdentityLabel"),
                getTotalIdentityCount());
    }

    public String getEfficiencyRadarJson() {
        return String.format(Locale.US,
                "[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f]",
                getAverageBranchesPerOrganization(),
                getAverageDepartmentsPerOrganization(),
                getAverageUsersPerOrganization(),
                getAverageUsersPerRole(),
                getAverageStatesPerCountry(),
                getAverageCitiesPerState());
    }

    public String getOperationsPulseJson() {
        return String.format(Locale.US,
                "[%.2f, %.2f, %.2f, %.2f]",
                getActiveUserRate(),
                getRoleUsageRate(),
                getAuthenticationShare(),
                getChangeShare());
    }

    public String getDashboardInsights() {
        String activeUserRate = String.format(Locale.US, "%.1f", getActiveUserRate());
        String roleUsageRate = String.format(Locale.US, "%.1f", getRoleUsageRate());
        String avgActivitiesPerUser = String.format(Locale.US, "%.2f", getAverageActivitiesPerUser());
        int activeLicenseCount = getActiveLicenseCount();

        if (!isAllOrganizationsSelected() && selectedOrganizationId != null) {
            return formatMessage("dashboardInsightsScopedTemplate",
                    getSelectedOrganizationName(),
                    activeUserRate,
                    roleUsageRate,
                    avgActivitiesPerUser,
                    activeLicenseCount);
        }
        return formatMessage("dashboardInsightsGlobalTemplate",
                activeUserRate,
                roleUsageRate,
                avgActivitiesPerUser,
                activeLicenseCount);
    }

    public int getLicensedOrganizationCount() {
        return getAllLicenses().size();
    }

    public int getActiveLicenseCount() {
        int activeCount = 0;
        for (Licenses license : getAllLicenses()) {
            if (license != null && license.isActive()) {
                activeCount++;
            }
        }
        return activeCount;
    }

    public int getExpiredLicenseCount() {
        int expiredCount = 0;
        for (Licenses license : getAllLicenses()) {
            if (license != null && !license.isActive()) {
                expiredCount++;
            }
        }
        return expiredCount;
    }

    public int getExpiringSoonLicenseCount() {
        int expiringSoonCount = 0;
        Date today = new Date();
        for (Licenses license : getAllLicenses()) {
            if (license == null || !license.isActive() || license.getEndDate() == null) {
                continue;
            }
            long daysRemaining = TimeUnit.MILLISECONDS.toDays(license.getEndDate().getTime() - today.getTime());
            if (daysRemaining <= 30) {
                expiringSoonCount++;
            }
        }
        return expiringSoonCount;
    }

    public int getLicensedUserCount() {
        Set<Integer> licensedOrganizationIds = new HashSet<>();
        for (Licenses license : getAllLicenses()) {
            if (license != null && license.getOrganization() != null) {
                licensedOrganizationIds.add(license.getOrganization().getId());
            }
        }

        int userCount = 0;
        for (UserDetails userDetails : userAdministrationService.getUserDetailsList()) {
            if (userDetails != null
                    && userDetails.getOrganization() != null
                    && licensedOrganizationIds.contains(userDetails.getOrganization().getId())) {
                userCount++;
            }
        }
        return userCount;
    }

    public int getUnlicensedOrganizationCount() {
        if (coreDashboardTO == null) {
            return 0;
        }
        return (int) Math.max(0L, coreDashboardTO.getOrganizationCount() - getLicensedOrganizationCount());
    }

    public double getLicenseCoverageRate() {
        return percentage(getActiveLicenseCount(), getLicensedOrganizationCount());
    }

    public String getLicenseSummary() {
        return formatMessage("licenseSummaryTemplate",
                getLicensedOrganizationCount(),
                getLicensedUserCount(),
                getActiveLicenseCount(),
                getExpiredLicenseCount(),
                getExpiringSoonLicenseCount());
    }

    private java.util.List<Licenses> getAllLicenses() {
        List<Licenses> licenses = licenseService.getLicenseList();
        if (isAllOrganizationsSelected()) {
            return licenses;
        }
        return licenses.stream()
                .filter(license -> license != null
                        && license.getOrganization() != null
                        && selectedOrganizationId != null
                        && license.getOrganization().getId() == selectedOrganizationId)
                .collect(Collectors.toList());
    }

    private CoreDashboardTO buildDashboardData() {
        if (isAllOrganizationsSelected()) {
            return coreDashboardService.fetchDashboardData();
        }

        CoreDashboardTO scopedDashboard = new CoreDashboardTO();
        Integer organizationId = selectedOrganizationId;
        if (organizationId == null) {
            return scopedDashboard;
        }

        List<UserDetails> scopedUsers = userAdministrationService.getUserDetailsList().stream()
                .filter(user -> user != null && user.getOrganization() != null && user.getOrganization().getId() == organizationId)
                .collect(Collectors.toList());
        Set<Integer> scopedUserIds = scopedUsers.stream().map(UserDetails::getUserId).collect(Collectors.toSet());

        List<Branches> scopedBranches = branchService.getBranchesListByOrgId(organizationId);
        List<Departments> scopedDepartments = departmentService.getDepartmentsList().stream()
                .filter(department -> department != null && department.getOrganization() != null && department.getOrganization().getId() == organizationId)
                .collect(Collectors.toList());
        List<Designations> scopedDesignations = designationService.getDesignationsList().stream()
                .filter(designation -> designation != null && designation.getOrganization() != null && designation.getOrganization().getId() == organizationId)
                .collect(Collectors.toList());
        List<UserActivities> scopedActivities = userActivityService.getUserActivitiesList().stream()
                .filter(activity -> activity != null && scopedUserIds.contains(activity.getUserId()))
                .collect(Collectors.toList());

        Set<Integer> scopedRoleIds = scopedUsers.stream()
                .filter(user -> user.getRole() != null)
                .map(user -> user.getRole().getId())
                .collect(Collectors.toSet());
        List<Roles> allRoles = roleAdministrationService.getRolesList();

        scopedDashboard.setOrganizationCount(1);
        scopedDashboard.setBranchCount(scopedBranches.size());
        scopedDashboard.setDepartmentCount(scopedDepartments.size());
        scopedDashboard.setDesignationCount(scopedDesignations.size());
        scopedDashboard.setCountryCount(coreDashboardService.fetchDashboardData().getCountryCount());
        scopedDashboard.setStateCount(coreDashboardService.fetchDashboardData().getStateCount());
        scopedDashboard.setCityCount(coreDashboardService.fetchDashboardData().getCityCount());
        scopedDashboard.setCurrencyCount(coreDashboardService.fetchDashboardData().getCurrencyCount());
        scopedDashboard.setRoleCount(scopedRoleIds.size());
        scopedDashboard.setUserCount(scopedUsers.size());
        scopedDashboard.setUserActivityCount(scopedActivities.size());

        scopedDashboard.setLoginCount(countActivities(scopedActivities, "login"));
        scopedDashboard.setLogoutCount(countActivities(scopedActivities, "logout"));
        scopedDashboard.setAddCount(countActivities(scopedActivities, "add"));
        scopedDashboard.setUpdateCount(countActivities(scopedActivities, "update"));
        scopedDashboard.setDeleteCount(countActivities(scopedActivities, "delete"));

        scopedDashboard.setUsersLoggedInCount((int) scopedUsers.stream().filter(user -> user.getStatus() == 1).count());
        scopedDashboard.setUsersLoggedOutCount((int) scopedUsers.stream().filter(user -> user.getStatus() == 2).count());
        scopedDashboard.setUsersNeverLoggedinCount((int) scopedUsers.stream().filter(user -> user.getLastSuccessfulLogin() == null).count());

        scopedDashboard.setRolesUsedCount(scopedRoleIds.size());
        scopedDashboard.setRolesNotUsedCount((int) allRoles.stream()
                .filter(role -> role != null && !scopedRoleIds.contains(role.getId()))
                .count());
        return scopedDashboard;
    }

    private int countActivities(List<UserActivities> activities, String type) {
        return (int) activities.stream()
                .filter(activity -> activity.getActivityType() != null && type.equalsIgnoreCase(activity.getActivityType()))
                .count();
    }

    private Integer resolveDashboardOrganizationId(Integer requestedOrganizationId) {
        if (isApplicationAdmin()) {
            if (ALL_ORGANIZATIONS_ID.equals(requestedOrganizationId)) {
                return ALL_ORGANIZATIONS_ID;
            }
            if (requestedOrganizationId != null) {
                return requestedOrganizationId;
            }
            return organizationList == null || organizationList.isEmpty() ? null : ALL_ORGANIZATIONS_ID;
        }
        return resolveAccessibleOrganizationId(requestedOrganizationId);
    }

    public boolean isAllOrganizationsSelected() {
        return isApplicationAdmin() && ALL_ORGANIZATIONS_ID.equals(selectedOrganizationId);
    }

    private double ratio(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return round(numerator / denominator);
    }

    private double percentage(double part, double total) {
        if (total <= 0) {
            return 0;
        }
        return round((part / total) * 100.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatMessage(String key, Object... args) {
        String pattern = localizedLabel(key);
        return MessageFormat.format(pattern, args);
    }

    private String jsLabel(String key) {
        return escapeForSingleQuotedJs(localizedLabel(key));
    }

    public String js(String key) {
        return jsLabel(key);
    }

    private String escapeForSingleQuotedJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private String localizedLabel(String key) {
        ResourceBundle bundle = resourceBundle;
        if (bundle == null) {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            bundle = ResourceBundle.getBundle("coreAppMessages", locale);
            resourceBundle = bundle;
        }
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }

    // Getter for the memory data as JSON
//    public String getMemoryJson() {
//        return memoryJson;
//    }
}




