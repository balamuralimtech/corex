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
package com.module.coretix.commonto;

import java.math.BigDecimal;

public class CoreDashboardTO {

    private long organizationCount;
    private long branchCount;
    private long departmentCount;
    private long designationCount;
    private long countryCount;
    private long stateCount;
    private long cityCount;
    private long currencyCount;
    private long roleCount;
    private long userCount;
    private long userActivityCount;

    private int loginCount;
    private int logoutCount;
    private int addCount;
    private int updateCount;
    private int deleteCount;

    private int usersLoggedInCount;
    private int usersLoggedOutCount;
    private int usersNeverLoggedinCount;

    private int rolesUsedCount;
    private int rolesNotUsedCount;

    private long disabledUserCount;
    private long lockedUserCount;
    private long stalePasswordUserCount;
    private long inactiveUser30DaysCount;

    private long activityLast7DaysCount;
    private long activityLast30DaysCount;
    private long uniqueActiveUsers30DaysCount;

    private long licensesExpiring7DaysCount;
    private long licensesExpiring15DaysCount;
    private long licensesExpiring30DaysCount;
    private long expiredLicensesMetricCount;
    private BigDecimal averageLicenseDaysRemaining = BigDecimal.ZERO;

    private long demoRequestsTotalCount;
    private long demoRequestsPendingCount;
    private long demoRequestsCompletedCount;
    private BigDecimal averageDemoCompletionHours = BigDecimal.ZERO;

    private long notificationsTotalCount;
    private long notificationReceiptsTotalCount;
    private long notificationUnseenTotalCount;
    private BigDecimal latestNotificationAgeHours = BigDecimal.ZERO;

    private long activeReferrersCount;
    private long referralAttributionsTotalCount;
    private BigDecimal referralSubscriptionAmount = BigDecimal.ZERO;
    private BigDecimal referralCommissionPendingAmount = BigDecimal.ZERO;
    private BigDecimal referralCommissionPaidAmount = BigDecimal.ZERO;

    private long chatConversationsTotalCount;
    private long chatMessagesLast24HoursCount;
    private long chatMessagesLast7DaysCount;
    private long activeChatUsers7DaysCount;

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

    public int getUsersLoggedInCount() {
        return usersLoggedInCount;
    }

    public void setUsersLoggedInCount(int usersLoggedInCount) {
        this.usersLoggedInCount = usersLoggedInCount;
    }

    public int getUsersLoggedOutCount() {
        return usersLoggedOutCount;
    }

    public void setUsersLoggedOutCount(int usersLoggedOutCount) {
        this.usersLoggedOutCount = usersLoggedOutCount;
    }

    public int getUsersNeverLoggedinCount() {
        return usersNeverLoggedinCount;
    }

    public void setUsersNeverLoggedinCount(int usersNeverLoggedinCount) {
        this.usersNeverLoggedinCount = usersNeverLoggedinCount;
    }

    public int getRolesUsedCount() {
        return rolesUsedCount;
    }

    public void setRolesUsedCount(int rolesUsedCount) {
        this.rolesUsedCount = rolesUsedCount;
    }

    public int getRolesNotUsedCount() {
        return rolesNotUsedCount;
    }

    public void setRolesNotUsedCount(int rolesNotUsedCount) {
        this.rolesNotUsedCount = rolesNotUsedCount;
    }

    public long getOrganizationCount() {
        return organizationCount;
    }

    public void setOrganizationCount(long organizationCount) {
        this.organizationCount = organizationCount;
    }

    public long getBranchCount() {
        return branchCount;
    }

    public void setBranchCount(long branchCount) {
        this.branchCount = branchCount;
    }

    public long getDepartmentCount() {
        return departmentCount;
    }

    public void setDepartmentCount(long departmentCount) {
        this.departmentCount = departmentCount;
    }

    public long getDesignationCount() {
        return designationCount;
    }

    public void setDesignationCount(long designationCount) {
        this.designationCount = designationCount;
    }

    public long getCountryCount() {
        return countryCount;
    }

    public void setCountryCount(long countryCount) {
        this.countryCount = countryCount;
    }

    public long getStateCount() {
        return stateCount;
    }

    public void setStateCount(long stateCount) {
        this.stateCount = stateCount;
    }

    public long getCityCount() {
        return cityCount;
    }

