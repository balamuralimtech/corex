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
package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.CoreDashboardTO;
import com.module.coretix.commonto.RoleUsageCountTO;
import com.module.coretix.commonto.UserActivitiesCountTO;
import com.module.coretix.commonto.UsersStatusCountTO;
import com.module.coretix.coretix.ICoreDashboardService;
import com.persist.coretix.modal.coretix.dao.ICoreDashboardDAO;
import com.persist.coretix.modal.usermanagement.dao.IRoleAdministrationDAO;
import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
import com.persist.coretix.modal.usermanagement.dao.IUserAdministrationDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Map;

@Named
@Transactional(readOnly = true)
public class CoreDashboardService implements ICoreDashboardService {

    @Inject
    private ICoreDashboardDAO coreDashboardDAO;

    @Inject
    private IUserAdministrationDAO userAdministrationDAO;

    @Inject
    private IRoleAdministrationDAO roleAdministrationDAO;

    @Inject
    private IUserActivityDAO userActivityDAO;

    @Transactional(readOnly = false)
    public CoreDashboardTO fetchDashboardData() {
        CoreDashboardTO coreDashboardTO = new CoreDashboardTO();

        Map<String, Long> entityCounts = coreDashboardDAO.fetchDashboardEntityCounts();
        coreDashboardTO.setOrganizationCount(getLongCount(entityCounts, "organizations"));
        coreDashboardTO.setBranchCount(getLongCount(entityCounts, "branches"));
        coreDashboardTO.setCountryCount(getLongCount(entityCounts, "countries"));
        coreDashboardTO.setStateCount(getLongCount(entityCounts, "states"));
        coreDashboardTO.setCityCount(getLongCount(entityCounts, "cities"));
        coreDashboardTO.setCurrencyCount(getLongCount(entityCounts, "currencies"));
        coreDashboardTO.setDepartmentCount(getLongCount(entityCounts, "departments"));
        coreDashboardTO.setDesignationCount(getLongCount(entityCounts, "designations"));
        coreDashboardTO.setRoleCount(getLongCount(entityCounts, "roles"));
        coreDashboardTO.setUserCount(getLongCount(entityCounts, "users"));
        coreDashboardTO.setUserActivityCount(getLongCount(entityCounts, "userActivities"));

        Map<String, BigDecimal> operationalMetrics = coreDashboardDAO.fetchDashboardOperationalMetrics();
        coreDashboardTO.setDisabledUserCount(getLongMetric(operationalMetrics, "disabledUsers"));
        coreDashboardTO.setLockedUserCount(getLongMetric(operationalMetrics, "lockedUsers"));
        coreDashboardTO.setStalePasswordUserCount(getLongMetric(operationalMetrics, "stalePasswordUsers"));
        coreDashboardTO.setInactiveUser30DaysCount(getLongMetric(operationalMetrics, "inactiveUsers30Days"));
        coreDashboardTO.setActivityLast7DaysCount(getLongMetric(operationalMetrics, "activityLast7Days"));
        coreDashboardTO.setActivityLast30DaysCount(getLongMetric(operationalMetrics, "activityLast30Days"));
        coreDashboardTO.setUniqueActiveUsers30DaysCount(getLongMetric(operationalMetrics, "uniqueActiveUsers30Days"));
        coreDashboardTO.setLicensesExpiring7DaysCount(getLongMetric(operationalMetrics, "licensesExpiring7Days"));
        coreDashboardTO.setLicensesExpiring15DaysCount(getLongMetric(operationalMetrics, "licensesExpiring15Days"));
        coreDashboardTO.setLicensesExpiring30DaysCount(getLongMetric(operationalMetrics, "licensesExpiring30Days"));
        coreDashboardTO.setExpiredLicensesMetricCount(getLongMetric(operationalMetrics, "expiredLicenses"));
        coreDashboardTO.setAverageLicenseDaysRemaining(getDecimalMetric(operationalMetrics, "avgLicenseDaysRemaining"));
        coreDashboardTO.setDemoRequestsTotalCount(getLongMetric(operationalMetrics, "demoRequestsTotal"));
        coreDashboardTO.setDemoRequestsPendingCount(getLongMetric(operationalMetrics, "demoRequestsPending"));
        coreDashboardTO.setDemoRequestsCompletedCount(getLongMetric(operationalMetrics, "demoRequestsCompleted"));
        coreDashboardTO.setAverageDemoCompletionHours(getDecimalMetric(operationalMetrics, "avgDemoCompletionHours"));
        coreDashboardTO.setNotificationsTotalCount(getLongMetric(operationalMetrics, "notificationsTotal"));
        coreDashboardTO.setNotificationReceiptsTotalCount(getLongMetric(operationalMetrics, "notificationReceiptsTotal"));
        coreDashboardTO.setNotificationUnseenTotalCount(getLongMetric(operationalMetrics, "notificationUnseenTotal"));
        coreDashboardTO.setLatestNotificationAgeHours(getDecimalMetric(operationalMetrics, "latestNotificationAgeHours"));
        coreDashboardTO.setActiveReferrersCount(getLongMetric(operationalMetrics, "activeReferrers"));
        coreDashboardTO.setReferralAttributionsTotalCount(getLongMetric(operationalMetrics, "referralAttributionsTotal"));
        coreDashboardTO.setReferralSubscriptionAmount(getDecimalMetric(operationalMetrics, "referralSubscriptionAmount"));
        coreDashboardTO.setReferralCommissionPendingAmount(getDecimalMetric(operationalMetrics, "referralCommissionPendingAmount"));
        coreDashboardTO.setReferralCommissionPaidAmount(getDecimalMetric(operationalMetrics, "referralCommissionPaidAmount"));
        coreDashboardTO.setChatConversationsTotalCount(getLongMetric(operationalMetrics, "chatConversationsTotal"));
        coreDashboardTO.setChatMessagesLast24HoursCount(getLongMetric(operationalMetrics, "chatMessagesLast24Hours"));
        coreDashboardTO.setChatMessagesLast7DaysCount(getLongMetric(operationalMetrics, "chatMessagesLast7Days"));
        coreDashboardTO.setActiveChatUsers7DaysCount(getLongMetric(operationalMetrics, "activeChatUsers7Days"));

        Map<String, Integer> activityTypeCounts = getUserActivityDAO().getActivityTypeCounts();
        coreDashboardTO.setLoginCount(getCount(activityTypeCounts, "login"));
        coreDashboardTO.setLogoutCount(getCount(activityTypeCounts, "logout"));
        coreDashboardTO.setAddCount(getCount(activityTypeCounts, "add"));
        coreDashboardTO.setUpdateCount(getCount(activityTypeCounts, "update"));
        coreDashboardTO.setDeleteCount(getCount(activityTypeCounts, "delete"));

        coreDashboardTO.setUsersLoggedInCount(getUserAdministrationDAO().getCountOfUsersLoggedIn());
        coreDashboardTO.setUsersLoggedOutCount(getUserAdministrationDAO().getCountOfUsersLoggedOut());
        coreDashboardTO.setUsersNeverLoggedinCount(getUserAdministrationDAO().getCountOfUsersNeverLoggedIn());

        Map<String, Integer> roleUsageCounts = getRoleAdministrationDAO().getCountOfRolesUsedAndNotUsed();
        coreDashboardTO.setRolesNotUsedCount(getCount(roleUsageCounts, "notUsedRoles"));
        coreDashboardTO.setRolesUsedCount(getCount(roleUsageCounts, "usedRoles"));

        return coreDashboardTO;

    }

