/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Countries;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface ICountryDAO {

    public GeneralConstants addCountry(Countries country);

    public GeneralConstants updateCountry(Countries country);

    public GeneralConstants deleteCountry(Countries country);

    public Countries getCountry(int id);
    
    public Countries getCountryEntityByCountryName(String countryName);

    public List<Countries> getCountriesList();
}
