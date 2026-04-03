package com.persist.carex.clinicmanagement.dao.impl;

import com.persist.carex.clinicmanagement.Medicine;
import com.persist.carex.clinicmanagement.dao.IMedicineDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Named
public class MedicineDAO implements IMedicineDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addMedicine(Medicine medicine) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            if (findByCode(session, medicine.getMedicineCode()) != null) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            medicine.setCreatedAt(now);
            medicine.setUpdatedAt(now);
            session.save(medicine);
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
    public GeneralConstants updateMedicine(Medicine medicine) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Medicine existing = session.get(Medicine.class, medicine.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            Medicine duplicate = findByCode(session, medicine.getMedicineCode());
            if (duplicate != null && !duplicate.getId().equals(medicine.getId())) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            medicine.setCreatedAt(existing.getCreatedAt());
            medicine.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            session.merge(medicine);
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
    public GeneralConstants deleteMedicine(Medicine medicine) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Medicine existing = session.get(Medicine.class, medicine.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            session.delete(existing);
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
    public Medicine getMedicineById(Integer medicineId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return medicineId == null ? null : session.get(Medicine.class, medicineId);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Medicine> getMedicinesByOrganizationId(Integer organizationId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (organizationId == null) {
                return new ArrayList<>();
            }
            return session.createQuery("from Medicine m where m.organization.id = :organizationId order by m.medicineName", Medicine.class)
                    .setParameter("organizationId", organizationId)
                    .list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private Medicine findByCode(Session session, String medicineCode) {
        if (medicineCode == null || medicineCode.trim().isEmpty()) {
            return null;
        }
        return session.createQuery("from Medicine m where lower(m.medicineCode) = :medicineCode", Medicine.class)
                .setParameter("medicineCode", medicineCode.trim().toLowerCase())
                .uniqueResult();
    }
}