    private int getCount(Map<String, Integer> counts, String key) {
        if (counts == null) {
            return 0;
        }
        Integer value = counts.get(key);
        return value == null ? 0 : value;
    }

    private long getLongCount(Map<String, Long> counts, String key) {
        if (counts == null) {
            return 0L;
        }
        Long value = counts.get(key);
        return value == null ? 0L : value;
    }

    private long getLongMetric(Map<String, BigDecimal> metrics, String key) {
        return getDecimalMetric(metrics, key).longValue();
    }

    private BigDecimal getDecimalMetric(Map<String, BigDecimal> metrics, String key) {
        if (metrics == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = metrics.get(key);
        return value == null ? BigDecimal.ZERO : value;
    }

    public ICoreDashboardDAO getCoreDashboardDAO() {
        return coreDashboardDAO;
    }

    public void setCoreDashboardDAO(ICoreDashboardDAO coreDashboardDAO) {
        this.coreDashboardDAO = coreDashboardDAO;
    }

    public IUserAdministrationDAO getUserAdministrationDAO() {
        return userAdministrationDAO;
    }

    public void setUserAdministrationDAO(IUserAdministrationDAO userAdministrationDAO) {
        this.userAdministrationDAO = userAdministrationDAO;
    }

    public IRoleAdministrationDAO getRoleAdministrationDAO() {
        return roleAdministrationDAO;
    }

    public void setRoleAdministrationDAO(IRoleAdministrationDAO roleAdministrationDAO) {
        this.roleAdministrationDAO = roleAdministrationDAO;
    }

    public IUserActivityDAO getUserActivityDAO() {
        return userActivityDAO;
    }


}


