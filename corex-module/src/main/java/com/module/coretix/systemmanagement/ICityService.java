/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Cities;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface ICityService {

    public GeneralConstants addCity(UserActivityTO userActivityTO,Cities city);

    public GeneralConstants updateCity(UserActivityTO userActivityTO,Cities city);

    public GeneralConstants deleteCity(UserActivityTO userActivityTO, Cities city);

    public Cities getCityById(int id);

    public Cities getCityEntityByCityName(String cityName);

    public List<Cities> getCitiesList();

    public List<Cities> getCitiesListByCountryIdAndStateId(int countryId, int stateId);

}
