package com.persist.shipx.quotation;

import com.persist.shipx.request.CustomerRequest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "shipx_quotation")
public class Quotation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "quotation_reference", nullable = false, unique = true, length = 40)
    private String quotationReference;

    @ManyToOne
    @JoinColumn(name = "customer_request_id")
    private CustomerRequest customerRequest;

    @Column(name = "request_reference_snapshot", length = 40)
    private String requestReferenceSnapshot;

    @Column(name = "quotation_title", nullable = false, length = 255)
    private String quotationTitle;

    @Column(name = "service_category", nullable = false, length = 60)
    private String serviceCategory;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "contact_number", length = 60)
    private String contactNumber;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "origin_location", length = 255)
    private String originLocation;

    @Column(name = "destination_location", length = 255)
    private String destinationLocation;

    @Column(name = "cargo_summary", length = 500)
    private String cargoSummary;

    @Column(name = "total_summary_label", length = 500)
    private String totalSummaryLabel;

    @Lob
    @Column(name = "pricing_breakdown_json", columnDefinition = "TEXT")
    private String pricingBreakdownJson;

    @Lob
    @Column(name = "note_lines_json", columnDefinition = "TEXT")
    private String noteLinesJson;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "valid_until", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date validUntil;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "email_subject", length = 255)
    private String emailSubject;

    @Lob
    @Column(name = "email_body", columnDefinition = "TEXT")
    private String emailBody;

    @Column(name = "sent_to_email", length = 255)
    private String sentToEmail;

    @Column(name = "sent_at")
    private Timestamp sentAt;

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

    public String getQuotationReference() {
        return quotationReference;
    }

    public void setQuotationReference(String quotationReference) {
        this.quotationReference = quotationReference;
    }

    public CustomerRequest getCustomerRequest() {
        return customerRequest;
    }

    public void setCustomerRequest(CustomerRequest customerRequest) {
        this.customerRequest = customerRequest;
    }

    public String getRequestReferenceSnapshot() {
        return requestReferenceSnapshot;
    }

    public void setRequestReferenceSnapshot(String requestReferenceSnapshot) {
        this.requestReferenceSnapshot = requestReferenceSnapshot;
    }

    public String getQuotationTitle() {
        return quotationTitle;
    }

    public void setQuotationTitle(String quotationTitle) {
        this.quotationTitle = quotationTitle;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getCargoSummary() {
        return cargoSummary;
    }

    public void setCargoSummary(String cargoSummary) {
        this.cargoSummary = cargoSummary;
    }

    public String getTotalSummaryLabel() {
        return totalSummaryLabel;
    }

    public void setTotalSummaryLabel(String totalSummaryLabel) {
        this.totalSummaryLabel = totalSummaryLabel;
    }

    public String getPricingBreakdownJson() {
        return pricingBreakdownJson;
    }

    public void setPricingBreakdownJson(String pricingBreakdownJson) {
        this.pricingBreakdownJson = pricingBreakdownJson;
    }

    public String getNoteLinesJson() {
        return noteLinesJson;
    }

    public void setNoteLinesJson(String noteLinesJson) {
        this.noteLinesJson = noteLinesJson;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public String getSentToEmail() {
        return sentToEmail;
    }

    public void setSentToEmail(String sentToEmail) {
        this.sentToEmail = sentToEmail;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
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

    public String getLinkedRequestReference() {
        if (customerRequest != null && customerRequest.getRequestReference() != null) {
            return customerRequest.getRequestReference();
        }
        return requestReferenceSnapshot;
    }
}
