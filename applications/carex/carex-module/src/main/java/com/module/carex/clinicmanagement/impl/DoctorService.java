package com.module.carex.clinicmanagement.impl;

import com.module.carex.clinicmanagement.IDoctorService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Doctor;
import com.persist.carex.clinicmanagement.dao.IDoctorDAO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class DoctorService implements IDoctorService {

    @Inject
    private IDoctorDAO doctorDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    public GeneralConstants addDoctor(UserActivityTO userActivityTO, Doctor doctor) {
        GeneralConstants result = doctorDAO.addDoctor(doctor);
        userActivityTO.setActivityDescription("Add doctor - (" + resolveDoctorLabel(doctor) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public GeneralConstants updateDoctor(UserActivityTO userActivityTO, Doctor doctor) {
        GeneralConstants result = doctorDAO.updateDoctor(doctor);
        userActivityTO.setActivityDescription("Update doctor - (" + resolveDoctorLabel(doctor) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public GeneralConstants deleteDoctor(UserActivityTO userActivityTO, Doctor doctor) {
        GeneralConstants result = doctorDAO.deleteDoctor(doctor);
        userActivityTO.setActivityDescription("Delete doctor - (" + resolveDoctorLabel(doctor) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public Doctor getDoctorById(Integer doctorId) {
        return doctorDAO.getDoctorById(doctorId);
    }

    @Override
    public List<Doctor> getDoctorsByOrganizationId(Integer organizationId) {
        return doctorDAO.getDoctorsByOrganizationId(organizationId);
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        UserActivities userActivity = new UserActivities();
        userActivity.setUserId(userActivityTO.getUserId());
        userActivity.setUserName(userActivityTO.getUserName());
        userActivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        userActivity.setIpAddress(userActivityTO.getIpAddress());
        userActivity.setLocationInfo(userActivityTO.getLocationInfo());
        userActivity.setActivityType(userActivityTO.getActivityType());
        userActivity.setActivityDescription(userActivityTO.getActivityDescription());
        userActivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userActivityDAO.addUserActivity(userActivity);
    }

    private String resolveDoctorLabel(Doctor doctor) {
        if (doctor == null) {
            return "Unknown Doctor";
        }
        String name = doctor.getDoctorName() == null ? "" : doctor.getDoctorName().trim();
        String code = doctor.getDoctorCode() == null ? "" : doctor.getDoctorCode().trim();
        return (name + " " + code).trim();
    }
}
