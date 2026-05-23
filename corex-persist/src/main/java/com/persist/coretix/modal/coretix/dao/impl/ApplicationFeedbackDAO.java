package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationFeedback;
import com.persist.coretix.modal.coretix.dao.IApplicationFeedbackDAO;
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
public class ApplicationFeedbackDAO implements IApplicationFeedbackDAO {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationFeedbackDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addApplicationFeedback(ApplicationFeedback applicationFeedback) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            session.save(applicationFeedback);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to save application feedback", e);
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ApplicationFeedback> getRecentFeedback(int maxResults) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            List<ApplicationFeedback> feedbackList = session.createQuery(
                            "from ApplicationFeedback order by createdAt desc, id desc")
                    .setMaxResults(maxResults)
                    .list();
            trans.commit();
            return feedbackList;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Unable to fetch application feedback", e);
            return Collections.emptyList();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
