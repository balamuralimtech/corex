package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IReferralManagementService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.coretix.modal.coretix.OrganizationReferralProfile;
import com.persist.coretix.modal.coretix.ReferralAttribution;
import com.persist.coretix.modal.coretix.ReferralCommission;
import com.persist.coretix.modal.coretix.ReferrerProfile;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.coretix.appgeneral.GenericManagedBean;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Named("referralDashboardBean")
@Scope("session")
public class ReferralDashboardBean extends GenericManagedBean implements Serializable {

    private static final SimpleDateFormat DASHBOARD_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
    private static final Integer ALL_ORGANIZATIONS_ID = -1;

    @Inject
    private transient IReferralManagementService referralManagementService;

    @Inject
    private transient IOrganizationService organizationService;

    private List<ReferralAttribution> referralAttributions = new ArrayList<>();
    private List<ReferralCommission> referralCommissions = new ArrayList<>();
    private List<BreakdownRow> planBreakdown = new ArrayList<>();
    private List<BreakdownRow> gatewayBreakdown = new ArrayList<>();
    private List<BreakdownRow> commissionStatusBreakdown = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();

    private String referralCode = "";
    private String benefitType = "";
    private String referralOwnerName = "";
    private String ownerType = "";
    private String headline = "";
    private String latestReferralLabel = "-";
    private String topPlanLabel = "-";
    private Integer selectedOrganizationId;
    private int activeCodeCount;

    private int totalReferrals;
    private int totalFreeMonthsAwarded;
    private int pendingCommissionCount;
    private int paidCommissionCount;

    private BigDecimal totalCommission = BigDecimal.ZERO;
    private BigDecimal pendingCommission = BigDecimal.ZERO;
    private BigDecimal paidCommission = BigDecimal.ZERO;
    private BigDecimal totalSubscriptionAmount = BigDecimal.ZERO;
    private BigDecimal averageSubscriptionAmount = BigDecimal.ZERO;

    public void initializePageAttributes() {
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().isPostback()) {
            return;
        }

