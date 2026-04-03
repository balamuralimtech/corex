package com.module.carex.clinicmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IPatientService {
    GeneralConstants addPatient(UserActivityTO userActivityTO, Patient patient);
    GeneralConstants updatePatient(UserActivityTO userActivityTO, Patient patient);
    GeneralConstants deletePatient(UserActivityTO userActivityTO, Patient patient);
    Patient getPatientById(Integer patientId);
    List<Patient> getPatientsByOrganizationId(Integer organizationId);
}
