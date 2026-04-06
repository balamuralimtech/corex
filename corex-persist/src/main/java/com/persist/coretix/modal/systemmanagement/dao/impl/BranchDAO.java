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
import com.persist.coretix.modal.systemmanagement.Branches;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.systemmanagement.dao.IBranchDAO;
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
public class BranchDAO implements IBranchDAO {

    private static final Logger logger = LoggerFactory.getLogger(BranchDAO.class);
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

    public GeneralConstants addBranch(Branches branch) {

        logger.debug("BranchDAO: addBranch");
        Session session = null;
        Transaction trans = null;
        try {
            logger.debug("BranchDAO: inside try method");
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Branches o where o.branchName = :name")
                    .setParameter("name", branch.getBranchName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(branch);
            trans.commit();
            logger.debug("BranchDAO: inside success option");
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            logger.debug("BranchDAO: inside catch exception"+e);
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

    public GeneralConstants updateBranch(Branches branch) {
        logger.debug("inside dao updateBranch !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from Branches o where o.id = :id")
                    .setParameter("id", branch.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from Branches o where o.branchName = :name and o.id != :id")
                    .setParameter("name", branch.getBranchName())
                    .setParameter("id", branch.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(branch);
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

    public GeneralConstants deleteBranch(Branches branch) {
        logger.debug("inside dao deleteBranch !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Branches o where o.id = :id")
                    .setParameter("id", branch.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(branch);
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


    public Branches getBranch(int id) {
        Session session = getSessionFactory().getCurrentSession();
        List<?> list = session
                .createQuery("from Branches where id=?1").setParameter(1, id)
                .list();

        return list.isEmpty() ? null : (Branches) list.get(0);
    }

    public Branches getBranchEntityByBranchName(String branchName) {
        Session session = getSessionFactory().getCurrentSession();
        List<?> list = session
                .createQuery("from Branches where branch_name=?1").setParameter(1, branchName)
                .list();

        return list.isEmpty() ? null : (Branches) list.get(0);
    }

    public List<Branches> getBranchesList() {
        Session session = getSessionFactory().getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Branches> list = (List<Branches>) session.createQuery("from Branches").list();

        return list;
    }

    public List<Branches> getBranchesListByOrgId(int orgId) {
        logger.debug("inside getBranchesListByOrgId : "+orgId);
        Session session = getSessionFactory().getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Branches> list = (List<Branches>) session.createQuery("from Branches where organization_id=?1").setParameter(1, orgId).list();
        logger.debug("list size  : "+list.size());
        return list;
    }

}






