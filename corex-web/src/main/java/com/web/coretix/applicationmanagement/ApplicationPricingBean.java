package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IApplicationPricingService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationPricing;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Named("applicationPricingBean")
@Scope("session")
public class ApplicationPricingBean implements Serializable {

    private List<ApplicationPricing> applicationPricingList = new ArrayList<>();
    private boolean datatableRendered;
    private boolean addOperation = true;
    private int recordsCount;
    private ApplicationPricing selectedApplicationPricing = new ApplicationPricing();

    private String applicationCode;
    private String applicationName;
    private String countryCode;
    private String countryName;
    private String currencyCode;
    private String currencySymbol;
    private BigDecimal oneMonthPrice;
    private BigDecimal sixMonthPrice;
    private BigDecimal oneYearPrice;
    private Integer displayOrder;
    private boolean active = true;
    private String notes;

    @Inject
    private transient IApplicationPricingService applicationPricingService;

    public void initializePageAttributes() {
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().isPostback()) {
            return;
        }
        resetFields();
        fetchPricingList();
    }

    public void searchButtonAction() {
        fetchPricingList();
    }

    public void addButtonAction() {
        addOperation = true;
        resetFields();
    }

    public void editButtonAction() {
        if (selectedApplicationPricing == null) {
            return;
        }
        addOperation = false;
        applicationCode = selectedApplicationPricing.getApplicationCode();
        applicationName = selectedApplicationPricing.getApplicationName();
        countryCode = selectedApplicationPricing.getCountryCode();
        countryName = selectedApplicationPricing.getCountryName();
        currencyCode = selectedApplicationPricing.getCurrencyCode();
        currencySymbol = selectedApplicationPricing.getCurrencySymbol();
        oneMonthPrice = selectedApplicationPricing.getOneMonthPrice();
        sixMonthPrice = selectedApplicationPricing.getSixMonthPrice();
        oneYearPrice = selectedApplicationPricing.getOneYearPrice();
        displayOrder = selectedApplicationPricing.getDisplayOrder();
        active = selectedApplicationPricing.isActive();
        notes = selectedApplicationPricing.getNotes();
    }

    public void saveApplicationPricing() {
        try {
            ApplicationPricing applicationPricing = new ApplicationPricing();
            applicationPricing.setApplicationCode(normalize(applicationCode).toLowerCase());
            applicationPricing.setApplicationName(normalize(applicationName));
            applicationPricing.setCountryCode(normalize(countryCode).toUpperCase());
            applicationPricing.setCountryName(normalize(countryName));
            applicationPricing.setCurrencyCode(normalize(currencyCode).toUpperCase());
            applicationPricing.setCurrencySymbol(normalize(currencySymbol));
            applicationPricing.setOneMonthPrice(requirePrice(oneMonthPrice, "1 month"));
            applicationPricing.setSixMonthPrice(requirePrice(sixMonthPrice, "6 months"));
            applicationPricing.setOneYearPrice(requirePrice(oneYearPrice, "1 year"));
            applicationPricing.setDisplayOrder(displayOrder);
            applicationPricing.setActive(active);
            applicationPricing.setNotes(normalize(notes));

            GeneralConstants result;
            if (addOperation) {
                result = applicationPricingService.addApplicationPricing(applicationPricing);
            } else {
                applicationPricing.setId(selectedApplicationPricing.getId());
                result = applicationPricingService.updateApplicationPricing(applicationPricing);
            }

            if (result == GeneralConstants.SUCCESSFUL) {
                fetchPricingList();
                PrimeFaces.current().executeScript("PF('ManagePricingDialog').hide()");
                PrimeFaces.current().ajax().update("form:messages", "form:pricingMainPanelId");
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Pricing saved"));
                return;
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", resolveErrorMessage(result)));
            PrimeFaces.current().ajax().update("form:messages");
        } catch (IllegalArgumentException exception) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", exception.getMessage()));
            PrimeFaces.current().ajax().update("form:messages");
        }
    }

    public void deleteApplicationPricing() {
        if (selectedApplicationPricing == null) {
            return;
        }
        GeneralConstants result = applicationPricingService.deleteApplicationPricing(selectedApplicationPricing);
        if (result == GeneralConstants.SUCCESSFUL) {
            fetchPricingList();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Pricing removed"));
            PrimeFaces.current().ajax().update("form:messages", "form:pricingMainPanelId");
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", resolveErrorMessage(result)));
        PrimeFaces.current().ajax().update("form:messages");
    }

    private void fetchPricingList() {
        applicationPricingList = new ArrayList<>(applicationPricingService.getApplicationPricingList());
        datatableRendered = CollectionUtils.isNotEmpty(applicationPricingList);
        recordsCount = applicationPricingList.size();
    }

    private void resetFields() {
        addOperation = true;
        applicationCode = "";
        applicationName = "";
        countryCode = "IN";
        countryName = "India";
        currencyCode = "INR";
        currencySymbol = "Rs.";
        oneMonthPrice = null;
        sixMonthPrice = null;
        oneYearPrice = null;
        displayOrder = null;
        active = true;
        notes = "";
        selectedApplicationPricing = new ApplicationPricing();
    }

    private BigDecimal requirePrice(BigDecimal value, String label) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Please enter a valid " + label + " price.");
        }
        return value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveErrorMessage(GeneralConstants result) {
        if (result == GeneralConstants.ENTRY_ALREADY_EXISTS) {
            return "Pricing already exists for this application and country.";
        }
        if (result == GeneralConstants.ENTRY_NOT_EXISTS) {
            return "Pricing entry not found.";
        }
        if (result == GeneralConstants.ENTRY_IN_USE) {
            return "Pricing entry is currently in use.";
        }
        return "Unable to save pricing.";
    }

    public List<ApplicationPricing> getApplicationPricingList() {
        return applicationPricingList;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public boolean isAddOperation() {
        return addOperation;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public ApplicationPricing getSelectedApplicationPricing() {
        return selectedApplicationPricing;
    }

    public void setSelectedApplicationPricing(ApplicationPricing selectedApplicationPricing) {
        this.selectedApplicationPricing = selectedApplicationPricing;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public BigDecimal getOneMonthPrice() {
        return oneMonthPrice;
    }

    public void setOneMonthPrice(BigDecimal oneMonthPrice) {
        this.oneMonthPrice = oneMonthPrice;
    }

    public BigDecimal getSixMonthPrice() {
        return sixMonthPrice;
    }

    public void setSixMonthPrice(BigDecimal sixMonthPrice) {
        this.sixMonthPrice = sixMonthPrice;
    }

    public BigDecimal getOneYearPrice() {
        return oneYearPrice;
    }

    public void setOneYearPrice(BigDecimal oneYearPrice) {
        this.oneYearPrice = oneYearPrice;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
