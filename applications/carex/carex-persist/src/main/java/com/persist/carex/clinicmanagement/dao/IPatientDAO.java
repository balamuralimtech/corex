package com.persist.carex.clinicmanagement.dao;

import com.persist.carex.clinicmanagement.Patient;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IPatientDAO {
    GeneralConstants addPatient(Patient patient);
    GeneralConstants updatePatient(Patient patient);
    GeneralConstants deletePatient(Patient patient);
    Patient getPatientById(Integer patientId);
    List<Patient> getPatientsByOrganizationId(Integer organizationId);
}
