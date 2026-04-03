package com.persist.carex.settings.dao.impl;

import com.persist.carex.settings.MedicalCertificateSettings;
import com.persist.carex.settings.dao.IMedicalCertificateSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class MedicalCertificateSettingsDAO implements IMedicalCertificateSettingsDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants saveMedicalCertificateSettings(MedicalCertificateSettings medicalCertificateSettings) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            MedicalCertificateSettings existing = findByOrganizationId(session,
                    medicalCertificateSettings.getOrganization().getId());
            if (existing == null) {
                medicalCertificateSettings.setCreatedAt(now);
                medicalCertificateSettings.setUpdatedAt(now);
                session.save(medicalCertificateSettings);
            } else {
                medicalCertificateSettings.setId(existing.getId());
                medicalCertificateSettings.setCreatedAt(existing.getCreatedAt());
                medicalCertificateSettings.setUpdatedAt(now);
                session.merge(medicalCertificateSettings);
            }
            transaction.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            if (transaction != null) {
                transaction.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            session.close();
        }
    }

    @Override
    public MedicalCertificateSettings getMedicalCertificateSettingsByOrganizationId(Integer organizationId) {
        Session session = sessionFactory.openSession();
        try {
            return findByOrganizationId(session, organizationId);
        } finally {
            session.close();
        }
    }

    private MedicalCertificateSettings findByOrganizationId(Session session, Integer organizationId) {
        return session.createQuery("from MedicalCertificateSettings where organization.id = :organizationId",
                        MedicalCertificateSettings.class)
                .setParameter("organizationId", organizationId)
                .uniqueResult();
    }
}
