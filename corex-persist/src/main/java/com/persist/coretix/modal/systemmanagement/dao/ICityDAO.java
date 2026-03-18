/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Cities;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface ICityDAO {

    public GeneralConstants addCity(Cities state);

    public GeneralConstants updateCity(Cities state);

    public GeneralConstants deleteCity(Cities state);

    public Cities getCity(int id);
    
    public Cities getCityEntityByCityName(String cityName);

    public List<Cities> getCitiesList();
    
    public List<Cities> getCitiesListByCountryIdAndStateId(int countryId, int stateId);
}
