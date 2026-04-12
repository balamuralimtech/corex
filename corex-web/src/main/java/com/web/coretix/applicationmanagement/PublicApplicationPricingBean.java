package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IApplicationPricingService;
import com.persist.coretix.modal.coretix.ApplicationPricing;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.springframework.context.annotation.Scope;

@Named("publicApplicationPricing")
@Scope("request")
public class PublicApplicationPricingBean implements Serializable {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    private transient ApplicationPricing indiaPricing;

    @Inject
    private IApplicationPricingService applicationPricingService;

    public String getOneMonthPriceLabel() {
        return formatPrice(resolveIndiaPricing() == null ? null : resolveIndiaPricing().getOneMonthPrice());
    }

    public String getSixMonthPriceLabel() {
        return formatPrice(resolveIndiaPricing() == null ? null : resolveIndiaPricing().getSixMonthPrice());
    }

    public String getOneYearPriceLabel() {
        return formatPrice(resolveIndiaPricing() == null ? null : resolveIndiaPricing().getOneYearPrice());
    }

    public String getCurrencySymbol() {
        ApplicationPricing applicationPricing = resolveIndiaPricing();
        return applicationPricing == null || blank(applicationPricing.getCurrencySymbol()) ? "Rs." : applicationPricing.getCurrencySymbol();
    }

    public boolean isPricingAvailable() {
        return resolveIndiaPricing() != null;
    }

    public String getPricingHeadline() {
        return isPricingAvailable()
                ? "Simple subscription pricing, managed centrally."
                : "Pricing will appear here once it is configured in application management.";
    }

    public String getPricingDescription() {
        return isPricingAvailable()
                ? "CareX reads India pricing from the shared application pricing configuration, so updates happen from one common admin page."
                : "Set India pricing for this application from Application Management to publish the plans on the landing page.";
    }

    private ApplicationPricing resolveIndiaPricing() {
        if (indiaPricing == null) {
            indiaPricing = applicationPricingService.getApplicationPricingByApplicationAndCountry(resolveApplicationCode(), "IN");
        }
        return indiaPricing;
    }

    private String resolveApplicationCode() {
        String applicationCode = System.getProperty("app.context");
        if (!blank(applicationCode)) {
            return applicationCode.trim();
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return "carex";
        }

        String contextPath = context.getExternalContext().getRequestContextPath();
        if (blank(contextPath) || "/".equals(contextPath.trim())) {
            return "carex";
        }

        return contextPath.replace("/", "").trim();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Set in Application Management";
        }
        return getCurrencySymbol() + " " + PRICE_FORMAT.format(price);
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
