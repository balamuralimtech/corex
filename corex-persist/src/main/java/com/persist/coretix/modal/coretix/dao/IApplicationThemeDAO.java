/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.coretix.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationTheme;
import com.persist.coretix.modal.systemmanagement.Organizations;

import java.util.List;

/**
 *
 * @author balamurali
 */
public interface IApplicationThemeDAO {
    
    public GeneralConstants addApplicationTheme(ApplicationTheme applicationTheme);

    public GeneralConstants updateApplicationTheme(ApplicationTheme applicationTheme);

    public ApplicationTheme getApplicationThemeByUserid(int userid);
    
}

