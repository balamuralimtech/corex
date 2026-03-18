/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface IOrganizationService {
    
    public GeneralConstants addOrganization(UserActivityTO userActivityTO, Organizations organization);

    public GeneralConstants updateOrganization(UserActivityTO userActivityTO, Organizations organization);

    public GeneralConstants deleteOrganization(UserActivityTO userActivityTO, Organizations organization);

    public Organizations getOrganizationById(int id);

    public List<Organizations> getOrganizationsList();
    
    public Organizations getOrganizationsEntityByOrganizationName(String organizationName);
}
