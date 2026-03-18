/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Designations;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IDesignationDAO {

    public GeneralConstants addDesignation(Designations designation);

    public GeneralConstants updateDesignation(Designations designation);

    public GeneralConstants deleteDesignation(Designations designation);

    public Designations getDesignation(int id);

    public List<Designations> getDesignationsList();

}
