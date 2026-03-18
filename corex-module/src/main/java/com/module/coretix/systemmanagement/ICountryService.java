/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Countries;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface ICountryService {

    public GeneralConstants addCountry(UserActivityTO userActivityTO, Countries country);

    public GeneralConstants updateCountry(UserActivityTO userActivityTO,Countries country);

    public GeneralConstants deleteCountry(UserActivityTO userActivityTO,Countries country);

    public Countries getCountryById(int id);
    
    public Countries getCountryEntityByCountryName(String countryName);

    public List<Countries> getCountriesList();
}
