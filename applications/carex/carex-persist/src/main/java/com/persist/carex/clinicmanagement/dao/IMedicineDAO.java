package com.persist.carex.clinicmanagement.dao;

import com.persist.carex.clinicmanagement.Medicine;
import com.persist.coretix.modal.constants.GeneralConstants;

import java.util.List;

public interface IMedicineDAO {
    GeneralConstants addMedicine(Medicine medicine);
    GeneralConstants updateMedicine(Medicine medicine);
    GeneralConstants deleteMedicine(Medicine medicine);
    Medicine getMedicineById(Integer medicineId);
    List<Medicine> getMedicinesByOrganizationId(Integer organizationId);
}
