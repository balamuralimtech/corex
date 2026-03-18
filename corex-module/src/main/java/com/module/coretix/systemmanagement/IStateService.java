/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.States;
import java.util.List;

/**
 *
 * @author admin
 */
public interface IStateService {
    public GeneralConstants addState(UserActivityTO userActivityTO, States state);

    public GeneralConstants updateState(UserActivityTO userActivityTO,States state);

    public GeneralConstants deleteState(UserActivityTO userActivityTO,
                                        States state);
    
    public States getStateEntityByStateName(String stateName);

    public States getStateById(int id);

    public List<States> getStatesList();
    
    public List<States> getStatesListByCountryId(int countryId);
}
