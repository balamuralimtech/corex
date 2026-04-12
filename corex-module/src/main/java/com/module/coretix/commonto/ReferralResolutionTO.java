package com.module.coretix.commonto;

import java.math.BigDecimal;

public class ReferralResolutionTO {

    private boolean matched;
    private String referralCode;
    private String referralSourceType;
    private String benefitType;
    private Integer referrerProfileId;
    private Integer organizationReferralProfileId;
    private int extraFreeMonths;
    private BigDecimal commissionPercentage;

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
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

    public Integer getReferrerProfileId() {
        return referrerProfileId;
    }

    public void setReferrerProfileId(Integer referrerProfileId) {
        this.referrerProfileId = referrerProfileId;
    }

    public Integer getOrganizationReferralProfileId() {
        return organizationReferralProfileId;
    }

    public void setOrganizationReferralProfileId(Integer organizationReferralProfileId) {
        this.organizationReferralProfileId = organizationReferralProfileId;
    }

    public int getExtraFreeMonths() {
        return extraFreeMonths;
    }

    public void setExtraFreeMonths(int extraFreeMonths) {
        this.extraFreeMonths = extraFreeMonths;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }
}
