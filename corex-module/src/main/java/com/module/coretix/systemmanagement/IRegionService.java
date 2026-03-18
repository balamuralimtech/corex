/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Regions;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IRegionService {

    public GeneralConstants addRegion(UserActivityTO userActivityTO, Regions region);

    public GeneralConstants updateRegion(UserActivityTO userActivityTO,Regions region);

    public GeneralConstants deleteRegion(UserActivityTO userActivityTO,Regions region);

    public Regions getRegionById(int id);

    public Regions getRegionByRegionName(String regionName);

    public List<Regions> getRegionsList();

}
