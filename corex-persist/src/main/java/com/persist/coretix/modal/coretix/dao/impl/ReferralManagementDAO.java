package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.OrganizationReferralProfile;
import com.persist.coretix.modal.coretix.ReferralAttribution;
import com.persist.coretix.modal.coretix.ReferralCommission;
import com.persist.coretix.modal.coretix.ReferrerProfile;
import com.persist.coretix.modal.coretix.dao.IReferralManagementDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Named
public class ReferralManagementDAO implements IReferralManagementDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReferralManagementDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public GeneralConstants addReferrerProfile(ReferrerProfile referrerProfile) {
        Session session = sessionFactory.getCurrentSession();
        try {
            if (findReferrerByCode(session, referrerProfile.getReferralCode()) != null) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            referrerProfile.setCreatedAt(now);
            referrerProfile.setUpdatedAt(now);
            session.save(referrerProfile);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to add referrer profile", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants updateReferrerProfile(ReferrerProfile referrerProfile) {
        Session session = sessionFactory.getCurrentSession();
        try {
            ReferrerProfile existing = session.get(ReferrerProfile.class, referrerProfile.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            ReferrerProfile duplicate = findReferrerByCode(session, referrerProfile.getReferralCode());
            if (duplicate != null && duplicate.getId() != referrerProfile.getId()) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            referrerProfile.setCreatedAt(existing.getCreatedAt());
            referrerProfile.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            session.merge(referrerProfile);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to update referrer profile", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants deleteReferrerProfile(int referrerProfileId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            ReferrerProfile existing = session.get(ReferrerProfile.class, referrerProfileId);
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long attributionCount = session.createQuery(
                            "select count(ra.id) from ReferralAttribution ra where ra.referrerProfile.id = :referrerProfileId",
                            Long.class)
                    .setParameter("referrerProfileId", referrerProfileId)
                    .uniqueResult();
            if (attributionCount != null && attributionCount > 0) {
                return GeneralConstants.ENTRY_IN_USE;
            }

            Long commissionCount = session.createQuery(
                            "select count(rc.id) from ReferralCommission rc where rc.referrerProfile.id = :referrerProfileId",
                            Long.class)
                    .setParameter("referrerProfileId", referrerProfileId)
                    .uniqueResult();
            if (commissionCount != null && commissionCount > 0) {
                return GeneralConstants.ENTRY_IN_USE;
            }

            session.remove(existing);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to delete referrer profile", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants addOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile) {
        Session session = sessionFactory.getCurrentSession();
        try {
            if (findOrganizationReferrerByCode(session, organizationReferralProfile.getReferralCode()) != null) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            organizationReferralProfile.setCreatedAt(now);
            organizationReferralProfile.setUpdatedAt(now);
            session.save(organizationReferralProfile);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to add organization referral profile", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants updateOrganizationReferralProfile(OrganizationReferralProfile organizationReferralProfile) {
        Session session = sessionFactory.getCurrentSession();
        try {
            OrganizationReferralProfile existing = session.get(OrganizationReferralProfile.class, organizationReferralProfile.getId());
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            OrganizationReferralProfile duplicate = findOrganizationReferrerByCode(session, organizationReferralProfile.getReferralCode());
            if (duplicate != null && duplicate.getId() != organizationReferralProfile.getId()) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }
            organizationReferralProfile.setCreatedAt(existing.getCreatedAt());
            organizationReferralProfile.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            session.merge(organizationReferralProfile);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to update organization referral profile", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public GeneralConstants deleteOrganizationReferralProfile(int organizationReferralProfileId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            OrganizationReferralProfile existing = session.get(OrganizationReferralProfile.class, organizationReferralProfileId);
            if (existing == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long attributionCount = session.createQuery(
                            "select count(ra.id) from ReferralAttribution ra where ra.organizationReferralProfile.id = :organizationReferralProfileId",
                            Long.class)
                    .setParameter("organizationReferralProfileId", organizationReferralProfileId)
                    .uniqueResult();
            if (attributionCount != null && attributionCount > 0) {
                return GeneralConstants.ENTRY_IN_USE;
            }

            session.remove(existing);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception exception) {
            logger.error("Unable to delete organization referral profile", exception);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public ReferrerProfile getReferrerProfile(int id) {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.get(ReferrerProfile.class, id);
        } catch (Exception exception) {
            logger.warn("Unable to fetch referrer profile. Referral tables may not be installed yet.", exception);
            session.clear();
            return null;
        }
    }

    @Override
    public ReferrerProfile getReferrerProfileByUserId(int userId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            List<ReferrerProfile> list = session.createQuery(
                            "from ReferrerProfile where userDetails.userId = :userId", ReferrerProfile.class)
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .list();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception exception) {
            logger.warn("Unable to fetch referrer profile by user. Referral tables may not be installed yet.", exception);
            session.clear();
            return null;
        }
    }

    @Override
    public ReferrerProfile getReferrerProfileByCode(String referralCode) {
        return findReferrerByCode(sessionFactory.getCurrentSession(), referralCode);
    }

    @Override
    public OrganizationReferralProfile getOrganizationReferralProfile(int id) {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.get(OrganizationReferralProfile.class, id);
        } catch (Exception exception) {
            logger.warn("Unable to fetch organization referral profile. Referral tables may not be installed yet.", exception);
            session.clear();
            return null;
        }
    }

    @Override
    public OrganizationReferralProfile getOrganizationReferralProfileByOrganizationId(int organizationId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            List<OrganizationReferralProfile> list = session.createQuery(
                            "from OrganizationReferralProfile where organization.id = :organizationId", OrganizationReferralProfile.class)
                    .setParameter("organizationId", organizationId)
                    .setMaxResults(1)
                    .list();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception exception) {
            logger.warn("Unable to fetch organization referral profile by organization. Referral tables may not be installed yet.", exception);
            session.clear();
            return null;
        }
    }

    @Override
    public OrganizationReferralProfile getOrganizationReferralProfileByCode(String referralCode) {
        return findOrganizationReferrerByCode(sessionFactory.getCurrentSession(), referralCode);
    }

    @Override
    public List<ReferrerProfile> getReferrerProfileList() {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "select distinct rp from ReferrerProfile rp left join fetch rp.userDetails order by lower(rp.referrerName)",
                            ReferrerProfile.class)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch referrer profiles. Referral tables may not be installed yet.", exception);
            session.clear();
            return Collections.emptyList();
        }
    }

    @Override
    public List<OrganizationReferralProfile> getOrganizationReferralProfileList() {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "select distinct op from OrganizationReferralProfile op left join fetch op.organization order by lower(op.organization.organizationName)",
                            OrganizationReferralProfile.class)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch organization referral profiles. Referral tables may not be installed yet.", exception);
            session.clear();
            return Collections.emptyList();
        }
    }

    @Override
    public void addReferralAttribution(ReferralAttribution referralAttribution) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.save(referralAttribution);
        } catch (Exception exception) {
            logger.error("Unable to save referral attribution", exception);
            session.clear();
        }
    }

    @Override
    public void addReferralCommission(ReferralCommission referralCommission) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.save(referralCommission);
        } catch (Exception exception) {
            logger.error("Unable to save referral commission", exception);
            session.clear();
        }
    }

    @Override
    public List<ReferralAttribution> getReferralAttributions() {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "select distinct ra from ReferralAttribution ra "
                                    + "left join fetch ra.referrerProfile rp "
                                    + "left join fetch rp.userDetails "
                                    + "left join fetch ra.organizationReferralProfile op "
                                    + "left join fetch op.organization "
                                    + "left join fetch ra.referredOrganization "
                                    + "order by ra.createdAt desc",
                            ReferralAttribution.class)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch referral attributions. Referral tables may not be installed yet.", exception);
            session.clear();
            return Collections.emptyList();
        }
    }

    @Override
    public List<ReferralAttribution> getReferralAttributionsByReferrerProfileId(int referrerProfileId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "select distinct ra from ReferralAttribution ra "
                                    + "left join fetch ra.referredOrganization "
                                    + "where ra.referrerProfile.id = :referrerProfileId order by ra.createdAt desc",
                            ReferralAttribution.class)
                    .setParameter("referrerProfileId", referrerProfileId)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch referral attributions by referrer. Referral tables may not be installed yet.", exception);
            session.clear();
            return Collections.emptyList();
        }
    }

    @Override
    public List<ReferralAttribution> getReferralAttributionsByOrganizationReferralProfileId(int organizationReferralProfileId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "select distinct ra from ReferralAttribution ra "
                                    + "left join fetch ra.referredOrganization "
                                    + "where ra.organizationReferralProfile.id = :organizationReferralProfileId order by ra.createdAt desc",
                            ReferralAttribution.class)
                    .setParameter("organizationReferralProfileId", organizationReferralProfileId)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch referral attributions by organization referral profile. Referral tables may not be installed yet.", exception);
            session.clear();
            return Collections.emptyList();
        }
    }

    @Override
    public List<ReferralCommission> getReferralCommissionsByReferrerProfileId(int referrerProfileId) {
        Session session = sessionFactory.getCurrentSession();
        try {
            return session.createQuery(
                            "select distinct rc from ReferralCommission rc "
                                    + "left join fetch rc.referralAttribution ra "
                                    + "left join fetch ra.referredOrganization "
                                    + "where rc.referrerProfile.id = :referrerProfileId order by rc.createdAt desc",
                            ReferralCommission.class)
                    .setParameter("referrerProfileId", referrerProfileId)
                    .list();
        } catch (Exception exception) {
            logger.warn("Unable to fetch referral commissions. Referral tables may not be installed yet.", exception);
            session.clear();
            return Collections.emptyList();
        }
    }

    private ReferrerProfile findReferrerByCode(Session session, String referralCode) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return null;
        }
        List<ReferrerProfile> list = session.createQuery(
                        "from ReferrerProfile where lower(referralCode) = :referralCode", ReferrerProfile.class)
                .setParameter("referralCode", referralCode.trim().toLowerCase())
                .setMaxResults(1)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }

    private OrganizationReferralProfile findOrganizationReferrerByCode(Session session, String referralCode) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return null;
        }
        List<OrganizationReferralProfile> list = session.createQuery(
                        "from OrganizationReferralProfile where lower(referralCode) = :referralCode", OrganizationReferralProfile.class)
                .setParameter("referralCode", referralCode.trim().toLowerCase())
                .setMaxResults(1)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }
}
