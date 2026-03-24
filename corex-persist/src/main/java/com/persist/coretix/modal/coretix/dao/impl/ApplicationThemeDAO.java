/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
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
 * Project: `app.name`
 */
package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationTheme;
import com.persist.coretix.modal.coretix.dao.IApplicationThemeDAO;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class ApplicationThemeDAO implements IApplicationThemeDAO {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationThemeDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Inject
    private IUserActivityDAO userActivityDAO;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addApplicationTheme(ApplicationTheme applicationTheme) {
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();
            session.save(applicationTheme);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Error while saving application theme for user {}", applicationTheme.getUserId(), e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GeneralConstants updateApplicationTheme(ApplicationTheme applicationTheme) {
        logger.debug("inside dao updateApplicationTheme !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from ApplicationTheme o where o.id = :id")
                    .setParameter("id", applicationTheme.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(applicationTheme);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Constraint violation while updating application theme for user {}", applicationTheme.getUserId(), e);
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Error while updating application theme for user {}", applicationTheme.getUserId(), e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public ApplicationTheme getApplicationThemeByUserid(int userid)
    {
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            List<?> list = session
                    .createQuery("from ApplicationTheme where userId = ?1")
                    .setParameter(1, userid)
                    .list();

            trans.commit();
            return list.isEmpty() ? null : (ApplicationTheme) list.get(0);
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Error while fetching application theme for user {}", userid, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}







