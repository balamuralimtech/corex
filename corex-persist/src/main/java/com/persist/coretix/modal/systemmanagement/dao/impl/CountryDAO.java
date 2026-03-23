/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.dao.ICountryDAO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
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
public class CountryDAO implements ICountryDAO {

    private static final Logger logger = LoggerFactory.getLogger(CountryDAO.class);
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

    public GeneralConstants addCountry(Countries country) {
        logger.debug("inside CountryDAO addCountry");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Countries o where o.name = :name")
                    .setParameter("name", country.getName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(country);
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

    public GeneralConstants deleteCountry(Countries country) {
        logger.debug("inside dao deleteCountry!!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Countries o where o.id = :id")
                    .setParameter("id", country.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(country);
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

    public GeneralConstants updateCountry(Countries country) {
        logger.debug("inside dao updateCountry !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from Countries o where o.id = :id")
                    .setParameter("id", country.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from Countries o where o.name = :name and o.id != :id")
                    .setParameter("name", country.getName())
                    .setParameter("id", country.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(country);
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

    public Countries getCountry(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from Countries where id=?1").setParameter(1, id)
                .list();

        trans.commit();
        return (Countries) list.get(0);
    }

    public Countries getCountryEntityByCountryName(String countryName) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from Countries where name=?1").setParameter(1, countryName)
                .list();

        trans.commit();
        return (Countries) list.get(0);
    }
    
    public List<Countries> getCountriesList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<Countries> list = (List<Countries>) session.createQuery("from Countries").list();

        trans.commit();
        return list;
    }
}



