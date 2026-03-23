/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Departments;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IDepartmentDAO {

    public GeneralConstants addDepartment(Departments department);

    public GeneralConstants updateDepartment(Departments department);

    public GeneralConstants deleteDepartment(Departments department);

    public Departments getDepartment(int id);

    public List<Departments> getDepartmentsList();
}

