package com.module.carex.clinicmanagement.impl;

import com.module.carex.clinicmanagement.IMedicineService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Medicine;
import com.persist.carex.clinicmanagement.dao.IMedicineDAO;
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
public class MedicineService implements IMedicineService {

    @Inject
    private IMedicineDAO medicineDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    public GeneralConstants addMedicine(UserActivityTO userActivityTO, Medicine medicine) {
        GeneralConstants result = medicineDAO.addMedicine(medicine);
        userActivityTO.setActivityDescription("Add medicine - (" + resolveMedicineLabel(medicine) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public GeneralConstants updateMedicine(UserActivityTO userActivityTO, Medicine medicine) {
        GeneralConstants result = medicineDAO.updateMedicine(medicine);
        userActivityTO.setActivityDescription("Update medicine - (" + resolveMedicineLabel(medicine) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public GeneralConstants deleteMedicine(UserActivityTO userActivityTO, Medicine medicine) {
        GeneralConstants result = medicineDAO.deleteMedicine(medicine);
        userActivityTO.setActivityDescription("Delete medicine - (" + resolveMedicineLabel(medicine) + ") - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public Medicine getMedicineById(Integer medicineId) {
        return medicineDAO.getMedicineById(medicineId);
    }

    @Override
    public List<Medicine> getMedicinesByOrganizationId(Integer organizationId) {
        return medicineDAO.getMedicinesByOrganizationId(organizationId);
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

    private String resolveMedicineLabel(Medicine medicine) {
        if (medicine == null) {
            return "Unknown Medicine";
        }
        String name = medicine.getMedicineName() == null ? "" : medicine.getMedicineName().trim();
        String code = medicine.getMedicineCode() == null ? "" : medicine.getMedicineCode().trim();
        return (name + " " + code).trim();
    }
}
