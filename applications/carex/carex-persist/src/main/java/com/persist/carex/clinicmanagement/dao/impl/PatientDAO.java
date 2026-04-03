package com.persist.carex.clinicmanagement.dao.impl;

import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.clinicmanagement.dao.IPatientDAO;
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
public class PatientDAO implements IPatientDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addPatient(Patient patient) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            if (findByCode(session, patient.getPatientCode()) != null) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            patient.setCreatedAt(now);
            patient.setUpdatedAt(now);
            session.save(patient);
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
    public GeneralConstants updatePatient(Patient patient) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Patient existing = session.get(Patient.class, patient.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            Patient duplicate = findByCode(session, patient.getPatientCode());
            if (duplicate != null && !duplicate.getId().equals(patient.getId())) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            patient.setCreatedAt(existing.getCreatedAt());
            patient.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            session.merge(patient);
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
    public GeneralConstants deletePatient(Patient patient) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Patient existing = session.get(Patient.class, patient.getId());
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
    public Patient getPatientById(Integer patientId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return patientId == null ? null : session.get(Patient.class, patientId);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Patient> getPatientsByOrganizationId(Integer organizationId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (organizationId == null) {
                return new ArrayList<>();
            }
            return session.createQuery("from Patient p where p.organization.id = :organizationId order by p.patientName", Patient.class)
                    .setParameter("organizationId", organizationId)
                    .list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private Patient findByCode(Session session, String patientCode) {
        if (patientCode == null || patientCode.trim().isEmpty()) {
            return null;
        }
        return session.createQuery("from Patient p where lower(p.patientCode) = :patientCode", Patient.class)
                .setParameter("patientCode", patientCode.trim().toLowerCase())
                .uniqueResult();
    }
}
