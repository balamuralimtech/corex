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
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.systemmanagement.BankDetails;
import com.persist.coretix.modal.systemmanagement.dao.IBankDetailsDAO;
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
public class BankDetailsDAO implements IBankDetailsDAO {

    private static final Logger logger = LoggerFactory.getLogger(BankDetailsDAO.class);
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addBankDetails(BankDetails bankDetails) {
        logger.debug("inside BankDetailsDAO addTermsAndCondtions");

        logger.debug("Bank Details OrgName:"+ bankDetails.getOrganization().getOrganizationName());
        logger.debug("Bank Details OrgId:"+ bankDetails.getOrganization().getId());
        logger.debug("Bank Account Details:"+ bankDetails.getBankAccountDetails());



        Session session = null;
        Transaction trans = null;
        try {
            logger.debug("BankDetailsDAO: inside try method");
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            logger.debug("Before Count assign");
            Long count = (Long) session.createQuery(
                            "select count(o) from BankDetails o where o.organization = :organization and o.bankAccountDetails= :bankAccountDetails ")

                    .setParameter("organization", bankDetails.getOrganization())
                    .setParameter("bankAccountDetails", bankDetails.getBankAccountDetails())
                    .uniqueResult();

            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(bankDetails);
            trans.commit();
            logger.debug("BankDetailsDAO: inside success option");
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {

            logger.debug("BankDetailsDAO: inside catch exception"+e);
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

    public GeneralConstants deleteBankDetails(BankDetails bankDetails) {

        logger.debug("inside dao deleteBankDetails !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from BankDetails o where o.id = :id")
                    .setParameter("id", bankDetails.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(bankDetails);
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


    public GeneralConstants updateBankDetails(BankDetails bankDetails) {
        logger.debug("inside BankDetailsDAO updateBankDetails");
        logger.debug("getBankAccountDetails :"+ bankDetails.getBankAccountDetails());
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from BankDetails o where o.id = :id")
                    .setParameter("id", bankDetails.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from BankDetails o where o.bankAccountDetails = :name and o.id != :id and o.organization = :organization")
                    .setParameter("name", bankDetails.getBankAccountDetails())
                    .setParameter("id", bankDetails.getId())
                    .setParameter("organization", bankDetails.getOrganization())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(bankDetails);
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
    public BankDetails getBankDetails(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from BankDetails where id=?1").setParameter(1, id)
                .list();

        trans.commit();
        return (BankDetails) list.get(0);
    }

    public BankDetails getBankDetailsByOrgId(int orgId) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from BankDetails where organization_id=?1").setParameter(1, orgId)
                .list();

        trans.commit();
        return (BankDetails) list.get(0);
    }

    public List<BankDetails> getBankDetailsList() {
        logger.debug("inside DAO getBankDetailsList");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<BankDetails> list = (List<BankDetails>) session.createQuery("from BankDetails").list();

        for (BankDetails bankDetails : list) {
            logger.debug("bankDetails getBankAccountDetails  : "+bankDetails.getBankAccountDetails());
        }
        trans.commit();
        return list;
    }

    public List<BankDetails> getBankDetailsListByOrgId(int orgId) {
        logger.debug("inside DAO getBankDetailsList");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<BankDetails> list = (List<BankDetails>) session.createQuery("from BankDetails where organization_id=?1").setParameter(1, orgId).list();

        for (BankDetails bankDetails : list) {
            logger.debug("bankDetails getBankAccountDetails  : "+bankDetails.getBankAccountDetails());
        }
        trans.commit();
        return list;
    }

}





