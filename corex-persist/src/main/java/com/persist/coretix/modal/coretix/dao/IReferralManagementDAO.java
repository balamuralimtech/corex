package com.persist.coretix.modal.coretix.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.OrganizationReferralProfile;
import com.persist.coretix.modal.coretix.ReferralAttribution;
import com.persist.coretix.modal.coretix.ReferralCommission;
import com.persist.coretix.modal.coretix.ReferrerProfile;

import java.util.List;

public interface IReferralManagementDAO {

    GeneralConstants addReferrerProfile(ReferrerProfile referrerProfile);

    GeneralConstants updateReferrerProfile(ReferrerProfile referrerProfile);

    GeneralConstants deleteReferrerProfile(int referrerProfileId);

    GeneralConstants addOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile);

    GeneralConstants updateOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile);

    GeneralConstants deleteOrganizationReferralProfile(int organizationReferralProfileId);

    ReferrerProfile getReferrerProfile(int id);

    ReferrerProfile getReferrerProfileByUserId(int userId);

    ReferrerProfile getReferrerProfileByCode(String referralCode);

    OrganizationReferralProfile getOrganizationReferralProfile(int id);

    OrganizationReferralProfile getOrganizationReferralProfileByOrganizationId(int organizationId);

    OrganizationReferralProfile getOrganizationReferralProfileByCode(String referralCode);

    List<ReferrerProfile> getReferrerProfileList();

    List<OrganizationReferralProfile> getOrganizationReferralProfileList();

    void addReferralAttribution(ReferralAttribution referralAttribution);

    void addReferralCommission(ReferralCommission referralCommission);

    List<ReferralAttribution> getReferralAttributions();

    List<ReferralAttribution> getReferralAttributionsByReferrerProfileId(int referrerProfileId);

    List<ReferralAttribution> getReferralAttributionsByOrganizationReferralProfileId(int organizationReferralProfileId);

    List<ReferralCommission> getReferralCommissionsByReferrerProfileId(int referrerProfileId);
}
