/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao.impl;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Designations;
import com.persist.coretix.modal.systemmanagement.dao.IDesignationDAO;
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
 * @author Pragadeesh
 */
@Named
public class DesignationDAO implements IDesignationDAO {

    private static final Logger logger = LoggerFactory.getLogger(DesignationDAO.class);
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

    public GeneralConstants addDesignation(Designations designation) {
        logger.debug("inside DesignationDAO addDesignation");

        logger.debug("DesignationDAO: addDesignation");
        Session session = null;
        Transaction trans = null;
        try {
            logger.debug("DesignationDAO: inside try method");
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Designations o where o.designationName = :name")
                    .setParameter("name", designation.getDesignationName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(designation);
            trans.commit();
            logger.debug("DesignationDAO: inside success option");
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            logger.debug("DesignationDAO: inside catch exception"+e);
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

    public GeneralConstants deleteDesignation(Designations designation) {

        logger.debug("inside dao deleteDesignation !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Designations o where o.id = :id")
                    .setParameter("id", designation.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(designation);
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

    public GeneralConstants updateDesignation(Designations designation) {

        logger.debug("inside dao updateDesignation !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from Designations o where o.id = :id")
                    .setParameter("id", designation.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from Designations o where o.designationName = :name and o.id != :id")
                    .setParameter("name", designation.getDesignationName())
                    .setParameter("id", designation.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(designation);
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

    public Designations getDesignation(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from Designations where id=?1").setParameter(1, id)
                .list();

        trans.commit();
        return (Designations) list.get(0);
    }

    public List<Designations> getDesignationsList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<Designations> list = (List<Designations>) session.createQuery("from Designations").list();

        trans.commit();
        return list;
    }

}


