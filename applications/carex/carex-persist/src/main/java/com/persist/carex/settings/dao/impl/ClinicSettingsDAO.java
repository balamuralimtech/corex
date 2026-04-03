package com.persist.carex.settings.dao.impl;

import com.persist.carex.settings.ClinicSettings;
import com.persist.carex.settings.dao.IClinicSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
public class ClinicSettingsDAO implements IClinicSettingsDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants saveClinicSettings(ClinicSettings clinicSettings) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            ClinicSettings existing = findByOrganizationId(session, clinicSettings.getOrganization().getId());
            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (existing != null) {
                clinicSettings.setId(existing.getId());
                clinicSettings.setCreatedAt(existing.getCreatedAt());
                clinicSettings.setUpdatedAt(now);
                session.merge(clinicSettings);
            } else {
                clinicSettings.setCreatedAt(now);
                clinicSettings.setUpdatedAt(now);
                session.save(clinicSettings);
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
    public ClinicSettings getClinicSettingsByOrganizationId(Integer organizationId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            ClinicSettings clinicSettings = findByOrganizationId(session, organizationId);
            transaction.commit();
            return clinicSettings;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private ClinicSettings findByOrganizationId(Session session, Integer organizationId) {
        if (organizationId == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<ClinicSettings> list = session.createQuery(
                        "from ClinicSettings cs where cs.organization.id = :organizationId")
                .setParameter("organizationId", organizationId)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }
}
