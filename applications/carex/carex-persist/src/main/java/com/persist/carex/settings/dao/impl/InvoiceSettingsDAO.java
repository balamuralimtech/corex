package com.persist.carex.settings.dao.impl;

import com.persist.carex.settings.InvoiceSettings;
import com.persist.carex.settings.dao.IInvoiceSettingsDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
public class InvoiceSettingsDAO implements IInvoiceSettingsDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants saveInvoiceSettings(InvoiceSettings invoiceSettings) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            InvoiceSettings existing = findByOrganizationId(session, invoiceSettings.getOrganization().getId());
            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (existing != null) {
                invoiceSettings.setId(existing.getId());
                invoiceSettings.setCreatedAt(existing.getCreatedAt());
                invoiceSettings.setUpdatedAt(now);
                session.merge(invoiceSettings);
            } else {
                invoiceSettings.setCreatedAt(now);
                invoiceSettings.setUpdatedAt(now);
                session.save(invoiceSettings);
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
    public InvoiceSettings getInvoiceSettingsByOrganizationId(Integer organizationId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            InvoiceSettings settings = findByOrganizationId(session, organizationId);
            transaction.commit();
            return settings;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private InvoiceSettings findByOrganizationId(Session session, Integer organizationId) {
        if (organizationId == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<InvoiceSettings> list = session.createQuery(
                        "from InvoiceSettings iset where iset.organization.id = :organizationId")
                .setParameter("organizationId", organizationId)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }
}
