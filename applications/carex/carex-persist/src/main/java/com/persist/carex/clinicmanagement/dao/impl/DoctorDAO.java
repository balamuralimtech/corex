package com.persist.carex.clinicmanagement.dao.impl;

import com.persist.carex.clinicmanagement.Doctor;
import com.persist.carex.clinicmanagement.dao.IDoctorDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Named
public class DoctorDAO implements IDoctorDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addDoctor(Doctor doctor) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            if (findByCode(session, doctor.getDoctorCode()) != null) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            if (findByUserDetailId(session, doctor.getUserDetail() == null ? null : doctor.getUserDetail().getUserId()) != null) {
                return GeneralConstants.ENTRY_IN_USE;
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            doctor.setCreatedAt(now);
            doctor.setUpdatedAt(now);
            session.save(doctor);
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
    public GeneralConstants updateDoctor(Doctor doctor) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Doctor existing = session.get(Doctor.class, doctor.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            Doctor duplicate = findByCode(session, doctor.getDoctorCode());
            if (duplicate != null && !duplicate.getId().equals(doctor.getId())) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            Doctor linkedUserDoctor = findByUserDetailId(session, doctor.getUserDetail() == null ? null : doctor.getUserDetail().getUserId());
            if (linkedUserDoctor != null && !linkedUserDoctor.getId().equals(doctor.getId())) {
                return GeneralConstants.ENTRY_IN_USE;
            }
            doctor.setCreatedAt(existing.getCreatedAt());
            doctor.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            session.merge(doctor);
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
    public GeneralConstants deleteDoctor(Doctor doctor) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Doctor existing = session.get(Doctor.class, doctor.getId());
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
    public Doctor getDoctorById(Integer doctorId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return doctorId == null ? null : session.get(Doctor.class, doctorId);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Doctor getDoctorByCode(String doctorCode) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return findByCode(session, doctorCode);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Doctor> getDoctorsByOrganizationId(Integer organizationId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (organizationId == null) {
                return Collections.emptyList();
            }
            @SuppressWarnings("unchecked")
            List<Doctor> doctors = session.createQuery("from Doctor d where d.organization.id = :organizationId order by d.doctorName")
                    .setParameter("organizationId", organizationId)
                    .list();
            return doctors;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private Doctor findByCode(Session session, String doctorCode) {
        if (doctorCode == null || doctorCode.trim().isEmpty()) {
            return null;
        }
        return session.createQuery("from Doctor d where lower(d.doctorCode) = :doctorCode", Doctor.class)
                .setParameter("doctorCode", doctorCode.trim().toLowerCase())
                .uniqueResult();
    }

    private Doctor findByUserDetailId(Session session, Integer userDetailId) {
        if (userDetailId == null) {
            return null;
        }
        return session.createQuery("from Doctor d where d.userDetail.userId = :userDetailId", Doctor.class)
                .setParameter("userDetailId", userDetailId)
                .uniqueResult();
    }
}
