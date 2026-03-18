/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Subregions;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface ISubRegionDAO {
        public GeneralConstants addSubRegion(Subregions subRegion);

    public GeneralConstants updateSubRegion(Subregions subRegion);

    public GeneralConstants deleteSubRegion(Subregions subRegion);

    public Subregions getSubRegion(int id);
    
    public Subregions getSubregionBySubregionName(String subregionName);

    public List<Subregions> getSubRegionsList();
    
    
}
