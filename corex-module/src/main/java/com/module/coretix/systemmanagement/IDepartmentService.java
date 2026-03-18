/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Departments;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IDepartmentService {
    
    public GeneralConstants addDepartment(UserActivityTO userActivityTO, Departments department);

    public GeneralConstants updateDepartment(UserActivityTO userActivityTO,Departments department);

    public GeneralConstants deleteDepartment(UserActivityTO userActivityTO,Departments department);

    public Departments getDepartmentById(int id);

    public List<Departments> getDepartmentsList();
    
}
