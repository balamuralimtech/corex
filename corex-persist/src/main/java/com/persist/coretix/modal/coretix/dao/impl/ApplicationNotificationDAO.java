package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.persist.coretix.modal.coretix.UserNotificationReceipt;
import com.persist.coretix.modal.coretix.dao.IApplicationNotificationDAO;
import com.persist.coretix.modal.usermanagement.UserDetails;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named
public class ApplicationNotificationDAO implements IApplicationNotificationDAO {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNotificationDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addApplicationNotification(ApplicationNotification applicationNotification) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            session.save(applicationNotification);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to save application notification", e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public GeneralConstants updateApplicationNotification(ApplicationNotification applicationNotification) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(n.id) from ApplicationNotification n where n.id = :id")
                    .setParameter("id", applicationNotification.getId())
                    .uniqueResult();
            if (count == null || count == 0) {
                trans.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.update(applicationNotification);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to update application notification {}", applicationNotification.getId(), e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public GeneralConstants deleteApplicationNotification(ApplicationNotification applicationNotification) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            ApplicationNotification persistentNotification = session.get(ApplicationNotification.class, applicationNotification.getId());
            if (persistentNotification == null) {
                trans.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.createQuery("delete from UserNotificationReceipt r where r.notification.id = :notificationId")
                    .setParameter("notificationId", applicationNotification.getId())
                    .executeUpdate();
            session.delete(persistentNotification);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to delete application notification {}", applicationNotification.getId(), e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ApplicationNotification> getRecentNotifications(int maxResults) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            List<ApplicationNotification> notifications = session.createQuery(
                            "from ApplicationNotification order by createdAt desc, id desc")
                    .setMaxResults(maxResults)
                    .list();
            trans.commit();
            return notifications;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to fetch recent application notifications", e);
            return Collections.emptyList();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public ApplicationNotification getApplicationNotificationById(int id) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            ApplicationNotification notification = session.get(ApplicationNotification.class, id);
            trans.commit();
            return notification;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to fetch application notification {}", id, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public int getUnreadNotificationCountForUser(int userId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            Long unreadCount = (Long) session.createQuery(
                            "select count(n.id) from ApplicationNotification n " +
                                    "where not exists (" +
                                    "select r.id from UserNotificationReceipt r " +
                                    "where r.notification.id = n.id and r.user.userId = :userId)")
                    .setParameter("userId", userId)
                    .uniqueResult();
            trans.commit();
            return unreadCount == null ? 0 : unreadCount.intValue();
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to count unread notifications for user {}", userId, e);
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public GeneralConstants markAllNotificationsAsSeen(int userId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            UserDetails user = session.get(UserDetails.class, userId);
            if (user == null) {
                trans.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            List<ApplicationNotification> unreadNotifications = session.createQuery(
                            "from ApplicationNotification n where not exists (" +
                                    "select r.id from UserNotificationReceipt r " +
                                    "where r.notification.id = n.id and r.user.userId = :userId)")
                    .setParameter("userId", userId)
                    .list();

            for (ApplicationNotification notification : unreadNotifications) {
                UserNotificationReceipt receipt = new UserNotificationReceipt();
                receipt.setNotification(notification);
                receipt.setUser(user);
                session.save(receipt);
            }

            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to mark notifications as seen for user {}", userId, e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
