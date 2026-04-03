package com.module.carex.clinicmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IConsultationService {
    GeneralConstants addConsultation(UserActivityTO userActivityTO, Consultation consultation);
    GeneralConstants updateConsultation(UserActivityTO userActivityTO, Consultation consultation);
    GeneralConstants deleteConsultation(UserActivityTO userActivityTO, Consultation consultation);
    Consultation getConsultationById(Integer consultationId);
    List<Consultation> getConsultationsByOrganizationId(Integer organizationId);
}
