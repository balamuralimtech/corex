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
package com.persist.coretix.modal.usermanagement.dao.impl;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.usermanagement.dao.IUserAdministrationDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


/**
 *
 * @author Pragadeesh
 */
@Named
public class UserAdministrationDAO  implements IUserAdministrationDAO{
    
private static final Logger logger = LoggerFactory.getLogger(UserAdministrationDAO.class);
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addUserDetail(UserDetails entity) {
        logger.debug("inside UserAdministrationDAO addUserDetail");
        executeWrite(session -> session.save(entity));
    }

    public void deleteUserDetail(UserDetails entity) {
        executeWrite(session -> session.delete(entity));
    }

    public void updateUserDetail(UserDetails entity) {
        logger.debug("inside CountryDAO updateCountry");
        executeWrite(session -> session.merge(entity));
    }

    public UserDetails getUserDetail(int id) {
        logger.debug("User Id inside getUserDetail(int id):" + id);
        return executeRead(session -> {
            List<?> list = session
                    .createQuery("SELECT DISTINCT u FROM UserDetails u " +
                                "LEFT JOIN FETCH u.role " +
                                "LEFT JOIN FETCH u.organization " +
                                "LEFT JOIN FETCH u.branch " +
                                "LEFT JOIN FETCH u.country " +
                                "LEFT JOIN FETCH u.state " +
                                "LEFT JOIN FETCH u.city " +
                                "WHERE u.userId = :userId")
                    .setParameter("userId", id)
                    .list();
            return list.isEmpty() ? null : (UserDetails) list.get(0);
        });
    }

    public UserDetails getUserDetailEntityByUserName(String userName) {
        return executeRead(session -> {
            List<?> list = session
                    .createQuery("SELECT DISTINCT u FROM UserDetails u " +
                                "LEFT JOIN FETCH u.role " +
                                "LEFT JOIN FETCH u.organization " +
                                "LEFT JOIN FETCH u.branch " +
                                "WHERE u.userName = :userName")
                    .setParameter("userName", userName)
                    .list();
            return list.isEmpty() ? null : (UserDetails) list.get(0);
        });
    }
    
    public List<UserDetails> getUserDetailsList() {
        return executeRead(session -> {
            @SuppressWarnings("unchecked")
            List<UserDetails> list = (List<UserDetails>) session
                    .createQuery("SELECT DISTINCT u FROM UserDetails u " +
                                "LEFT JOIN FETCH u.role " +
                                "LEFT JOIN FETCH u.organization " +
                                "LEFT JOIN FETCH u.branch " +
                                "LEFT JOIN FETCH u.country " +
                                "LEFT JOIN FETCH u.state " +
                                "LEFT JOIN FETCH u.city")
                    .list();
            return list;
        });
    }

    public long getUserCount() {
        return executeRead(this::countUsers);
    }

    private long countUsers(Session session) {
        Long count = (Long) session.createQuery("select count(*) from UserDetails").uniqueResult();
        return count == null ? 0L : count;
    }


