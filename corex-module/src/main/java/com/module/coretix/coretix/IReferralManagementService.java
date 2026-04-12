package com.module.coretix.coretix;

import com.module.coretix.commonto.ReferralResolutionTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.OrganizationReferralProfile;
import com.persist.coretix.modal.coretix.ReferralAttribution;
import com.persist.coretix.modal.coretix.ReferralCommission;
import com.persist.coretix.modal.coretix.ReferrerProfile;

import java.math.BigDecimal;
import java.util.List;

public interface IReferralManagementService {

    GeneralConstants saveReferrerProfile(ReferrerProfile referrerProfile);

    GeneralConstants saveOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile);

    GeneralConstants deleteReferrerProfile(int referrerProfileId);

    GeneralConstants deleteOrganizationReferralProfile(int organizationReferralProfileId);

    ReferralResolutionTO resolveReferralCode(String referralCode);

    void createReferralAttribution(ReferralResolutionTO referralResolutionTO, int referredOrganizationId,
            String planCode, BigDecimal subscriptionAmount, String paymentGatewayCode, String paymentOrderId, String paymentId);

    ReferrerProfile getReferrerProfileByUserId(int userId);

    OrganizationReferralProfile getOrganizationReferralProfileByOrganizationId(int organizationId);

    List<ReferrerProfile> getReferrerProfileList();

    List<OrganizationReferralProfile> getOrganizationReferralProfileList();

    List<ReferralAttribution> getReferralAttributions();

    List<ReferralAttribution> getReferralAttributionsByReferrerProfileId(int referrerProfileId);

    List<ReferralAttribution> getReferralAttributionsByOrganizationReferralProfileId(int organizationReferralProfileId);

    List<ReferralCommission> getReferralCommissionsByReferrerProfileId(int referrerProfileId);
}
