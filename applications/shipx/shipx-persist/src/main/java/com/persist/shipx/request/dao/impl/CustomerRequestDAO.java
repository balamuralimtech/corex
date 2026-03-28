package com.persist.shipx.request.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.request.CustomerRequest;
import com.persist.shipx.request.dao.ICustomerRequestDAO;
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
public class CustomerRequestDAO implements ICustomerRequestDAO {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRequestDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addCustomerRequest(CustomerRequest customerRequest) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            if (requestReferenceExists(session, customerRequest.getRequestReference(), null)) {
                transaction.rollback();
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(customerRequest);
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Failed to add customer request", exception);
            rollback(transaction);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    public GeneralConstants updateCustomerRequest(CustomerRequest customerRequest) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            CustomerRequest persistentRequest = session.get(CustomerRequest.class, customerRequest.getId());
            if (persistentRequest == null || !persistentRequest.isFlag()) {
                transaction.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            if (requestReferenceExists(session, customerRequest.getRequestReference(), customerRequest.getId())) {
                transaction.rollback();
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.merge(customerRequest);
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Failed to update customer request {}", customerRequest.getId(), exception);
            rollback(transaction);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    public GeneralConstants deleteCustomerRequest(CustomerRequest customerRequest) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            CustomerRequest persistentRequest = session.get(CustomerRequest.class, customerRequest.getId());
            if (persistentRequest == null || !persistentRequest.isFlag()) {
                transaction.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            persistentRequest.setFlag(false);
            session.update(persistentRequest);
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Failed to delete customer request {}", customerRequest.getId(), exception);
            rollback(transaction);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CustomerRequest> getCustomerRequestList() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            List<CustomerRequest> requests = session.createQuery(
                            "from CustomerRequest where flag = true order by createdAt desc, id desc")
                    .list();
            transaction.commit();
            return requests;
        } catch (Exception exception) {
            logger.error("Failed to fetch customer request list", exception);
            rollback(transaction);
            return Collections.emptyList();
        } finally {
            close(session);
        }
    }

    @Override
    public CustomerRequest getCustomerRequestById(Integer id) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            CustomerRequest customerRequest = session.get(CustomerRequest.class, id);
            transaction.commit();
            return customerRequest != null && customerRequest.isFlag() ? customerRequest : null;
        } catch (Exception exception) {
            logger.error("Failed to fetch customer request {}", id, exception);
            rollback(transaction);
            return null;
        } finally {
            close(session);
        }
    }

    private boolean requestReferenceExists(Session session, String requestReference, Integer currentId) {
        String hql = "select count(cr.id) from CustomerRequest cr where cr.requestReference = :requestReference";
        if (currentId != null) {
            hql += " and cr.id <> :currentId";
        }

        org.hibernate.query.Query<Long> query = session.createQuery(hql, Long.class)
                .setParameter("requestReference", requestReference);
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
