/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface ICurrencyDetailsDAO 
{
    public GeneralConstants addCurrencyDetails(CurrencyDetails currencyDetail);

    public GeneralConstants updateCurrencyDetails(CurrencyDetails currencyDetail);

    public GeneralConstants deleteCurrencyDetails(CurrencyDetails currencyDetail);

    public CurrencyDetails getCurrencyDetailsById(int id);

    public CurrencyDetails getCurrencyDetailsEntityByCurrencyName(String currencyName);

    public List<CurrencyDetails> getCurrencyDetailsList();
}
