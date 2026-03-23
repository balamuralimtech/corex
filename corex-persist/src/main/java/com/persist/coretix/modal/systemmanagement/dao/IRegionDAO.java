/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Regions;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IRegionDAO {
    public GeneralConstants addRegion(Regions region);

    public GeneralConstants updateRegion(Regions region);

    public GeneralConstants deleteRegion(Regions region);

    public Regions getRegion(int id);

    public Regions getRegionByRegionName(String regionName);

    public List<Regions> getRegionsList();

}

