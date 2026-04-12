package com.module.coretix.coretix.impl;

import com.module.coretix.coretix.IApplicationPricingService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationPricing;
import com.persist.coretix.modal.coretix.dao.IApplicationPricingDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class ApplicationPricingService implements IApplicationPricingService {

    @Inject
    private IApplicationPricingDAO applicationPricingDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addApplicationPricing(ApplicationPricing applicationPricing) {
        return applicationPricingDAO.addApplicationPricing(applicationPricing);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updateApplicationPricing(ApplicationPricing applicationPricing) {
        return applicationPricingDAO.updateApplicationPricing(applicationPricing);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteApplicationPricing(ApplicationPricing applicationPricing) {
        return applicationPricingDAO.deleteApplicationPricing(applicationPricing);
    }

    @Override
    public ApplicationPricing getApplicationPricing(int id) {
        return applicationPricingDAO.getApplicationPricing(id);
    }

    @Override
    public ApplicationPricing getApplicationPricingByApplicationAndCountry(String applicationCode, String countryCode) {
        return applicationPricingDAO.getApplicationPricingByApplicationAndCountry(applicationCode, countryCode);
    }

    @Override
    public List<ApplicationPricing> getApplicationPricingList() {
        return applicationPricingDAO.getApplicationPricingList();
    }
}
