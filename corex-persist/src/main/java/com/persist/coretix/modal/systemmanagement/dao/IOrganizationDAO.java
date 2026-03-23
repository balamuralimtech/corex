/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;

import java.util.List;

/**
 *
 * @author balamurali
 */
public interface IOrganizationDAO {
    
    public GeneralConstants addOrganization(Organizations organization);

    public GeneralConstants updateOrganization(Organizations organization);

    public GeneralConstants deleteOrganization(Organizations organization);

    public Organizations getOrganization(int id);

    public List<Organizations> getOrganizationsList();
    
    public Organizations getOrganizationsEntityByOrganizationName(String organizationName);
    
}

