/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.CurrencyDetails;
import java.util.List;

/**
 *
 * @author balamurali
 */
public interface ICurrencyDetailsService {

    public GeneralConstants addCurrencyDetails(UserActivityTO userActivityTO, CurrencyDetails currencyDetail);

    public GeneralConstants updateCurrencyDetails(UserActivityTO userActivityTO,CurrencyDetails currencyDetail);

    public GeneralConstants deleteCurrencyDetails(UserActivityTO userActivityTO,CurrencyDetails currencyDetail);

    public CurrencyDetails getCurrencyDetailsById(int id);

    public CurrencyDetails getCurrencyDetailsEntityByCurrencyName(String currencyName);

    public List<CurrencyDetails> getCurrencyDetailsList();
}
