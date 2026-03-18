/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Subregions;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface ISubRegionService {
        public GeneralConstants addSubRegion(UserActivityTO userActivityTO, Subregions subRegion);

    public GeneralConstants updateSubRegion(UserActivityTO userActivityTO,Subregions subRegion);

    public GeneralConstants deleteSubRegion(UserActivityTO userActivityTO,Subregions subRegion);

    public Subregions getSubRegionById(int id);
    
    public Subregions getSubregionBySubregionName(String subregionName);

    public List<Subregions> getSubRegionsList();
    
}
