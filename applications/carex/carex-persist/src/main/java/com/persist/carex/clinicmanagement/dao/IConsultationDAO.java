package com.persist.carex.clinicmanagement.dao;

import com.persist.carex.clinicmanagement.Consultation;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IConsultationDAO {
    GeneralConstants addConsultation(Consultation consultation);
    GeneralConstants updateConsultation(Consultation consultation);
    GeneralConstants deleteConsultation(Consultation consultation);
    Consultation getConsultationById(Integer consultationId);
    List<Consultation> getConsultationsByOrganizationId(Integer organizationId);
    List<Consultation> getActiveQueueByOrganizationId(Integer organizationId);
    Integer getNextTokenNumber(Integer organizationId, java.sql.Timestamp dayStart, java.sql.Timestamp dayEnd);
}
