package com.module.carex.clinicmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.clinicmanagement.Medicine;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IMedicineService {
    GeneralConstants addMedicine(UserActivityTO userActivityTO, Medicine medicine);
    GeneralConstants updateMedicine(UserActivityTO userActivityTO, Medicine medicine);
    GeneralConstants deleteMedicine(UserActivityTO userActivityTO, Medicine medicine);
    Medicine getMedicineById(Integer medicineId);
    List<Medicine> getMedicinesByOrganizationId(Integer organizationId);
}
