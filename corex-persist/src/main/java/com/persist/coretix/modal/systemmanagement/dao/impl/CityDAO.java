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
package com.persist.coretix.modal.systemmanagement.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Cities;
import com.persist.coretix.modal.systemmanagement.dao.ICityDAO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author balamurali
 */
@Named
public class CityDAO implements ICityDAO {

    private static final Logger logger = LoggerFactory.getLogger(CityDAO.class);
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addCity(Cities city) {
        logger.debug("inside CityDAO addCity");

        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Cities o where o.name = :name")
                    .setParameter("name", city.getName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(city);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }


    }

    public GeneralConstants updateCity(Cities city) {
        logger.debug("inside dao updateCity !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from Cities o where o.id = :id")
                    .setParameter("id", city.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from Cities o where o.name = :name and o.id != :id")
                    .setParameter("name", city.getName())
                    .setParameter("id", city.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(city);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    public GeneralConstants deleteCity(Cities city) {
        logger.debug("inside dao deleteCity !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Cities o where o.id = :id")
                    .setParameter("id", city.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(city);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            logger.debug("Exception : " + e);
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Cities getCity(int id) {
        Session session = getSessionFactory().getCurrentSession();
        List<?> list = session
                .createQuery("from Cities where id=?1").setParameter(1, id)
                .list();

        return list.isEmpty() ? null : (Cities) list.get(0);
    }

    public Cities getCityEntityByCityName(String cityName) {
        Session session = getSessionFactory().getCurrentSession();
        List<?> list = session
                .createQuery("from Cities where name=?1").setParameter(1, cityName)
                .list();

        return list.isEmpty() ? null : (Cities) list.get(0);
    }

    public List<Cities> getCitiesList() {
        Session session = getSessionFactory().getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Cities> list = (List<Cities>) session.createQuery("from Cities").list();

        return list;
    }
    
    public List<Cities> getCitiesListByCountryIdAndStateId(int countryId, int stateId) {
        Session session = getSessionFactory().getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Cities> list = (List<Cities>) session
                .createQuery("from Cities where country_id = ?1 and state_id = ?2")
                .setParameter(1, countryId)
                .setParameter(2, stateId)
                .list();

        return list;
    }

}






