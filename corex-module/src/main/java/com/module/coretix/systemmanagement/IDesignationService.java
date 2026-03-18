/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Designations;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IDesignationService {
    
    public GeneralConstants addDesignation(UserActivityTO userActivityTO, Designations designation);

    public GeneralConstants updateDesignation(UserActivityTO userActivityTO,Designations designation);

    public GeneralConstants deleteDesignation(UserActivityTO userActivityTO,Designations designation);

    public Designations getDesignationById(int id);

    public List<Designations> getDesignationsList();
    
}
