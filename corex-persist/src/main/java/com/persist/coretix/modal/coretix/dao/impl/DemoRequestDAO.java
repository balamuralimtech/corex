package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.DemoRequest;
import com.persist.coretix.modal.coretix.dao.IDemoRequestDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Named
public class DemoRequestDAO implements IDemoRequestDAO {

    private static final Logger logger = LoggerFactory.getLogger(DemoRequestDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addDemoRequest(DemoRequest demoRequest) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            session.save(demoRequest);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to save demo request", e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DemoRequest> getRecentDemoRequests(int maxResults) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            List<DemoRequest> demoRequests = session.createQuery(
                            "from DemoRequest order by createdAt desc, id desc")
                    .setMaxResults(maxResults)
                    .list();
            trans.commit();
            return demoRequests;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to fetch demo requests", e);
            return Collections.emptyList();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public GeneralConstants updateDemoRequestStatus(int demoRequestId, boolean demoDone, String doneBy) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            DemoRequest demoRequest = session.get(DemoRequest.class, demoRequestId);
            if (demoRequest == null) {
                if (trans != null) {
                    trans.rollback();
                }
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            demoRequest.setDemoDone(demoDone);
            if (demoDone) {
                demoRequest.setDemoDoneAt(new Timestamp(System.currentTimeMillis()));
                demoRequest.setDemoDoneBy(doneBy);
            } else {
                demoRequest.setDemoDoneAt(null);
                demoRequest.setDemoDoneBy(null);
            }
            session.update(demoRequest);

            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to update demo request status for {}", demoRequestId, e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
