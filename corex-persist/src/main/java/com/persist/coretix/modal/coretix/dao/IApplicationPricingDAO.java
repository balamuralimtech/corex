package com.persist.coretix.modal.coretix.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationPricing;

import java.util.List;

public interface IApplicationPricingDAO {

    GeneralConstants addApplicationPricing(ApplicationPricing applicationPricing);

    GeneralConstants updateApplicationPricing(ApplicationPricing applicationPricing);

    GeneralConstants deleteApplicationPricing(ApplicationPricing applicationPricing);

    ApplicationPricing getApplicationPricing(int id);

    ApplicationPricing getApplicationPricingByApplicationAndCountry(String applicationCode, String countryCode);

    List<ApplicationPricing> getApplicationPricingList();
}
