/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import com.persist.coretix.modal.systemmanagement.dao.ICurrencyDetailsDAO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.usermanagement.UserDetails;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author balamurali
 */
@Named
public class CurrencyDetailsDAO implements ICurrencyDetailsDAO {

    private final Logger logger = Logger.getLogger(getClass());
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addCurrencyDetails(CurrencyDetails currencyDetail) {
        logger.debug("inside CurrencyDetails DAO addCurrency Details");

        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from CurrencyDetails o where o.currencyName = :name")
                    .setParameter("name", currencyDetail.getCurrencyName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(currencyDetail);
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



    public GeneralConstants deleteCurrencyDetails(CurrencyDetails currencyDetail) {
        logger.debug("inside dao deleteCurrencyDetails !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from CurrencyDetails o where o.id = :id")
                    .setParameter("id", currencyDetail.getCurrencyId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(currencyDetail);
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

    public GeneralConstants updateCurrencyDetails(CurrencyDetails currencyDetail) {
        logger.debug("inside dao updateCurrencyDetail !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from CurrencyDetails o where o.id = :id")
                    .setParameter("id", currencyDetail.getCurrencyId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from CurrencyDetails o where o.currencyName = :name and o.id != :id")
                    .setParameter("name", currencyDetail.getCurrencyName())
                    .setParameter("id", currencyDetail.getCurrencyId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(currencyDetail);
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

    public CurrencyDetails getCurrencyDetailsById(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from CurrencyDetails where id=?").setParameter(0, id)
                .list();

        trans.commit();
        return (CurrencyDetails) list.get(0);
    }

    public CurrencyDetails getCurrencyDetailsEntityByCurrencyName(String currencyName) {
        logger.debug("inside getCurrencyDetailsEntityByCurrencyName : "+currencyName);
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from CurrencyDetails where currencyCode=?").setParameter(0, currencyName)
                .list();

        trans.commit();
        return (CurrencyDetails) list.get(0);
    }

    public List<CurrencyDetails> getCurrencyDetailsList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<CurrencyDetails> list = (List<CurrencyDetails>) session.createQuery("from CurrencyDetails").list();

        trans.commit();
        return list;
    }

}
