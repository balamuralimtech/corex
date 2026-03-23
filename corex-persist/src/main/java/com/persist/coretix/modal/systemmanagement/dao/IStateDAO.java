/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.States;
import java.util.List;

/**
 *
 * @author admin
 */
public interface IStateDAO {

    public GeneralConstants addState(States state);

    public GeneralConstants updateState(States state);

    public GeneralConstants deleteState(States state);

    public States getState(int id);

    public States getStateEntityByStateName(String stateName);

    public List<States> getStatesList();
    
    public List<States> getStatesListByCountryId(int countryId);

}

