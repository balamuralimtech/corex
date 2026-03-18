package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.coretix.dao.ICoreDashboardDAO;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CoreDashboardDAO implements ICoreDashboardDAO {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    /**
     * Fetches the count of organizations from the database.
     *
     * @return the count of organizations.
     */
    public long fetchOrganizationCount() {
        long count = 0;
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Organizations").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching organization count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Branches").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching branch count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Departments").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching department count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Designations").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching designation count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Countries").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching country count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM States").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching state count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Cities").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching city count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM CurrencyDetails").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching currency count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM Roles").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching role count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM UserDetails").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching user count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            count = (long) session.createQuery("SELECT COUNT(*) FROM UserActivities").uniqueResult();
        } catch (Exception e) {
            logger.error("Error fetching user count", e);
        }
        finally {
            if (session != null) {
                session.close();
            }
        }
        return count;
    }
}
