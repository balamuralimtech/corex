package com.persist.coretix.modal.coretix;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "referral_commission")
public class ReferralCommission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_attribution_id", nullable = false)
    private ReferralAttribution referralAttribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_profile_id", nullable = false)
    private ReferrerProfile referrerProfile;

    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "commission_status", nullable = false, length = 40)
    private String commissionStatus;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "paid_at")
    private Timestamp paidAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ReferralAttribution getReferralAttribution() {
        return referralAttribution;
    }

    public void setReferralAttribution(ReferralAttribution referralAttribution) {
        this.referralAttribution = referralAttribution;
    }

    public ReferrerProfile getReferrerProfile() {
        return referrerProfile;
    }

    public void setReferrerProfile(ReferrerProfile referrerProfile) {
        this.referrerProfile = referrerProfile;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public String getCommissionStatus() {
        return commissionStatus;
    }

    public void setCommissionStatus(String commissionStatus) {
        this.commissionStatus = commissionStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }
}
