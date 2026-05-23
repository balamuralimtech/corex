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
package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.coretix.dao.ICoreDashboardDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class CoreDashboardDAO implements ICoreDashboardDAO {

    private static final Logger logger = LoggerFactory.getLogger(CoreDashboardDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Fetches all base dashboard entity counts in one database roundtrip.
     *
     * @return count map keyed by dashboard metric name.
     */
    public Map<String, Long> fetchDashboardEntityCounts() {
        Map<String, Long> counts = new HashMap<>();
        try {
            Session session = getSessionFactory().getCurrentSession();
            @SuppressWarnings("unchecked")
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT 'organizations' AS metric, COUNT(*) AS total FROM Organizations " +
                            "UNION ALL SELECT 'branches', COUNT(*) FROM Branches " +
                            "UNION ALL SELECT 'departments', COUNT(*) FROM Departments " +
                            "UNION ALL SELECT 'designations', COUNT(*) FROM Designations " +
                            "UNION ALL SELECT 'countries', COUNT(*) FROM Countries " +
                            "UNION ALL SELECT 'states', COUNT(*) FROM States " +
                            "UNION ALL SELECT 'cities', COUNT(*) FROM Cities " +
                            "UNION ALL SELECT 'currencies', COUNT(*) FROM CurrencyDetails " +
                            "UNION ALL SELECT 'roles', COUNT(*) FROM Roles " +
                            "UNION ALL SELECT 'users', COUNT(*) FROM UserDetails " +
                            "UNION ALL SELECT 'userActivities', COUNT(*) FROM UserActivities")
                    .list();

            for (Object[] row : rows) {
                if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                    continue;
                }
                counts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
            }
        } catch (Exception e) {
            logger.error("Error fetching dashboard entity counts", e);
        }
        return counts;
    }

    public Map<String, BigDecimal> fetchDashboardOperationalMetrics() {
        Map<String, BigDecimal> metrics = new HashMap<>();
        Session session = getSessionFactory().getCurrentSession();
        putOperationalMetrics(metrics, session,
                "SELECT 'disabledUsers' AS metric, COUNT(*) AS total FROM UserDetails WHERE account_disabled = true " +
                        "UNION ALL SELECT 'lockedUsers', COUNT(*) FROM UserDetails WHERE account_locked = true " +
                        "UNION ALL SELECT 'stalePasswordUsers', COUNT(*) FROM UserDetails WHERE last_password_change IS NULL OR last_password_change < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY) " +
                        "UNION ALL SELECT 'inactiveUsers30Days', COUNT(*) FROM UserDetails WHERE last_seen_at IS NULL OR last_seen_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) " +
                        "UNION ALL SELECT 'activityLast7Days', COUNT(*) FROM UserActivities WHERE created_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) " +
                        "UNION ALL SELECT 'activityLast30Days', COUNT(*) FROM UserActivities WHERE created_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) " +
                        "UNION ALL SELECT 'uniqueActiveUsers30Days', COUNT(DISTINCT user_id) FROM UserActivities WHERE created_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) " +
                        "UNION ALL SELECT 'licensesExpiring7Days', COUNT(*) FROM licenses WHERE end_date >= CURRENT_DATE AND end_date <= DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) " +
                        "UNION ALL SELECT 'licensesExpiring15Days', COUNT(*) FROM licenses WHERE end_date >= CURRENT_DATE AND end_date <= DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY) " +
                        "UNION ALL SELECT 'licensesExpiring30Days', COUNT(*) FROM licenses WHERE end_date >= CURRENT_DATE AND end_date <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) " +
                        "UNION ALL SELECT 'expiredLicenses', COUNT(*) FROM licenses WHERE end_date < CURRENT_DATE " +
                        "UNION ALL SELECT 'avgLicenseDaysRemaining', COALESCE(AVG(DATEDIFF(end_date, CURRENT_DATE)), 0) FROM licenses WHERE end_date >= CURRENT_DATE",
                "security/activity/license metrics");
        putOperationalMetrics(metrics, session,
                "SELECT 'demoRequestsTotal' AS metric, COUNT(*) AS total FROM demo_request " +
                        "UNION ALL SELECT 'demoRequestsPending', COUNT(*) FROM demo_request WHERE demo_done = false " +
                        "UNION ALL SELECT 'demoRequestsCompleted', COUNT(*) FROM demo_request WHERE demo_done = true " +
                        "UNION ALL SELECT 'avgDemoCompletionHours', COALESCE(AVG(TIMESTAMPDIFF(HOUR, created_at, demo_done_at)), 0) FROM demo_request WHERE demo_done = true AND created_at IS NOT NULL AND demo_done_at IS NOT NULL " +
                        "UNION ALL SELECT 'notificationsTotal', COUNT(*) FROM application_notification " +
                        "UNION ALL SELECT 'notificationReceiptsTotal', COUNT(*) FROM user_notification_receipt " +
                        "UNION ALL SELECT 'notificationUnseenTotal', (SELECT COUNT(*) FROM application_notification) * (SELECT COUNT(*) FROM UserDetails) - (SELECT COUNT(*) FROM user_notification_receipt) " +
                        "UNION ALL SELECT 'latestNotificationAgeHours', COALESCE(TIMESTAMPDIFF(HOUR, MAX(created_at), CURRENT_TIMESTAMP), 0) FROM application_notification",
                "demo/notification metrics");
        putOperationalMetrics(metrics, session,
                "SELECT 'activeReferrers' AS metric, COUNT(*) AS total FROM referrer_profile WHERE is_active = true " +
                        "UNION ALL SELECT 'referralAttributionsTotal', COUNT(*) FROM referral_attribution " +
                        "UNION ALL SELECT 'referralSubscriptionAmount', COALESCE(SUM(subscription_amount), 0) FROM referral_attribution " +
                        "UNION ALL SELECT 'referralCommissionPendingAmount', COALESCE(SUM(commission_amount), 0) FROM referral_commission WHERE lower(commission_status) <> 'paid' " +
                        "UNION ALL SELECT 'referralCommissionPaidAmount', COALESCE(SUM(commission_amount), 0) FROM referral_commission WHERE lower(commission_status) = 'paid'",
                "referral metrics");
        putOperationalMetrics(metrics, session,
                "SELECT 'chatConversationsTotal' AS metric, COUNT(*) AS total FROM chat_conversation " +
                        "UNION ALL SELECT 'chatMessagesLast24Hours', COUNT(*) FROM chat_message WHERE created_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) " +
                        "UNION ALL SELECT 'chatMessagesLast7Days', COUNT(*) FROM chat_message WHERE created_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) " +
                        "UNION ALL SELECT 'activeChatUsers7Days', COUNT(DISTINCT sender_user_id) FROM chat_message WHERE created_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)",
                "chat metrics");
        return metrics;
    }

    private void putOperationalMetrics(Map<String, BigDecimal> metrics, Session session, String sql, String metricGroup) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = session.createNativeQuery(sql).list();

            for (Object[] row : rows) {
                if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                    continue;
                }
                metrics.put(String.valueOf(row[0]), toBigDecimal(row[1]));
            }
        } catch (Exception e) {
            logger.warn("Unable to fetch dashboard {}", metricGroup, e);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }


    /**
     * Fetches the count of organizations from the database.
     *
     * @return the count of organizations.
     */
    public long fetchOrganizationCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Organizations").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching organization count", e);
        }
        return count;
    }

    /**
     * Fetches the count of branches from the database.
     *
     * @return the count of branches.
     */
    public long fetchBranchCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Branches").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching branch count", e);
        }
        return count;
    }

    /**
     * Fetches the count of departments from the database.
     *
     * @return the count of departments.
     */
    public long fetchDepartmentCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Departments").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching department count", e);
        }
        return count;
    }

    /**
     * Fetches the count of designations from the database.
     *
     * @return the count of designations.
     */
    public long fetchDesignationCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Designations").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching designation count", e);
        }
        return count;
    }

    /**
     * Fetches the count of countries from the database.
     *
     * @return the count of countries.
     */
    public long fetchCountryCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Countries").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching country count", e);
        }
        return count;
    }

    /**
     * Fetches the count of states from the database.
     *
     * @return the count of states.
     */
    public long fetchStateCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM States").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching state count", e);
        }
        return count;
    }

    /**
     * Fetches the count of cities from the database.
     *
     * @return the count of cities.
     */
    public long fetchCityCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Cities").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching city count", e);
        }
        return count;
    }

    /**
     * Fetches the count of currencies from the database.
     *
     * @return the count of currencies.
     */
    public long fetchCurrencyCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM CurrencyDetails").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching currency count", e);
        }
        return count;
    }

    /**
     * Fetches the count of roles from the database.
     *
     * @return the count of roles.
     */
    public long fetchRoleCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Roles").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching role count", e);
        }
        return count;
    }

    /**
     * Fetches the count of users from the database.
     *
     * @return the count of users.
     */
    public long fetchUserCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM UserDetails").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching user count", e);
        }
        return count;
    }

    /**
     * Fetches the count of users from the database.
     *
     * @return the count of users.
     */
    public long fetchUserActivityCount() {
        long count = 0;
        try {
            Session session = getSessionFactory().getCurrentSession();
            count = (long) session.createQuery("SELECT COUNT(*) FROM UserActivities").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching user count", e);
        }
        return count;
    }
}



