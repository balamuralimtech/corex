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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

@Named("homePageBean")
@Scope("session")
public class HomePageBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private static final Logger logger = LoggerFactory.getLogger(HomePageBean.class);
    private ResourceBundle resourceBundle;
    @Inject
    private ICoreDashboardService coreDashboardService;

    private CoreDashboardTO coreDashboardTO;


    private boolean timerEnabled = true;  // Timer is enabled by default

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public void refreshForm() {
        coreDashboardTO = coreDashboardService.fetchDashboardData();
        logger.info("Dashboard refreshed at: " + System.currentTimeMillis());
    }

    public void toggleTimer() {
        this.timerEnabled = !this.timerEnabled;  // Toggle the timer state
    }
//    private String memoryJson;

    public void initializePageAttributes() {
        resourceBundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        // Only load dashboard data if not already loaded (cached in session)
        if (coreDashboardTO == null) {
            coreDashboardTO = coreDashboardService.fetchDashboardData();
            logger.info("Dashboard data loaded");
        }
    }

    public CoreDashboardTO getCoreDashboardTO() {
        return coreDashboardTO;
    }

    public void setCoreDashboardTO(CoreDashboardTO coreDashboardTO) {
        this.coreDashboardTO = coreDashboardTO;
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
                "[['Organizations', %d], ['Branches', %d], ['Departments', %d], ['Designations', %d], ['Countries', %d], ['States', %d], ['Cities', %d], ['Currencies', %d], ['Roles', %d], ['Users', %d], ['Activities', %d]]",
                coreDashboardTO.getOrganizationCount(),
                coreDashboardTO.getBranchCount(),
                coreDashboardTO.getDepartmentCount(),
                coreDashboardTO.getDesignationCount(),
                coreDashboardTO.getCountryCount(),
                coreDashboardTO.getStateCount(),
                coreDashboardTO.getCityCount(),
                coreDashboardTO.getCurrencyCount(),
                coreDashboardTO.getRoleCount(),
                coreDashboardTO.getUserCount(),
                coreDashboardTO.getUserActivityCount());
    }

    public String getUserStatusJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['Logged In', %d], ['Logged Out', %d], ['Never Logged In', %d]]",
                coreDashboardTO.getUsersLoggedInCount(),
                coreDashboardTO.getUsersLoggedOutCount(),
                coreDashboardTO.getUsersNeverLoggedinCount());
    }

    public String getRoleUsageJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['Used Roles', %d], ['Unused Roles', %d]]",
                coreDashboardTO.getRolesUsedCount(),
                coreDashboardTO.getRolesNotUsedCount());
    }

    public String getActivityMixJson() {
        if (coreDashboardTO == null) {
            return "[]";
        }
        return String.format(Locale.US,
                "[['Login', %d], ['Logout', %d], ['Add', %d], ['Update', %d], ['Delete', %d]]",
                coreDashboardTO.getLoginCount(),
                coreDashboardTO.getLogoutCount(),
                coreDashboardTO.getAddCount(),
                coreDashboardTO.getUpdateCount(),
                coreDashboardTO.getDeleteCount());
    }

    public String getPortfolioMixJson() {
        return String.format(Locale.US,
                "[['Structure', %d], ['Geography', %d], ['Identity', %d]]",
                getTotalStructureCount(),
                getTotalGeographyCount(),
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
        return String.format(Locale.US,
                "Active users are %.1f%% of the tracked user base. Roles are utilized at %.1f%%, and each user generates %.2f tracked activities on average.",
                getActiveUserRate(),
                getRoleUsageRate(),
                getAverageActivitiesPerUser());
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

    // Getter for the memory data as JSON
//    public String getMemoryJson() {
//        return memoryJson;
//    }
}





