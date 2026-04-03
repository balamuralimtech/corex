package com.persist.carex.settings.dao.impl;

import com.persist.carex.settings.PrescriptionSettings;
import com.persist.carex.settings.dao.IPrescriptionSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
public class PrescriptionSettingsDAO implements IPrescriptionSettingsDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants savePrescriptionSettings(PrescriptionSettings prescriptionSettings) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            PrescriptionSettings existing = findByOrganizationId(session, prescriptionSettings.getOrganization().getId());
            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (existing != null) {
                prescriptionSettings.setId(existing.getId());
                prescriptionSettings.setCreatedAt(existing.getCreatedAt());
                prescriptionSettings.setUpdatedAt(now);
                session.merge(prescriptionSettings);
            } else {
                prescriptionSettings.setCreatedAt(now);
                prescriptionSettings.setUpdatedAt(now);
                session.save(prescriptionSettings);
            }

            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            if (transaction != null) {
                transaction.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public PrescriptionSettings getPrescriptionSettingsByOrganizationId(Integer organizationId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            PrescriptionSettings settings = findByOrganizationId(session, organizationId);
            transaction.commit();
            return settings;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private PrescriptionSettings findByOrganizationId(Session session, Integer organizationId) {
        if (organizationId == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<PrescriptionSettings> list = session.createQuery(
                        "from PrescriptionSettings ps where ps.organization.id = :organizationId")
                .setParameter("organizationId", organizationId)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }
}
