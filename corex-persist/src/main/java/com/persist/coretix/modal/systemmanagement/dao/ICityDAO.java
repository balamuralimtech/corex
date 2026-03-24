/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: `app.name`
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




