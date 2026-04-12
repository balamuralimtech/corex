package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationPricing;
import com.persist.coretix.modal.coretix.dao.IApplicationPricingDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
public class ApplicationPricingDAO implements IApplicationPricingDAO {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationPricingDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addApplicationPricing(ApplicationPricing applicationPricing) {
        Session session = sessionFactory.getCurrentSession();
        try {
            if (getExistingCount(session, applicationPricing.getApplicationCode(), applicationPricing.getCountryCode(), null) > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            applicationPricing.setCreatedAt(now);
            applicationPricing.setUpdatedAt(now);
            session.save(applicationPricing);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to add application pricing", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants updateApplicationPricing(ApplicationPricing applicationPricing) {
        Session session = sessionFactory.getCurrentSession();
        try {
            ApplicationPricing persistentPricing = session.get(ApplicationPricing.class, applicationPricing.getId());
            if (persistentPricing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            if (getExistingCount(session, applicationPricing.getApplicationCode(), applicationPricing.getCountryCode(),
                    applicationPricing.getId()) > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            persistentPricing.setApplicationCode(applicationPricing.getApplicationCode());
            persistentPricing.setApplicationName(applicationPricing.getApplicationName());
            persistentPricing.setCountryCode(applicationPricing.getCountryCode());
            persistentPricing.setCountryName(applicationPricing.getCountryName());
            persistentPricing.setCurrencyCode(applicationPricing.getCurrencyCode());
            persistentPricing.setCurrencySymbol(applicationPricing.getCurrencySymbol());
            persistentPricing.setOneMonthPrice(applicationPricing.getOneMonthPrice());
            persistentPricing.setSixMonthPrice(applicationPricing.getSixMonthPrice());
            persistentPricing.setOneYearPrice(applicationPricing.getOneYearPrice());
            persistentPricing.setDisplayOrder(applicationPricing.getDisplayOrder());
            persistentPricing.setActive(applicationPricing.isActive());
            persistentPricing.setNotes(applicationPricing.getNotes());
            persistentPricing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            session.merge(persistentPricing);
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException exception) {
            logger.warn("Application pricing update failed because entry is in use", exception);
            session.clear();
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception exception) {
            logger.error("Unable to update application pricing", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants deleteApplicationPricing(ApplicationPricing applicationPricing) {
        Session session = sessionFactory.getCurrentSession();
        try {
            ApplicationPricing persistentPricing = session.get(ApplicationPricing.class, applicationPricing.getId());
            if (persistentPricing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            session.delete(persistentPricing);
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException exception) {
            logger.warn("Application pricing delete failed because entry is in use", exception);
            session.clear();
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception exception) {
            logger.error("Unable to delete application pricing", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public ApplicationPricing getApplicationPricing(int id) {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.get(ApplicationPricing.class, id);
        } catch (Exception exception) {
            logger.warn("Unable to fetch application pricing by id. Pricing table may not be installed yet.", exception);
            session.clear();
            return null;
        }
    }

    @Override
    public ApplicationPricing getApplicationPricingByApplicationAndCountry(String applicationCode, String countryCode) {
        Session session = sessionFactory.getCurrentSession();
        try {
            List<ApplicationPricing> applicationPricingList = session.createQuery(
                            "from ApplicationPricing where lower(applicationCode) = :applicationCode and lower(countryCode) = :countryCode and active = true order by id desc",
                            ApplicationPricing.class)
                    .setParameter("applicationCode", applicationCode.toLowerCase())
                    .setParameter("countryCode", countryCode.toLowerCase())
                    .setMaxResults(1)
                    .list();
            return applicationPricingList.isEmpty() ? null : applicationPricingList.get(0);
        } catch (Exception exception) {
            logger.warn("Unable to fetch application pricing. Pricing table may not be installed yet.", exception);
            session.clear();
            return null;
        }
    }

    @Override
    public List<ApplicationPricing> getApplicationPricingList() {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "from ApplicationPricing order by coalesce(displayOrder, 9999), lower(applicationName), lower(countryName), id desc",
                            ApplicationPricing.class)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch application pricing list. Pricing table may not be installed yet.", exception);
            session.clear();
            return java.util.Collections.emptyList();
        }
    }

    private long getExistingCount(Session session, String applicationCode, String countryCode, Integer excludeId) {
        String hql = "select count(ap.id) from ApplicationPricing ap where lower(ap.applicationCode) = :applicationCode "
                + "and lower(ap.countryCode) = :countryCode";
        if (excludeId != null) {
            hql += " and ap.id != :excludeId";
        }
        org.hibernate.query.Query<Long> query = session.createQuery(hql, Long.class)
                .setParameter("applicationCode", applicationCode.toLowerCase())
                .setParameter("countryCode", countryCode.toLowerCase());
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        Long count = query.uniqueResult();
        return count == null ? 0L : count;
    }
}
