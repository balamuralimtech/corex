package com.web.shipx.request;

import com.module.coretix.systemmanagement.ICountryService;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.shipx.request.CustomerRequest;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class CustomerRequestFormSupport implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> countryNames = new ArrayList<>();
    private String customerType = "INDIVIDUAL";
    private String customerName;
    private String originCountry;
    private String destinationCountry;
    private String finalDestinationDetails;
    private String capacityType;
    private String spaceSize;
    private String containerCount;
    private String weightValue;
    private String weightUnit;
    private Date estimatedShippingDate;
    private String contactPerson;
    private String contactNumber;
    private String howYouKnowUs;
    private String lastSubmittedReference;

    protected void initializeForm(ICountryService countryService) {
        loadCountryNames(countryService);
        if (customerType == null || customerType.trim().isEmpty()) {
            customerType = "INDIVIDUAL";
        }
    }

    protected void loadCountryNames(ICountryService countryService) {
        countryNames.clear();
        List<Countries> countries = countryService.getCountriesList();
        for (Countries country : countries) {
            countryNames.add(country.getName());
        }
        countryNames.sort(String::compareToIgnoreCase);
    }

    protected boolean populateCustomerRequest(CustomerRequest customerRequest, ICountryService countryService) {
        if (!validateForm()) {
            return false;
        }

        Countries originCountryEntity = countryService.getCountryEntityByCountryName(originCountry);
        Countries destinationCountryEntity = countryService.getCountryEntityByCountryName(destinationCountry);
        if (originCountryEntity == null || destinationCountryEntity == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid country selection",
                    "Please select a valid origin and destination country.");
            return false;
        }

        customerRequest.setCustomerType(customerType);
        customerRequest.setCustomerName(customerName.trim());
        customerRequest.setOriginCountry(originCountryEntity);
        customerRequest.setDestinationCountry(destinationCountryEntity);
        customerRequest.setFinalDestinationDetails(trimToNull(finalDestinationDetails));
        customerRequest.setCapacityType(capacityType);
        customerRequest.setSpaceSize(isSpaceSelected() ? trimToNull(spaceSize) : null);
        customerRequest.setContainerCount(isContainerSelected() ? Integer.valueOf(containerCount.trim()) : null);
        customerRequest.setWeightValue(isWeightSelected() ? new BigDecimal(weightValue.trim()) : null);
        customerRequest.setWeightUnit(isWeightSelected() ? weightUnit : null);
        customerRequest.setEstimatedShippingDate(estimatedShippingDate);
        customerRequest.setContactPerson(contactPerson.trim());
        customerRequest.setContactNumber(contactNumber.trim());
        customerRequest.setHowYouKnowUs(trimToNull(howYouKnowUs));
        customerRequest.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        customerRequest.setFlag(true);
        return true;
    }

    protected void loadFromCustomerRequest(CustomerRequest customerRequest) {
        if (customerRequest == null) {
            resetForm();
            return;
        }

        customerType = customerRequest.getCustomerType();
        customerName = customerRequest.getCustomerName();
        originCountry = customerRequest.getOriginCountry() == null ? null : customerRequest.getOriginCountry().getName();
        destinationCountry = customerRequest.getDestinationCountry() == null ? null : customerRequest.getDestinationCountry().getName();
        finalDestinationDetails = customerRequest.getFinalDestinationDetails();
        capacityType = customerRequest.getCapacityType();
        spaceSize = customerRequest.getSpaceSize();
        containerCount = customerRequest.getContainerCount() == null ? null : String.valueOf(customerRequest.getContainerCount());
        weightValue = customerRequest.getWeightValue() == null ? null : customerRequest.getWeightValue().stripTrailingZeros().toPlainString();
        weightUnit = customerRequest.getWeightUnit();
        estimatedShippingDate = customerRequest.getEstimatedShippingDate();
        contactPerson = customerRequest.getContactPerson();
        contactNumber = customerRequest.getContactNumber();
        howYouKnowUs = customerRequest.getHowYouKnowUs();
    }

    protected void resetForm() {
        customerType = "INDIVIDUAL";
        customerName = null;
        originCountry = null;
        destinationCountry = null;
        finalDestinationDetails = null;
        capacityType = null;
        spaceSize = null;
        containerCount = null;
        weightValue = null;
        weightUnit = null;
        estimatedShippingDate = null;
        contactPerson = null;
        contactNumber = null;
        howYouKnowUs = null;
    }

    protected String generateRequestReference() {
        return "CRF-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    private boolean validateForm() {
        if (isBlank(customerType)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Customer type required",
                    "Please select Individual or Organization.");
            return false;
        }
        if (isBlank(customerName)) {
            addMessage(FacesMessage.SEVERITY_ERROR, getCustomerNameLabel() + " required",
                    "Please enter " + getCustomerNameLabel().toLowerCase() + ".");
            return false;
        }
        if (isBlank(originCountry) || isBlank(destinationCountry)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Route details required",
                    "Please select both origin and destination country.");
            return false;
        }
        if (isBlank(capacityType)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Requirement required",
                    "Please select space, container, or weight.");
            return false;
        }
        if (estimatedShippingDate == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Estimated shipping date required",
                    "Please select an estimated shipping date.");
            return false;
        }
        if (isBlank(contactPerson) || isBlank(contactNumber)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Contact details required",
                    "Please enter the contact person and contact number.");
            return false;
        }
        if (isSpaceSelected() && isBlank(spaceSize)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Space size required",
                    "Please enter the required space size.");
            return false;
        }
        if (isContainerSelected()) {
            if (isBlank(containerCount)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Container count required",
                        "Please enter the number of containers.");
                return false;
            }
            try {
                if (Integer.parseInt(containerCount.trim()) <= 0) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "Invalid container count",
                            "Container count must be greater than zero.");
                    return false;
                }
            } catch (NumberFormatException exception) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Invalid container count",
                        "Container count must be a valid whole number.");
                return false;
            }
        }
        if (isWeightSelected()) {
            if (isBlank(weightValue) || isBlank(weightUnit)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Weight details required",
                        "Please enter the weight and unit.");
                return false;
            }
            try {
                if (new BigDecimal(weightValue.trim()).compareTo(BigDecimal.ZERO) <= 0) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "Invalid weight",
                            "Weight must be greater than zero.");
                    return false;
                }
            } catch (NumberFormatException exception) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Invalid weight",
                        "Weight must be a valid number.");
                return false;
            }
        }
        return true;
    }

    public void onCustomerTypeChange() {
        customerName = null;
    }

    public void onCapacityTypeChange() {
        spaceSize = null;
        containerCount = null;
        weightValue = null;
        weightUnit = null;
    }

    public String getCustomerNameLabel() {
        return isOrganizationCustomer() ? "Organization Name" : "Customer Name";
    }

    public boolean isOrganizationCustomer() {
        return "ORGANIZATION".equals(customerType);
    }

    public boolean isSpaceSelected() {
        return "SPACE".equals(capacityType);
    }

    public boolean isContainerSelected() {
        return "CONTAINER".equals(capacityType);
    }

    public boolean isWeightSelected() {
        return "WEIGHT".equals(capacityType);
    }

    protected void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    protected String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    public List<String> getCountryNames() {
        return countryNames;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getFinalDestinationDetails() {
        return finalDestinationDetails;
    }

    public void setFinalDestinationDetails(String finalDestinationDetails) {
        this.finalDestinationDetails = finalDestinationDetails;
    }

    public String getCapacityType() {
        return capacityType;
    }

    public void setCapacityType(String capacityType) {
        this.capacityType = capacityType;
    }

    public String getSpaceSize() {
        return spaceSize;
    }

    public void setSpaceSize(String spaceSize) {
        this.spaceSize = spaceSize;
    }

    public String getContainerCount() {
        return containerCount;
    }

    public void setContainerCount(String containerCount) {
        this.containerCount = containerCount;
    }

    public String getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(String weightValue) {
        this.weightValue = weightValue;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public Date getEstimatedShippingDate() {
        return estimatedShippingDate;
    }

    public void setEstimatedShippingDate(Date estimatedShippingDate) {
        this.estimatedShippingDate = estimatedShippingDate;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getHowYouKnowUs() {
        return howYouKnowUs;
    }

    public void setHowYouKnowUs(String howYouKnowUs) {
        this.howYouKnowUs = howYouKnowUs;
    }

    public String getLastSubmittedReference() {
        return lastSubmittedReference;
    }

    public void setLastSubmittedReference(String lastSubmittedReference) {
        this.lastSubmittedReference = lastSubmittedReference;
    }
}
