package com.module.carex.clinicmanagement.impl;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.dao.IConsultationDAO;
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
public class ConsultationService implements IConsultationService {

    @Inject
    private IConsultationDAO consultationDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addConsultation(UserActivityTO userActivityTO, Consultation consultation) {
        GeneralConstants result = consultationDAO.addConsultation(consultation);
        userActivityTO.setActivityDescription("Add consultation - (" + resolveConsultationLabel(consultation) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updateConsultation(UserActivityTO userActivityTO, Consultation consultation) {
        GeneralConstants result = consultationDAO.updateConsultation(consultation);
        userActivityTO.setActivityDescription("Update consultation - (" + resolveConsultationLabel(consultation) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteConsultation(UserActivityTO userActivityTO, Consultation consultation) {
        GeneralConstants result = consultationDAO.deleteConsultation(consultation);
        userActivityTO.setActivityDescription("Delete consultation - (" + resolveConsultationLabel(consultation) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public Consultation getConsultationById(Integer consultationId) {
        return consultationDAO.getConsultationById(consultationId);
    }

    @Override
    public List<Consultation> getConsultationsByOrganizationId(Integer organizationId) {
        return consultationDAO.getConsultationsByOrganizationId(organizationId);
    }

    @Override
    public List<Consultation> getActiveQueueByOrganizationId(Integer organizationId) {
        return consultationDAO.getActiveQueueByOrganizationId(organizationId);
    }

    @Override
    public Integer getNextTokenNumber(Integer organizationId, Timestamp dayStart, Timestamp dayEnd) {
        return consultationDAO.getNextTokenNumber(organizationId, dayStart, dayEnd);
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

    private String resolveConsultationLabel(Consultation consultation) {
        if (consultation == null) {
            return "Unknown Consultation";
        }
        String consultationNumber = consultation.getConsultationNumber() == null ? "" : consultation.getConsultationNumber().trim();
        String patientName = consultation.getPatient() == null || consultation.getPatient().getPatientName() == null
                ? ""
                : consultation.getPatient().getPatientName().trim();
        return (consultationNumber + " " + patientName).trim();
    }
}
