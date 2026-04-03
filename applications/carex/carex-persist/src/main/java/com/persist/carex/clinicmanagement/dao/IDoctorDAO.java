package com.persist.carex.clinicmanagement.dao;

import com.persist.carex.clinicmanagement.Doctor;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IDoctorDAO {
    GeneralConstants addDoctor(Doctor doctor);
    GeneralConstants updateDoctor(Doctor doctor);
    GeneralConstants deleteDoctor(Doctor doctor);
    Doctor getDoctorById(Integer doctorId);
    Doctor getDoctorByCode(String doctorCode);
    List<Doctor> getDoctorsByOrganizationId(Integer organizationId);
}