    public void setCityCount(long cityCount) {
        this.cityCount = cityCount;
    }

    public long getCurrencyCount() {
        return currencyCount;
    }

    public void setCurrencyCount(long currencyCount) {
        this.currencyCount = currencyCount;
    }

    public long getRoleCount() {
        return roleCount;
    }

    public void setRoleCount(long roleCount) {
        this.roleCount = roleCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getUserActivityCount() {
        return userActivityCount;
    }

    public void setUserActivityCount(long userActivityCount) {
        this.userActivityCount = userActivityCount;
    }

    public long getDisabledUserCount() {
        return disabledUserCount;
    }

    public void setDisabledUserCount(long disabledUserCount) {
        this.disabledUserCount = disabledUserCount;
    }

    public long getLockedUserCount() {
        return lockedUserCount;
    }

    public void setLockedUserCount(long lockedUserCount) {
        this.lockedUserCount = lockedUserCount;
    }

    public long getStalePasswordUserCount() {
        return stalePasswordUserCount;
    }

    public void setStalePasswordUserCount(long stalePasswordUserCount) {
        this.stalePasswordUserCount = stalePasswordUserCount;
    }

    public long getInactiveUser30DaysCount() {
        return inactiveUser30DaysCount;
    }

    public void setInactiveUser30DaysCount(long inactiveUser30DaysCount) {
        this.inactiveUser30DaysCount = inactiveUser30DaysCount;
    }

    public long getActivityLast7DaysCount() {
        return activityLast7DaysCount;
    }

    public void setActivityLast7DaysCount(long activityLast7DaysCount) {
        this.activityLast7DaysCount = activityLast7DaysCount;
    }

    public long getActivityLast30DaysCount() {
        return activityLast30DaysCount;
    }

    public void setActivityLast30DaysCount(long activityLast30DaysCount) {
        this.activityLast30DaysCount = activityLast30DaysCount;
    }

    public long getUniqueActiveUsers30DaysCount() {
        return uniqueActiveUsers30DaysCount;
    }

    public void setUniqueActiveUsers30DaysCount(long uniqueActiveUsers30DaysCount) {
        this.uniqueActiveUsers30DaysCount = uniqueActiveUsers30DaysCount;
    }

    public long getLicensesExpiring7DaysCount() {
        return licensesExpiring7DaysCount;
    }

    public void setLicensesExpiring7DaysCount(long licensesExpiring7DaysCount) {
        this.licensesExpiring7DaysCount = licensesExpiring7DaysCount;
    }

    public long getLicensesExpiring15DaysCount() {
        return licensesExpiring15DaysCount;
    }

    public void setLicensesExpiring15DaysCount(long licensesExpiring15DaysCount) {
        this.licensesExpiring15DaysCount = licensesExpiring15DaysCount;
    }

    public long getLicensesExpiring30DaysCount() {
        return licensesExpiring30DaysCount;
    }

    public void setLicensesExpiring30DaysCount(long licensesExpiring30DaysCount) {
        this.licensesExpiring30DaysCount = licensesExpiring30DaysCount;
    }

    public long getExpiredLicensesMetricCount() {
        return expiredLicensesMetricCount;
    }

    public void setExpiredLicensesMetricCount(long expiredLicensesMetricCount) {
        this.expiredLicensesMetricCount = expiredLicensesMetricCount;
    }

    public BigDecimal getAverageLicenseDaysRemaining() {
        return averageLicenseDaysRemaining;
    }

    public void setAverageLicenseDaysRemaining(BigDecimal averageLicenseDaysRemaining) {
        this.averageLicenseDaysRemaining = averageLicenseDaysRemaining == null ? BigDecimal.ZERO : averageLicenseDaysRemaining;
    }

    public long getDemoRequestsTotalCount() {
        return demoRequestsTotalCount;
    }

    public void setDemoRequestsTotalCount(long demoRequestsTotalCount) {
        this.demoRequestsTotalCount = demoRequestsTotalCount;
    }

    public long getDemoRequestsPendingCount() {
        return demoRequestsPendingCount;
    }

    public void setDemoRequestsPendingCount(long demoRequestsPendingCount) {
        this.demoRequestsPendingCount = demoRequestsPendingCount;
    }

    public long getDemoRequestsCompletedCount() {
        return demoRequestsCompletedCount;
    }

    public void setDemoRequestsCompletedCount(long demoRequestsCompletedCount) {
        this.demoRequestsCompletedCount = demoRequestsCompletedCount;
    }

    public BigDecimal getAverageDemoCompletionHours() {
        return averageDemoCompletionHours;
    }

    public void setAverageDemoCompletionHours(BigDecimal averageDemoCompletionHours) {
        this.averageDemoCompletionHours = averageDemoCompletionHours == null ? BigDecimal.ZERO : averageDemoCompletionHours;
    }

    public long getNotificationsTotalCount() {
        return notificationsTotalCount;
    }

    public void setNotificationsTotalCount(long notificationsTotalCount) {
        this.notificationsTotalCount = notificationsTotalCount;
    }

    public long getNotificationReceiptsTotalCount() {
        return notificationReceiptsTotalCount;
    }

    public void setNotificationReceiptsTotalCount(long notificationReceiptsTotalCount) {
        this.notificationReceiptsTotalCount = notificationReceiptsTotalCount;
    }

    public long getNotificationUnseenTotalCount() {
        return notificationUnseenTotalCount;
    }

    public void setNotificationUnseenTotalCount(long notificationUnseenTotalCount) {
        this.notificationUnseenTotalCount = notificationUnseenTotalCount;
    }

    public BigDecimal getLatestNotificationAgeHours() {
        return latestNotificationAgeHours;
    }

    public void setLatestNotificationAgeHours(BigDecimal latestNotificationAgeHours) {
        this.latestNotificationAgeHours = latestNotificationAgeHours == null ? BigDecimal.ZERO : latestNotificationAgeHours;
    }

    public long getActiveReferrersCount() {
        return activeReferrersCount;
    }

    public void setActiveReferrersCount(long activeReferrersCount) {
        this.activeReferrersCount = activeReferrersCount;
    }

    public long getReferralAttributionsTotalCount() {
        return referralAttributionsTotalCount;
    }

    public void setReferralAttributionsTotalCount(long referralAttributionsTotalCount) {
        this.referralAttributionsTotalCount = referralAttributionsTotalCount;
    }

    public BigDecimal getReferralSubscriptionAmount() {
        return referralSubscriptionAmount;
    }

    public void setReferralSubscriptionAmount(BigDecimal referralSubscriptionAmount) {
        this.referralSubscriptionAmount = referralSubscriptionAmount == null ? BigDecimal.ZERO : referralSubscriptionAmount;
    }

    public BigDecimal getReferralCommissionPendingAmount() {
        return referralCommissionPendingAmount;
    }

    public void setReferralCommissionPendingAmount(BigDecimal referralCommissionPendingAmount) {
        this.referralCommissionPendingAmount = referralCommissionPendingAmount == null ? BigDecimal.ZERO : referralCommissionPendingAmount;
    }

    public BigDecimal getReferralCommissionPaidAmount() {
        return referralCommissionPaidAmount;
    }

    public void setReferralCommissionPaidAmount(BigDecimal referralCommissionPaidAmount) {
        this.referralCommissionPaidAmount = referralCommissionPaidAmount == null ? BigDecimal.ZERO : referralCommissionPaidAmount;
    }

    public long getChatConversationsTotalCount() {
        return chatConversationsTotalCount;
    }

    public void setChatConversationsTotalCount(long chatConversationsTotalCount) {
        this.chatConversationsTotalCount = chatConversationsTotalCount;
    }

    public long getChatMessagesLast24HoursCount() {
        return chatMessagesLast24HoursCount;
    }

    public void setChatMessagesLast24HoursCount(long chatMessagesLast24HoursCount) {
        this.chatMessagesLast24HoursCount = chatMessagesLast24HoursCount;
    }

    public long getChatMessagesLast7DaysCount() {
        return chatMessagesLast7DaysCount;
    }

    public void setChatMessagesLast7DaysCount(long chatMessagesLast7DaysCount) {
        this.chatMessagesLast7DaysCount = chatMessagesLast7DaysCount;
    }

    public long getActiveChatUsers7DaysCount() {
        return activeChatUsers7DaysCount;
    }

    public void setActiveChatUsers7DaysCount(long activeChatUsers7DaysCount) {
        this.activeChatUsers7DaysCount = activeChatUsers7DaysCount;
    }

}



