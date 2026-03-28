package com.persist.shipx.request;

import com.persist.coretix.modal.systemmanagement.Countries;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "shipx_customer_request")
public class CustomerRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_reference", nullable = false, unique = true, length = 40)
    private String requestReference;

    @Column(name = "customer_type", nullable = false, length = 20)
    private String customerType;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @ManyToOne
    @JoinColumn(name = "origin_country_id", nullable = false)
    private Countries originCountry;

    @ManyToOne
    @JoinColumn(name = "destination_country_id", nullable = false)
    private Countries destinationCountry;

    @Column(name = "final_destination_details", length = 500)
    private String finalDestinationDetails;

    @Column(name = "capacity_type", nullable = false, length = 20)
    private String capacityType;

    @Column(name = "space_size", length = 120)
    private String spaceSize;

    @Column(name = "container_count")
    private Integer containerCount;

    @Column(name = "weight_value", precision = 12, scale = 3)
    private BigDecimal weightValue;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit;

    @Column(name = "estimated_shipping_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date estimatedShippingDate;

    @Column(name = "contact_person", nullable = false, length = 255)
    private String contactPerson;

    @Column(name = "contact_number", nullable = false, length = 60)
    private String contactNumber;

    @Column(name = "how_you_know_us", length = 255)
    private String howYouKnowUs;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_by_user_id")
    private Integer createdByUserId;

    @Column(name = "created_by_user_name", length = 100)
    private String createdByUserName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(nullable = false)
    private boolean flag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
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

    public Countries getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(Countries originCountry) {
        this.originCountry = originCountry;
    }

    public Countries getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(Countries destinationCountry) {
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

    public Integer getContainerCount() {
        return containerCount;
    }

    public void setContainerCount(Integer containerCount) {
        this.containerCount = containerCount;
    }

    public BigDecimal getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(BigDecimal weightValue) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getRouteSummary() {
        String origin = originCountry == null ? "" : originCountry.getName();
        String destination = destinationCountry == null ? "" : destinationCountry.getName();
        if (origin.isEmpty() && destination.isEmpty()) {
            return "";
        }
        return origin + " -> " + destination;
    }

    public String getCapacitySummary() {
        if (capacityType == null) {
            return "";
        }

        switch (capacityType) {
            case "SPACE":
                return "Space - " + spaceSize;
            case "CONTAINER":
                return "Container - " + containerCount;
            case "WEIGHT":
                return "Weight - " + weightValue + " " + weightUnit;
            default:
                return capacityType;
        }
    }
}