        resetDashboard();
        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        selectedOrganizationId = resolveDashboardOrganizationId(selectedOrganizationId);
        loadDashboardData();
    }

    public void refreshDashboard() {
        resetDashboard();
        if (organizationList == null || organizationList.isEmpty()) {
            organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        }
        selectedOrganizationId = resolveDashboardOrganizationId(selectedOrganizationId);
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (isApplicationAdmin()) {
            loadAdminDashboard();
            return;
        }
        loadOrganizationDashboard();
    }

    private void resetDashboard() {
        referralAttributions = new ArrayList<>();
        referralCommissions = new ArrayList<>();
        planBreakdown = new ArrayList<>();
        gatewayBreakdown = new ArrayList<>();
        commissionStatusBreakdown = new ArrayList<>();
        referralCode = "";
        benefitType = "";
        referralOwnerName = "";
        ownerType = "";
        headline = "";
        latestReferralLabel = "-";
        topPlanLabel = "-";
        activeCodeCount = 0;
        totalReferrals = 0;
        totalFreeMonthsAwarded = 0;
        pendingCommissionCount = 0;
        paidCommissionCount = 0;
        totalCommission = BigDecimal.ZERO;
        pendingCommission = BigDecimal.ZERO;
        paidCommission = BigDecimal.ZERO;
        totalSubscriptionAmount = BigDecimal.ZERO;
        averageSubscriptionAmount = BigDecimal.ZERO;
    }

    private void loadAdminDashboard() {
        List<ReferralAttribution> allAttributions = new ArrayList<>(referralManagementService.getReferralAttributions());
        List<ReferralCommission> allCommissions = new ArrayList<>();
        List<ReferrerProfile> allProfiles = new ArrayList<>(referralManagementService.getReferrerProfileList());
        List<OrganizationReferralProfile> allOrganizationProfiles =
                new ArrayList<>(referralManagementService.getOrganizationReferralProfileList());

        for (ReferrerProfile referrerProfile : allProfiles) {
            allCommissions.addAll(referralManagementService.getReferralCommissionsByReferrerProfileId(referrerProfile.getId()));
        }

        if (!isAllOrganizationsSelected() && selectedOrganizationId != null) {
            allAttributions = allAttributions.stream()
                    .filter(item -> item.getReferredOrganization() != null
                            && item.getReferredOrganization().getId() == selectedOrganizationId)
                    .collect(Collectors.toList());

            allCommissions = allCommissions.stream()
                    .filter(item -> item.getReferralAttribution() != null
                            && item.getReferralAttribution().getReferredOrganization() != null
                            && item.getReferralAttribution().getReferredOrganization().getId() == selectedOrganizationId)
                    .collect(Collectors.toList());
        }

        referralAttributions = allAttributions;
        referralCommissions = deduplicateCommissions(allCommissions);

        String selectedOrganizationName = getSelectedOrganizationName();
        referralOwnerName = isAllOrganizationsSelected() ? "All Organizations" : selectedOrganizationName;
        ownerType = "Admin Referral Overview";
        referralCode = isAllOrganizationsSelected() ? "MULTI-CODE VIEW" : selectedOrganizationName.toUpperCase(Locale.ENGLISH);
        benefitType = "Commission + Free Month";

        activeCodeCount = isAllOrganizationsSelected()
                ? allProfiles.size() + allOrganizationProfiles.size()
                : countOrganizationCodes(selectedOrganizationId, allProfiles, allOrganizationProfiles);
        summarize();
    }

    private void loadOrganizationDashboard() {
        Integer organizationId = fetchCurrentOrganizationId();
        if (organizationId == null) {
            return;
        }

        List<ReferrerProfile> organizationUserProfiles = referralManagementService.getReferrerProfileList().stream()
                .filter(profile -> profile.getUserDetails() != null
                        && profile.getUserDetails().getOrganization() != null
                        && profile.getUserDetails().getOrganization().getId() == organizationId)
                .collect(Collectors.toList());

        OrganizationReferralProfile organizationReferralProfile =
                referralManagementService.getOrganizationReferralProfileByOrganizationId(organizationId);

        for (ReferrerProfile referrerProfile : organizationUserProfiles) {
            referralAttributions.addAll(referralManagementService.getReferralAttributionsByReferrerProfileId(referrerProfile.getId()));
            referralCommissions.addAll(referralManagementService.getReferralCommissionsByReferrerProfileId(referrerProfile.getId()));
        }

        if (organizationReferralProfile != null) {
            referralAttributions.addAll(
                    referralManagementService.getReferralAttributionsByOrganizationReferralProfileId(organizationReferralProfile.getId()));
        }

        referralCommissions = deduplicateCommissions(referralCommissions);
        referralAttributions = deduplicateAttributions(referralAttributions);

        referralOwnerName = organizationReferralProfile != null && organizationReferralProfile.getOrganization() != null
                ? safe(organizationReferralProfile.getOrganization().getOrganizationName())
                : getCurrentOrganizationName();
        ownerType = "Organization Referral Dashboard";
        referralCode = organizationReferralProfile != null ? safe(organizationReferralProfile.getReferralCode()) : "MULTI-CODE VIEW";
        benefitType = referralCommissions.isEmpty() ? "Free Month" : "Commission + Free Month";
        activeCodeCount = organizationUserProfiles.size() + (organizationReferralProfile == null ? 0 : 1);
        summarize();
    }

    private void summarize() {
        totalReferrals = referralAttributions.size();

        Timestamp latestTimestamp = null;
        Map<String, Integer> planCountMap = new LinkedHashMap<>();
        Map<String, BigDecimal> planAmountMap = new LinkedHashMap<>();
        Map<String, Integer> gatewayCountMap = new LinkedHashMap<>();
        Map<String, BigDecimal> gatewayAmountMap = new LinkedHashMap<>();

        for (ReferralAttribution attribution : referralAttributions) {
            totalFreeMonthsAwarded += attribution.getFreeMonthsAwarded();
            BigDecimal subscriptionAmount = attribution.getSubscriptionAmount() == null ? BigDecimal.ZERO : attribution.getSubscriptionAmount();
            totalSubscriptionAmount = totalSubscriptionAmount.add(subscriptionAmount);

            String planKey = prettifyCode(attribution.getPlanCode());
            planCountMap.merge(planKey, 1, Integer::sum);
            planAmountMap.merge(planKey, subscriptionAmount, BigDecimal::add);

            String gatewayKey = prettifyCode(attribution.getPaymentGatewayCode());
            gatewayCountMap.merge(gatewayKey, 1, Integer::sum);
            gatewayAmountMap.merge(gatewayKey, subscriptionAmount, BigDecimal::add);

            if (latestTimestamp == null || (attribution.getCreatedAt() != null && attribution.getCreatedAt().after(latestTimestamp))) {
                latestTimestamp = attribution.getCreatedAt();
            }
        }

        for (ReferralCommission referralCommission : referralCommissions) {
            BigDecimal amount = referralCommission.getCommissionAmount() == null ? BigDecimal.ZERO : referralCommission.getCommissionAmount();
            totalCommission = totalCommission.add(amount);

            String statusKey = prettifyCode(referralCommission.getCommissionStatus());
            if ("Paid".equalsIgnoreCase(statusKey)) {
                paidCommission = paidCommission.add(amount);
                paidCommissionCount++;
            } else {
                pendingCommission = pendingCommission.add(amount);
                pendingCommissionCount++;
            }
        }

        if (totalReferrals > 0) {
            averageSubscriptionAmount = totalSubscriptionAmount.divide(new BigDecimal(totalReferrals), 2, RoundingMode.HALF_UP);
        }

        planBreakdown = toBreakdownRows(planCountMap, planAmountMap, totalReferrals);
        gatewayBreakdown = toBreakdownRows(gatewayCountMap, gatewayAmountMap, totalReferrals);

        Map<String, Integer> commissionCountMap = new LinkedHashMap<>();
        Map<String, BigDecimal> commissionAmountMap = new LinkedHashMap<>();
        if (!referralCommissions.isEmpty()) {
            for (ReferralCommission referralCommission : referralCommissions) {
                String statusKey = prettifyCode(referralCommission.getCommissionStatus());
                commissionCountMap.merge(statusKey, 1, Integer::sum);
                commissionAmountMap.merge(statusKey,
                        referralCommission.getCommissionAmount() == null ? BigDecimal.ZERO : referralCommission.getCommissionAmount(),
                        BigDecimal::add);
            }
        } else {
            commissionCountMap.put("Free Month Benefit", totalReferrals);
            commissionAmountMap.put("Free Month Benefit", BigDecimal.ZERO);
        }
        commissionStatusBreakdown = toBreakdownRows(commissionCountMap, commissionAmountMap,
                Math.max(totalReferrals, Math.max(1, referralCommissions.size())));

        topPlanLabel = planBreakdown.isEmpty() ? "-" : planBreakdown.get(0).getLabel();
        latestReferralLabel = latestTimestamp == null ? "-" : DASHBOARD_DATE_FORMAT.format(new Date(latestTimestamp.getTime()));
        headline = buildHeadline();
    }

    private String buildHeadline() {
        if (totalReferrals <= 0) {
            return "Your referral code is active and ready to track the next conversion.";
        }

        if (isCommissionMode()) {
            return totalReferrals + " clinics activated under your code, with "
                    + formatCurrency(totalCommission) + " earned across the current commission ledger.";
        }

        return totalReferrals + " clinics activated through your code, delivering "
                + totalFreeMonthsAwarded + " free month benefits so far.";
    }

    private List<BreakdownRow> toBreakdownRows(Map<String, Integer> countMap, Map<String, BigDecimal> amountMap, int totalBase) {
        List<BreakdownRow> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            BigDecimal amount = amountMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            int share = totalBase <= 0 ? 0 : (int) Math.round((entry.getValue() * 100.0d) / totalBase);
            rows.add(new BreakdownRow(entry.getKey(), entry.getValue(), amount, Math.max(share, entry.getValue() > 0 ? 8 : 0)));
        }

        rows.sort(Comparator.comparing(BreakdownRow::getCount).reversed()
                .thenComparing(BreakdownRow::getAmount, Comparator.reverseOrder()));
        return rows;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String prettifyCode(String value) {
        String normalized = safe(value);
        if (normalized.isEmpty()) {
            return "Not Captured";
        }
        normalized = normalized.replace('_', ' ').replace('-', ' ');
        String[] tokens = normalized.toLowerCase(Locale.ENGLISH).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(token.charAt(0)));
            if (token.length() > 1) {
                builder.append(token.substring(1));
            }
        }
        return builder.toString();
    }

    public String formatCurrency(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        return "Rs. " + safeAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return DASHBOARD_DATE_FORMAT.format(new Date(timestamp.getTime()));
    }

    public String resolveAttributionStatus(ReferralAttribution attribution) {
        if (attribution == null) {
            return "Unknown";
        }
        if ("COMMISSION".equalsIgnoreCase(attribution.getBenefitType())) {
            return "Commission";
        }
        if (attribution.getFreeMonthsAwarded() > 0) {
            return attribution.getFreeMonthsAwarded() + " Free Month";
        }
        return "Tracked";
    }

    public String resolveAttributionStatusClass(ReferralAttribution attribution) {
        if (attribution == null) {
            return "neutral";
        }
        return "COMMISSION".equalsIgnoreCase(attribution.getBenefitType()) ? "commission" : "free";
    }

    public String resolveCommissionStatusClass(String status) {
        String normalized = safe(status).toUpperCase(Locale.ENGLISH);
        if ("PAID".equals(normalized)) {
            return "paid";
        }
        if ("APPROVED".equals(normalized)) {
            return "approved";
        }
        return "pending";
    }

    public List<ReferralAttribution> getReferralAttributions() {
        return referralAttributions;
    }

    public List<ReferralCommission> getReferralCommissions() {
        return referralCommissions;
    }

    public List<BreakdownRow> getPlanBreakdown() {
        return planBreakdown;
    }

    public List<BreakdownRow> getGatewayBreakdown() {
        return gatewayBreakdown;
    }

    public List<BreakdownRow> getCommissionStatusBreakdown() {
        return commissionStatusBreakdown;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public String getBenefitType() {
        return benefitType;
    }

    public String getReferralOwnerName() {
        return referralOwnerName;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public String getHeadline() {
        return headline;
    }

    public String getLatestReferralLabel() {
        return latestReferralLabel;
    }

    public String getTopPlanLabel() {
        return topPlanLabel;
    }

    public Integer getSelectedOrganizationId() {
        return selectedOrganizationId;
    }

    public void setSelectedOrganizationId(Integer selectedOrganizationId) {
        this.selectedOrganizationId = resolveDashboardOrganizationId(selectedOrganizationId);
    }

    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    public String getSelectedOrganizationName() {
        if (isAllOrganizationsSelected()) {
            return "All Organizations";
        }
        if (selectedOrganizationId == null) {
            return getCurrentOrganizationName().isEmpty() ? "Organization" : getCurrentOrganizationName();
        }
        for (Organizations organization : organizationList) {
            if (organization != null && organization.getId() == selectedOrganizationId) {
                return organization.getOrganizationName();
            }
        }
        return getCurrentOrganizationName().isEmpty() ? "Organization" : getCurrentOrganizationName();
    }

    public int getActiveCodeCount() {
        return activeCodeCount;
    }

    public int getTotalReferrals() {
        return totalReferrals;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public BigDecimal getPendingCommission() {
        return pendingCommission;
    }

    public BigDecimal getPaidCommission() {
        return paidCommission;
    }

    public int getPendingCommissionCount() {
        return pendingCommissionCount;
    }

    public int getPaidCommissionCount() {
        return paidCommissionCount;
    }

    public int getTotalFreeMonthsAwarded() {
        return totalFreeMonthsAwarded;
    }

    public BigDecimal getTotalSubscriptionAmount() {
        return totalSubscriptionAmount;
    }

    public BigDecimal getAverageSubscriptionAmount() {
        return averageSubscriptionAmount;
    }

    public boolean isHasReferralData() {
        return totalReferrals > 0 || activeCodeCount > 0 || !referralCode.isEmpty();
    }

    public boolean isCommissionMode() {
        return !referralCommissions.isEmpty() || "Commission".equalsIgnoreCase(benefitType);
    }

    public boolean isAllOrganizationsSelected() {
        return isApplicationAdmin() && ALL_ORGANIZATIONS_ID.equals(selectedOrganizationId);
    }

    private Integer resolveDashboardOrganizationId(Integer requestedOrganizationId) {
        if (isApplicationAdmin()) {
            if (ALL_ORGANIZATIONS_ID.equals(requestedOrganizationId)) {
                return ALL_ORGANIZATIONS_ID;
            }
            if (requestedOrganizationId != null) {
                return requestedOrganizationId;
            }
            return organizationList == null || organizationList.isEmpty() ? null : ALL_ORGANIZATIONS_ID;
        }
        return resolveAccessibleOrganizationId(requestedOrganizationId);
    }

    private List<ReferralAttribution> deduplicateAttributions(List<ReferralAttribution> items) {
        Map<Integer, ReferralAttribution> map = new LinkedHashMap<>();
        for (ReferralAttribution item : items) {
            if (item != null) {
                map.put(item.getId(), item);
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<ReferralCommission> deduplicateCommissions(List<ReferralCommission> items) {
        Map<Integer, ReferralCommission> map = new LinkedHashMap<>();
        for (ReferralCommission item : items) {
            if (item != null) {
                map.put(item.getId(), item);
            }
        }
        return new ArrayList<>(map.values());
    }

    private int countOrganizationCodes(Integer organizationId, List<ReferrerProfile> profiles,
                                       List<OrganizationReferralProfile> organizationProfiles) {
        if (organizationId == null) {
            return 0;
        }
        int personalCodes = (int) profiles.stream()
                .filter(profile -> profile.getUserDetails() != null
                        && profile.getUserDetails().getOrganization() != null
                        && profile.getUserDetails().getOrganization().getId() == organizationId)
                .count();
        int organizationCodes = (int) organizationProfiles.stream()
                .filter(profile -> profile.getOrganization() != null
                        && profile.getOrganization().getId() == organizationId)
                .count();
        return personalCodes + organizationCodes;
    }

    public static final class BreakdownRow implements Serializable {
        private final String label;
        private final int count;
        private final BigDecimal amount;
        private final int share;

        public BreakdownRow(String label, int count, BigDecimal amount, int share) {
            this.label = label;
            this.count = count;
            this.amount = amount == null ? BigDecimal.ZERO : amount;
            this.share = share;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public int getShare() {
            return share;
        }
    }
}
