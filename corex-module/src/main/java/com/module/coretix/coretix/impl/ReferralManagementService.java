package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.ReferralResolutionTO;
import com.module.coretix.coretix.IReferralManagementService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.OrganizationReferralProfile;
import com.persist.coretix.modal.coretix.ReferralAttribution;
import com.persist.coretix.modal.coretix.ReferralCommission;
import com.persist.coretix.modal.coretix.ReferrerProfile;
import com.persist.coretix.modal.coretix.dao.IReferralManagementDAO;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Named
@Transactional(readOnly = true)
public class ReferralManagementService implements IReferralManagementService {

    public static final String REFERRER_CATEGORY_APPLICATION_ADMIN = "APPLICATION_ADMIN";
    public static final String REFERRER_CATEGORY_APP_USER = "APP_USER";
    public static final String REFERRER_CATEGORY_GENERAL_USER = "GENERAL_USER";
    public static final String REFERRER_CATEGORY_EXTERNAL = "EXTERNAL_USER";
    private static final String REFERRAL_SOURCE_PERSONAL = "PERSONAL";
    private static final String REFERRAL_SOURCE_ORGANIZATION = "ORGANIZATION";
    private static final String BENEFIT_FREE_MONTH = "FREE_MONTH";
    private static final String BENEFIT_COMMISSION = "COMMISSION";
    private static final String COMMISSION_STATUS_PENDING = "PENDING";

    @Inject
    private IReferralManagementDAO referralManagementDAO;

