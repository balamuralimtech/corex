package com.module.carex.clinicmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Doctor;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IDoctorService {
    GeneralConstants addDoctor(UserActivityTO userActivityTO, Doctor doctor);
    GeneralConstants updateDoctor(UserActivityTO userActivityTO, Doctor doctor);
    GeneralConstants deleteDoctor(UserActivityTO userActivityTO, Doctor doctor);
    Doctor getDoctorById(Integer doctorId);
    List<Doctor> getDoctorsByOrganizationId(Integer organizationId);
}
