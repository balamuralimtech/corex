package com.persist.coretix.modal.coretix;

import com.persist.coretix.modal.systemmanagement.Organizations;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "referral_attribution")
public class ReferralAttribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_profile_id")
    private ReferrerProfile referrerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_referral_profile_id")
    private OrganizationReferralProfile organizationReferralProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_organization_id", nullable = false)
    private Organizations referredOrganization;

    @Column(name = "referral_code", nullable = false, length = 40)
    private String referralCode;

    @Column(name = "referral_source_type", nullable = false, length = 40)
    private String referralSourceType;

    @Column(name = "benefit_type", nullable = false, length = 40)
    private String benefitType;

    @Column(name = "plan_code", nullable = false, length = 40)
    private String planCode;

    @Column(name = "subscription_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subscriptionAmount;

    @Column(name = "free_months_awarded", nullable = false)
    private int freeMonthsAwarded;

    @Column(name = "payment_gateway_code", length = 40)
    private String paymentGatewayCode;

    @Column(name = "payment_order_id", length = 120)
    private String paymentOrderId;

    @Column(name = "payment_id", length = 120)
    private String paymentId;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ReferrerProfile getReferrerProfile() {
        return referrerProfile;
    }

    public void setReferrerProfile(ReferrerProfile referrerProfile) {
        this.referrerProfile = referrerProfile;
    }

    public OrganizationReferralProfile getOrganizationReferralProfile() {
        return organizationReferralProfile;
    }

    public void setOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile) {
        this.organizationReferralProfile = organizationReferralProfile;
    }

    public Organizations getReferredOrganization() {
        return referredOrganization;
    }

    public void setReferredOrganization(Organizations referredOrganization) {
        this.referredOrganization = referredOrganization;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getReferralSourceType() {
        return referralSourceType;
    }

    public void setReferralSourceType(String referralSourceType) {
        this.referralSourceType = referralSourceType;
    }

    public String getBenefitType() {
        return benefitType;
    }

    public void setBenefitType(String benefitType) {
        this.benefitType = benefitType;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public BigDecimal getSubscriptionAmount() {
        return subscriptionAmount;
    }

    public void setSubscriptionAmount(BigDecimal subscriptionAmount) {
        this.subscriptionAmount = subscriptionAmount;
    }

    public int getFreeMonthsAwarded() {
        return freeMonthsAwarded;
    }

    public void setFreeMonthsAwarded(int freeMonthsAwarded) {
        this.freeMonthsAwarded = freeMonthsAwarded;
    }

    public String getPaymentGatewayCode() {
        return paymentGatewayCode;
    }

    public void setPaymentGatewayCode(String paymentGatewayCode) {
        this.paymentGatewayCode = paymentGatewayCode;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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
}