    @Inject
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants saveReferrerProfile(ReferrerProfile referrerProfile) {
        ensureReferralCode(referrerProfile);
        if (referrerProfile.getId() > 0) {
            return referralManagementDAO.updateReferrerProfile(referrerProfile);
        }
        return referralManagementDAO.addReferrerProfile(referrerProfile);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants saveOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile) {
        ensureOrganizationReferralCode(organizationReferralProfile);
        if (organizationReferralProfile.getId() > 0) {
            return referralManagementDAO.updateOrganizationReferralProfile(organizationReferralProfile);
        }
        return referralManagementDAO.addOrganizationReferralProfile(organizationReferralProfile);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteReferrerProfile(int referrerProfileId) {
        return referralManagementDAO.deleteReferrerProfile(referrerProfileId);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteOrganizationReferralProfile(int organizationReferralProfileId) {
        return referralManagementDAO.deleteOrganizationReferralProfile(organizationReferralProfileId);
    }

    @Override
    public ReferralResolutionTO resolveReferralCode(String referralCode) {
        ReferralResolutionTO resolution = new ReferralResolutionTO();
        String normalizedCode = normalize(referralCode);
        if (normalizedCode.isEmpty()) {
            return resolution;
        }

        ReferrerProfile referrerProfile = referralManagementDAO.getReferrerProfileByCode(normalizedCode);
        if (referrerProfile != null && referrerProfile.isActive()) {
            resolution.setMatched(true);
            resolution.setReferralCode(referrerProfile.getReferralCode());
            resolution.setReferralSourceType(REFERRAL_SOURCE_PERSONAL);
            resolution.setReferrerProfileId(referrerProfile.getId());
            if (REFERRER_CATEGORY_EXTERNAL.equalsIgnoreCase(normalize(referrerProfile.getReferrerCategory()))) {
                resolution.setBenefitType(BENEFIT_COMMISSION);
                resolution.setCommissionPercentage(defaultCommissionPercentage(referrerProfile.getCommissionPercentage()));
            } else {
                resolution.setBenefitType(BENEFIT_FREE_MONTH);
                resolution.setExtraFreeMonths(1);
            }
            return resolution;
        }

        OrganizationReferralProfile organizationReferralProfile = referralManagementDAO.getOrganizationReferralProfileByCode(normalizedCode);
        if (organizationReferralProfile != null && organizationReferralProfile.isActive()) {
            resolution.setMatched(true);
            resolution.setReferralCode(organizationReferralProfile.getReferralCode());
            resolution.setReferralSourceType(REFERRAL_SOURCE_ORGANIZATION);
            resolution.setOrganizationReferralProfileId(organizationReferralProfile.getId());
            resolution.setBenefitType(BENEFIT_FREE_MONTH);
            resolution.setExtraFreeMonths(1);
        }
        return resolution;
    }

    @Override
    @Transactional(readOnly = false)
    public void createReferralAttribution(ReferralResolutionTO referralResolutionTO, int referredOrganizationId,
            String planCode, BigDecimal subscriptionAmount, String paymentGatewayCode, String paymentOrderId, String paymentId) {
        if (referralResolutionTO == null || !referralResolutionTO.isMatched()) {
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Organizations referredOrganization = sessionFactory.getCurrentSession().get(Organizations.class, referredOrganizationId);

        ReferralAttribution attribution = new ReferralAttribution();
        attribution.setReferredOrganization(referredOrganization);
        attribution.setReferralCode(referralResolutionTO.getReferralCode());
        attribution.setReferralSourceType(referralResolutionTO.getReferralSourceType());
        attribution.setBenefitType(referralResolutionTO.getBenefitType());
        attribution.setPlanCode(planCode);
        attribution.setSubscriptionAmount(subscriptionAmount);
        attribution.setFreeMonthsAwarded(referralResolutionTO.getExtraFreeMonths());
        attribution.setPaymentGatewayCode(paymentGatewayCode);
        attribution.setPaymentOrderId(paymentOrderId);
        attribution.setPaymentId(paymentId);
        attribution.setCreatedAt(now);
        attribution.setUpdatedAt(now);

        if (referralResolutionTO.getReferrerProfileId() != null) {
            attribution.setReferrerProfile(referralManagementDAO.getReferrerProfile(referralResolutionTO.getReferrerProfileId()));
        }
        if (referralResolutionTO.getOrganizationReferralProfileId() != null) {
            attribution.setOrganizationReferralProfile(
                    referralManagementDAO.getOrganizationReferralProfile(referralResolutionTO.getOrganizationReferralProfileId()));
        }

        referralManagementDAO.addReferralAttribution(attribution);

        if (BENEFIT_COMMISSION.equalsIgnoreCase(referralResolutionTO.getBenefitType()) && attribution.getReferrerProfile() != null) {
            ReferralCommission commission = new ReferralCommission();
            commission.setReferralAttribution(attribution);
            commission.setReferrerProfile(attribution.getReferrerProfile());
            commission.setCommissionAmount(calculateCommissionAmount(subscriptionAmount, referralResolutionTO.getCommissionPercentage()));
            commission.setCommissionStatus(COMMISSION_STATUS_PENDING);
            commission.setCreatedAt(now);
            referralManagementDAO.addReferralCommission(commission);
        }
    }

    @Override
    public ReferrerProfile getReferrerProfileByUserId(int userId) {
        return referralManagementDAO.getReferrerProfileByUserId(userId);
    }

    @Override
    public OrganizationReferralProfile getOrganizationReferralProfileByOrganizationId(int organizationId) {
        return referralManagementDAO.getOrganizationReferralProfileByOrganizationId(organizationId);
    }

    @Override
    public List<ReferrerProfile> getReferrerProfileList() {
        return referralManagementDAO.getReferrerProfileList();
    }

    @Override
    public List<OrganizationReferralProfile> getOrganizationReferralProfileList() {
        return referralManagementDAO.getOrganizationReferralProfileList();
    }

    @Override
    public List<ReferralAttribution> getReferralAttributions() {
        return referralManagementDAO.getReferralAttributions();
    }

    @Override
    public List<ReferralAttribution> getReferralAttributionsByReferrerProfileId(int referrerProfileId) {
        return referralManagementDAO.getReferralAttributionsByReferrerProfileId(referrerProfileId);
    }

    @Override
    public List<ReferralAttribution> getReferralAttributionsByOrganizationReferralProfileId(int organizationReferralProfileId) {
        return referralManagementDAO.getReferralAttributionsByOrganizationReferralProfileId(organizationReferralProfileId);
    }

    @Override
    public List<ReferralCommission> getReferralCommissionsByReferrerProfileId(int referrerProfileId) {
        return referralManagementDAO.getReferralCommissionsByReferrerProfileId(referrerProfileId);
    }

    private void ensureReferralCode(ReferrerProfile referrerProfile) {
        if (normalize(referrerProfile.getReferralCode()).isEmpty()) {
            referrerProfile.setReferralCode(generateCode("REF"));
        }
        if (normalize(referrerProfile.getReferrerCategory()).isEmpty()) {
            referrerProfile.setReferrerCategory(REFERRER_CATEGORY_GENERAL_USER);
        }
        if (!REFERRER_CATEGORY_EXTERNAL.equalsIgnoreCase(normalize(referrerProfile.getReferrerCategory()))) {
            referrerProfile.setCommissionPercentage(null);
        }
    }

    private void ensureOrganizationReferralCode(OrganizationReferralProfile organizationReferralProfile) {
        if (normalize(organizationReferralProfile.getReferralCode()).isEmpty()) {
            organizationReferralProfile.setReferralCode(generateCode("ORG"));
        }
    }

    private String generateCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ENGLISH);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal defaultCommissionPercentage(BigDecimal commissionPercentage) {
        return commissionPercentage == null ? new BigDecimal("10.00") : commissionPercentage;
    }

    private BigDecimal calculateCommissionAmount(BigDecimal subscriptionAmount, BigDecimal commissionPercentage) {
        if (subscriptionAmount == null || commissionPercentage == null) {
            return BigDecimal.ZERO;
        }
        return subscriptionAmount.multiply(commissionPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
