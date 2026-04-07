package com.persist.shipx.quotation.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.quotation.Quotation;
import com.persist.shipx.quotation.dao.IQuotationDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named
public class QuotationDAO implements IQuotationDAO {

    private static final Logger logger = LoggerFactory.getLogger(QuotationDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addQuotation(Quotation quotation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            if (quotationReferenceExists(session, quotation.getQuotationReference(), null)) {
                transaction.rollback();
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(quotation);
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Failed to add quotation", exception);
            rollback(transaction);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    public GeneralConstants updateQuotation(Quotation quotation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Quotation persistentQuotation = session.get(Quotation.class, quotation.getId());
            if (persistentQuotation == null || !persistentQuotation.isFlag()) {
                transaction.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            if (quotationReferenceExists(session, quotation.getQuotationReference(), quotation.getId())) {
                transaction.rollback();
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.merge(quotation);
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Failed to update quotation {}", quotation.getId(), exception);
            rollback(transaction);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    public GeneralConstants deleteQuotation(Quotation quotation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Quotation persistentQuotation = session.get(Quotation.class, quotation.getId());
            if (persistentQuotation == null || !persistentQuotation.isFlag()) {
                transaction.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            persistentQuotation.setFlag(false);
            session.update(persistentQuotation);
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Failed to delete quotation {}", quotation.getId(), exception);
            rollback(transaction);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Quotation> getQuotationList() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            List<Quotation> quotations = session.createQuery(
                            "SELECT DISTINCT q FROM Quotation q " +
                            "LEFT JOIN FETCH q.customerRequest cr " +
                            "LEFT JOIN FETCH cr.originCountry " +
                            "LEFT JOIN FETCH cr.destinationCountry " +
                            "WHERE q.flag = true " +
                            "ORDER BY q.createdAt DESC, q.id DESC")
                    .list();
            transaction.commit();
            return quotations;
        } catch (Exception exception) {
            logger.error("Failed to fetch quotation list", exception);
            rollback(transaction);
            return Collections.emptyList();
        } finally {
            close(session);
        }
    }

    @Override
    public Quotation getQuotationById(Integer id) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            List<Quotation> results = session.createQuery(
                            "SELECT DISTINCT q FROM Quotation q " +
                            "LEFT JOIN FETCH q.customerRequest cr " +
                            "LEFT JOIN FETCH cr.originCountry " +
                            "LEFT JOIN FETCH cr.destinationCountry " +
                            "WHERE q.id = :id", Quotation.class)
                    .setParameter("id", id)
                    .list();
            transaction.commit();
            Quotation quotation = results.isEmpty() ? null : results.get(0);
            return quotation != null && quotation.isFlag() ? quotation : null;
        } catch (Exception exception) {
            logger.error("Failed to fetch quotation {}", id, exception);
            rollback(transaction);
            return null;
        } finally {
            close(session);
        }
    }

    private boolean quotationReferenceExists(Session session, String quotationReference, Integer currentId) {
        String hql = "select count(q.id) from Quotation q where q.quotationReference = :quotationReference";
        if (currentId != null) {
            hql += " and q.id <> :currentId";
        }

        org.hibernate.query.Query<Long> query = session.createQuery(hql, Long.class)
                .setParameter("quotationReference", quotationReference);
        if (currentId != null) {
            query.setParameter("currentId", currentId);
        }

        Long duplicateCount = query.uniqueResult();
        return duplicateCount != null && duplicateCount > 0;
    }

    private void rollback(Transaction transaction) {
        if (transaction != null) {
            transaction.rollback();
        }
    }

    private void close(Session session) {
        if (session != null) {
            session.close();
        }
    }
}
