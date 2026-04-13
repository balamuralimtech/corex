package com.module.carex.clinicmanagement.impl;

import com.module.carex.clinicmanagement.IPatientService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.clinicmanagement.dao.IPatientDAO;
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
public class PatientService implements IPatientService {

    @Inject
    private IPatientDAO patientDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addPatient(UserActivityTO userActivityTO, Patient patient) {
        GeneralConstants result = patientDAO.addPatient(patient);
        userActivityTO.setActivityDescription("Add patient - (" + resolvePatientLabel(patient) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updatePatient(UserActivityTO userActivityTO, Patient patient) {
        GeneralConstants result = patientDAO.updatePatient(patient);
        userActivityTO.setActivityDescription("Update patient - (" + resolvePatientLabel(patient) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deletePatient(UserActivityTO userActivityTO, Patient patient) {
        GeneralConstants result = patientDAO.deletePatient(patient);
        userActivityTO.setActivityDescription("Delete patient - (" + resolvePatientLabel(patient) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public Patient getPatientById(Integer patientId) {
        return patientDAO.getPatientById(patientId);
    }

    @Override
    public List<Patient> getPatientsByOrganizationId(Integer organizationId) {
        return patientDAO.getPatientsByOrganizationId(organizationId);
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

    private String resolvePatientLabel(Patient patient) {
        if (patient == null) {
            return "Unknown Patient";
        }
        String name = patient.getPatientName() == null ? "" : patient.getPatientName().trim();
        String code = patient.getPatientCode() == null ? "" : patient.getPatientCode().trim();
        return (name + " " + code).trim();
    }
}
