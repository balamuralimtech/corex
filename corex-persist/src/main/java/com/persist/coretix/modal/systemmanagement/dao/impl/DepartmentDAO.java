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
import com.persist.coretix.modal.systemmanagement.Departments;
import com.persist.coretix.modal.systemmanagement.dao.IDepartmentDAO;
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
 * @author Pragadeesh
 */
@Named
public class DepartmentDAO implements IDepartmentDAO {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDAO.class);
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addDepartment(Departments department) {
        logger.debug("inside DepartmentDAO addDepartment");

        Session session = null;
        Transaction trans = null;
        try {
            logger.debug("DepartmentDAO: inside try method");
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Departments o where o.departmentName = :name")
                    .setParameter("name", department.getDepartmentName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(department);
            trans.commit();
            logger.debug("DepartmentDAO: inside success option");
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            logger.debug("DepartmentDAO: inside catch exception"+e);
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

    public GeneralConstants deleteDepartment(Departments department) {

        logger.debug("inside dao deleteDepartment !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Departments o where o.id = :id")
                    .setParameter("id", department.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(department);
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


    public GeneralConstants updateDepartment(Departments department) {
        logger.debug("inside DepartmentDAO updateDepartment");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from Departments o where o.id = :id")
                    .setParameter("id", department.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from Departments o where o.departmentName = :name and o.id != :id")
                    .setParameter("name", department.getDepartmentName())
                    .setParameter("id", department.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(department);
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
    public Departments getDepartment(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.getTransaction();
        boolean startedTransaction = trans == null || !trans.isActive();
        if (startedTransaction) {
            trans = session.beginTransaction();
        }

        List<?> list = session
                .createQuery("SELECT DISTINCT d FROM Departments d " +
                            "LEFT JOIN FETCH d.organization " +
                            "WHERE d.id = :id")
                .setParameter("id", id)
                .list();

        if (startedTransaction && trans != null && trans.isActive()) {
            trans.commit();
        }
        return list.isEmpty() ? null : (Departments) list.get(0);
    }

    public List<Departments> getDepartmentsList() {
        logger.debug("inside DAO getDepartmentsList");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.getTransaction();
        boolean startedTransaction = trans == null || !trans.isActive();
        if (startedTransaction) {
            trans = session.beginTransaction();
        }

        @SuppressWarnings("unchecked")
        List<Departments> list = (List<Departments>) session.createQuery(
                "SELECT DISTINCT d FROM Departments d " +
                "LEFT JOIN FETCH d.organization").list();

        for (Departments departments : list) {
            logger.debug("departments getDepartmentName  : "+departments.getDepartmentName());
        }
        if (startedTransaction && trans != null && trans.isActive()) {
            trans.commit();
        }
        return list;
    }

}





