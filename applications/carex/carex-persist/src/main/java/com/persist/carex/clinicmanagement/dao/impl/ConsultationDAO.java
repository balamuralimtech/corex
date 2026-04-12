package com.persist.carex.clinicmanagement.dao.impl;

import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.ConsultationMedicine;
import com.persist.carex.clinicmanagement.dao.IConsultationDAO;
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
public class ConsultationDAO implements IConsultationDAO {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addConsultation(Consultation consultation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            if (findByConsultationNumber(session, consultation.getConsultationNumber()) != null) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            consultation.setCreatedAt(now);
            consultation.setUpdatedAt(now);
            prepareLineItems(consultation, now, true);
            session.save(consultation);
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
    public GeneralConstants updateConsultation(Consultation consultation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Consultation existing = session.get(Consultation.class, consultation.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            Consultation duplicate = findByConsultationNumber(session, consultation.getConsultationNumber());
            if (duplicate != null && !duplicate.getId().equals(consultation.getId())) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            copyConsultation(existing, consultation);
            existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            existing.getConsultationMedicines().clear();
            Timestamp now = existing.getUpdatedAt();
            if (consultation.getConsultationMedicines() != null) {
                int lineNumber = 1;
                for (ConsultationMedicine sourceLine : consultation.getConsultationMedicines()) {
                    ConsultationMedicine targetLine = cloneLine(sourceLine);
                    targetLine.setLineNumber(lineNumber++);
                    targetLine.setCreatedAt(now);
                    targetLine.setUpdatedAt(now);
                    existing.addConsultationMedicine(targetLine);
                }
            }
            session.merge(existing);
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
    public GeneralConstants deleteConsultation(Consultation consultation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Consultation existing = session.get(Consultation.class, consultation.getId());
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
    public Consultation getConsultationById(Integer consultationId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (consultationId == null) {
                return null;
            }
            return session.createQuery(
                    "SELECT DISTINCT c FROM Consultation c " +
                    "LEFT JOIN FETCH c.organization " +
                    "LEFT JOIN FETCH c.doctor d " +
                    "LEFT JOIN FETCH d.userDetail " +
                    "LEFT JOIN FETCH c.patient " +
                    "LEFT JOIN FETCH c.consultationMedicines " +
                    "WHERE c.id = :consultationId", Consultation.class)
                    .setParameter("consultationId", consultationId)
                    .uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Consultation> getConsultationsByOrganizationId(Integer organizationId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (organizationId == null) {
                return new ArrayList<>();
            }
            return session.createQuery(
                            "SELECT DISTINCT c FROM Consultation c " +
                                    "LEFT JOIN FETCH c.organization " +
                                    "LEFT JOIN FETCH c.doctor d " +
                                    "LEFT JOIN FETCH d.userDetail " +
                                    "LEFT JOIN FETCH c.patient " +
                                    "LEFT JOIN FETCH c.consultationMedicines " +
                                    "WHERE c.organization.id = :organizationId " +
                                    "ORDER BY c.consultationDate DESC, c.id DESC",
                            Consultation.class)
                    .setParameter("organizationId", organizationId)
                    .list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Consultation> getActiveQueueByOrganizationId(Integer organizationId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (organizationId == null) {
                return new ArrayList<>();
            }
            return session.createQuery(
                            "SELECT DISTINCT c FROM Consultation c " +
                                    "LEFT JOIN FETCH c.organization " +
                                    "LEFT JOIN FETCH c.doctor d " +
                                    "LEFT JOIN FETCH d.userDetail " +
                                    "LEFT JOIN FETCH c.patient " +
                                    "WHERE c.organization.id = :organizationId " +
                                    "AND c.tokenNumber is not null " +
                                    "AND lower(c.status) in ('waiting', 'in progress') " +
                                    "ORDER BY c.tokenNumber asc, c.consultationDate asc, c.id asc",
                            Consultation.class)
                    .setParameter("organizationId", organizationId)
                    .list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Integer getNextTokenNumber(Integer organizationId, Timestamp dayStart, Timestamp dayEnd) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            if (organizationId == null || dayStart == null || dayEnd == null) {
                return 1;
            }
            Integer maxToken = session.createQuery(
                            "select max(c.tokenNumber) from Consultation c " +
                                    "where c.organization.id = :organizationId " +
                                    "and c.consultationDate >= :dayStart " +
                                    "and c.consultationDate < :dayEnd",
                            Integer.class)
                    .setParameter("organizationId", organizationId)
                    .setParameter("dayStart", dayStart)
                    .setParameter("dayEnd", dayEnd)
                    .uniqueResult();
            return maxToken == null ? 1 : maxToken + 1;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void prepareLineItems(Consultation consultation, Timestamp now, boolean setCreatedAt) {
        if (consultation.getConsultationMedicines() == null) {
            consultation.setConsultationMedicines(new ArrayList<>());
            return;
        }
        int lineNumber = 1;
        for (ConsultationMedicine line : consultation.getConsultationMedicines()) {
            line.setConsultation(consultation);
            line.setLineNumber(lineNumber++);
            if (setCreatedAt) {
                line.setCreatedAt(now);
            }
            line.setUpdatedAt(now);
        }
    }

    private ConsultationMedicine cloneLine(ConsultationMedicine sourceLine) {
        ConsultationMedicine targetLine = new ConsultationMedicine();
        targetLine.setMedicine(sourceLine.getMedicine());
        targetLine.setDescriptionText(sourceLine.getDescriptionText());
        targetLine.setDose(sourceLine.getDose());
        targetLine.setFrequency(sourceLine.getFrequency());
        targetLine.setDurationText(sourceLine.getDurationText());
        targetLine.setRemarks(sourceLine.getRemarks());
        targetLine.setQuantity(sourceLine.getQuantity());
        targetLine.setUnitPrice(sourceLine.getUnitPrice());
        targetLine.setLineTotal(sourceLine.getLineTotal());
        return targetLine;
    }

    private void copyConsultation(Consultation target, Consultation source) {
        target.setOrganization(source.getOrganization());
        target.setDoctor(source.getDoctor());
        target.setPatient(source.getPatient());
        target.setConsultationNumber(source.getConsultationNumber());
        target.setConsultationDate(source.getConsultationDate());
        target.setTokenNumber(source.getTokenNumber());
        target.setPatientAgeYears(source.getPatientAgeYears());
        target.setTemperatureCelsius(source.getTemperatureCelsius());
        target.setWeightKg(source.getWeightKg());
        target.setBloodPressure(source.getBloodPressure());
        target.setSymptoms(source.getSymptoms());
        target.setFamilyHistory(source.getFamilyHistory());
        target.setVitals(source.getVitals());
        target.setFindings(source.getFindings());
        target.setDiagnosis(source.getDiagnosis());
        target.setFollowUpNote(source.getFollowUpNote());
        target.setDoctorNotes(source.getDoctorNotes());
        target.setIssueInvoice(source.isIssueInvoice());
        target.setIssueMedicalCertificate(source.isIssueMedicalCertificate());
        target.setInvoicePaidBy(source.getInvoicePaidBy());
        target.setInvoiceIssueDate(source.getInvoiceIssueDate());
        target.setInvoiceDueDate(source.getInvoiceDueDate());
        target.setMedicalCertificateDisease(source.getMedicalCertificateDisease());
        target.setMedicalCertificateTreatmentDuration(source.getMedicalCertificateTreatmentDuration());
        target.setMedicalCertificatePlace(source.getMedicalCertificatePlace());
        target.setConsultationFee(source.getConsultationFee());
        target.setMedicineTotal(source.getMedicineTotal());
        target.setMedicalCertificateFee(source.getMedicalCertificateFee());
        target.setInvoiceTotal(source.getInvoiceTotal());
        target.setStatus(source.getStatus());
    }

    private Consultation findByConsultationNumber(Session session, String consultationNumber) {
        if (consultationNumber == null || consultationNumber.trim().isEmpty()) {
            return null;
        }
        return session.createQuery("from Consultation c where lower(c.consultationNumber) = :consultationNumber", Consultation.class)
                .setParameter("consultationNumber", consultationNumber.trim().toLowerCase())
                .uniqueResult();
    }
}