    public void updateUserPassword(int userId, String newPassword) {
        executeWrite(session -> {
            String hql = "update UserDetails set password = :newPassword, "
                    + "lastPasswordChange = current_timestamp(), updatedAt = current_timestamp() "
                    + "where userId = :userId";
            session.createQuery(hql)
                    .setParameter("newPassword", newPassword)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    public void updateUserAccountControls(int userId, boolean accountDisabled, boolean accountLocked) {
        executeWrite(session -> {
            String hql = "update UserDetails set accountDisabled = :accountDisabled, "
                    + "accountLocked = :accountLocked, updatedAt = current_timestamp() "
                    + "where userId = :userId";
            session.createQuery(hql)
                    .setParameter("accountDisabled", accountDisabled)
                    .setParameter("accountLocked", accountLocked)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    public boolean isUserValid(String username, String password) {
        return executeRead(session -> {
            List<?> list = session
                    .createQuery("SELECT DISTINCT u FROM UserDetails u " +
                                "LEFT JOIN FETCH u.role " +
                                "LEFT JOIN FETCH u.organization " +
                                "WHERE u.userName = :username AND u.password = :password")
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .list();
            return !list.isEmpty();
        });
    }

    /**
     * Optimized method to get user counts by status in a single query
     * @return Map with status as key and count as value
     */
    public Map<Integer, Long> getUserCountsByStatus() {
        return executeRead(session -> {
            @SuppressWarnings("unchecked")
            List<Object[]> results = session.createQuery(
                "SELECT u.status, COUNT(u) FROM UserDetails u " +
                "WHERE u.status IN (1, 3, 6) " +
                "GROUP BY u.status"
            ).list();

            Map<Integer, Long> countMap = new HashMap<>();
            for (Object[] row : results) {
                countMap.put((Integer) row[0], (Long) row[1]);
            }

            return countMap;
        });
    }

    private <T> T executeRead(Function<Session, T> operation) {
        try {
            return operation.apply(getSessionFactory().getCurrentSession());
        } catch (HibernateException exception) {
            logger.warn("Falling back to a temporary session for read operation", exception);
            try (Session session = getSessionFactory().openSession()) {
                return operation.apply(session);
            }
        }
    }

    public int getCountOfUsersLoggedOut() {
        Map<Integer, Long> counts = getUserCountsByStatus();
        return counts.getOrDefault(6, 0L).intValue();
    }

    public int getCountOfUsersLoggedIn() {
        Map<Integer, Long> counts = getUserCountsByStatus();
        return counts.getOrDefault(1, 0L).intValue();
    }

    public int getCountOfUsersNeverLoggedIn() {
        Map<Integer, Long> counts = getUserCountsByStatus();
        return counts.getOrDefault(3, 0L).intValue();
    }

    public void updateUserStatus(int userId, int newStatus) {
        executeWrite(session -> {
            String hql = "update UserDetails set status = :newStatus, updatedAt = current_timestamp() where userId = :userId";
            session.createQuery(hql)
                    .setParameter("newStatus", newStatus)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    public void markLoginSuccess(int userId, String sessionId) {
        executeWrite(session -> {
            String hql = "update UserDetails set status = :newStatus, lastSuccessfulLogin = current_timestamp(), "
                    + "lastSeenAt = current_timestamp(), lastLogoutAt = null, lastSessionId = :sessionId, "
                    + "updatedAt = current_timestamp() where userId = :userId";
            session.createQuery(hql)
                    .setParameter("newStatus", 1)
                    .setParameter("sessionId", sessionId)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    public void markLogout(int userId, int newStatus, String sessionId) {
        executeWrite(session -> {
            String hql = "update UserDetails set status = :newStatus, lastSeenAt = current_timestamp(), "
                    + "lastLogoutAt = current_timestamp(), lastSessionId = :sessionId, updatedAt = current_timestamp() "
                    + "where userId = :userId";
            session.createQuery(hql)
                    .setParameter("newStatus", newStatus)
                    .setParameter("sessionId", sessionId)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    public void touchUserSession(int userId, String sessionId) {
        executeWrite(session -> {
            String hql = "update UserDetails set lastSeenAt = current_timestamp(), lastSessionId = :sessionId, "
                    + "updatedAt = current_timestamp() where userId = :userId";
            session.createQuery(hql)
                    .setParameter("sessionId", sessionId)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    private void executeWrite(SessionOperation operation) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction transaction = session.getTransaction();
        boolean startedTransaction = transaction == null || !transaction.isActive();

        try {
            if (startedTransaction) {
                transaction = session.beginTransaction();
            }
            operation.accept(session);
            if (startedTransaction && transaction != null && transaction.isActive()) {
                transaction.commit();
            }
        } catch (RuntimeException exception) {
            if (startedTransaction && transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }

    @FunctionalInterface
    private interface SessionOperation {
        void accept(Session session);
    }

}


