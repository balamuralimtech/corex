/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.web.coretix.license;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.license.ILicenseService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.web.coretix.appgeneral.GenericManagedBean;
import com.web.coretix.constants.CoreAppModule;
import com.web.coretix.constants.LicenseManagementModule;
import com.web.coretix.constants.RolePrivilegeConstants;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.NotificationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Named("licenseBean")
@Scope("session")
public class LicenseBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13543439334535436L;
    private static final Logger logger = LoggerFactory.getLogger(LicenseBean.class);

    private List<Licenses> licenseList = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();
    private ResourceBundle resourceBundle;

    private String organizationName;
    private java.util.Date startDate;
    private java.util.Date endDate;
    private String remarks;

    private boolean isAddOperation;
    private boolean datatableRendered;
    private int recordsCount;
    private int activeLicenseCount;
    private int expiredLicenseCount;
    private int expiringSoonCount;
    private int licensedUserCount;
    private int unlicensedOrganizationCount;
    private int licenseHealthScore;
    private String portfolioSummary = "No license data available yet.";
    private String licensePulseSummary = "Start by fetching license records.";
    private String mostExposedOrganizationName = "-";
    private String earliestExpiryLabel = "-";
    private List<LicenseDashboardRow> licenseDashboardRows = new ArrayList<>();

    private Licenses selectedLicense = new Licenses();

    @Inject
    private ILicenseService licenseService;

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IUserAdministrationService userAdministrationService;

    public void initializePageAttributes() {
        resourceBundle = ResourceBundle.getBundle("coreAppMessages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        if (CollectionUtils.isNotEmpty(licenseList)) {
            licenseList.clear();
        }
        if (CollectionUtils.isNotEmpty(organizationList)) {
            organizationList.clear();
        }

        fetchRolePrivilegeList();
        fetchLicenseList();
    }

    private void fetchRolePrivilegeList() {
        List<RolePrivilegeConstants> privilegeConstantsList = getModulePrivilegeList(CoreAppModule.LICENCE.getId(), LicenseManagementModule.LICENSE.getId());
        if (CollectionUtils.isNotEmpty(privilegeConstantsList)) {
            for (RolePrivilegeConstants privilegeConstants : privilegeConstantsList) {
                logger.debug("License privilege : " + privilegeConstants);
            }
        }
    }

    private void resetFields() {
        organizationName = "";
        startDate = null;
        endDate = null;
        remarks = "";
    }

    public void addButtonAction() {
        isAddOperation = true;
        selectedLicense = new Licenses();
        resetFields();
    }

    public void searchButtonAction() {
        fetchLicenseList();
    }

    public void confirmEditButtonAction() {
        editLicense();
    }

    private void editLicense() {
        isAddOperation = false;
        if (selectedLicense != null) {
            organizationName = selectedLicense.getOrganization() == null ? "" : selectedLicense.getOrganization().getOrganizationName();
            startDate = selectedLicense.getStartDate();
            endDate = selectedLicense.getEndDate();
            remarks = selectedLicense.getRemarks();
        }
    }

    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizations = getSelectableOrganizations().stream()
                .map(Organizations::getOrganizationName)
                .collect(Collectors.toList());
        return organizations.stream()
                .filter(t -> t.toLowerCase().startsWith(queryLowerCase))
                .collect(Collectors.toList());
    }

    public void saveLicense() {
        if (StringUtils.isBlank(organizationName)) {
            addErrorMessage("Organization is required");
            return;
        }
        if (startDate == null) {
            addErrorMessage("Start date is required");
            return;
        }
        if (endDate == null) {
            addErrorMessage("End date is required");
            return;
        }
        if (endDate.before(startDate)) {
            addErrorMessage("End date should be greater than or equal to start date");
            return;
        }

        Organizations organization = getSelectableOrganizationEntityByName(organizationName);
        if (organization == null) {
            addErrorMessage("Selected organization does not exist");
            return;
        }

        Licenses license = new Licenses();
        license.setOrganization(organization);
        license.setStartDate(new Date(startDate.getTime()));
        license.setEndDate(new Date(endDate.getTime()));
        license.setRemarks(remarks);

        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants status;
        if (isAddOperation) {
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            status = licenseService.addLicense(userActivityTO, license);
            handleSaveStatus(status, true);
        } else {
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            license.setId(selectedLicense.getId());
            status = licenseService.updateLicense(userActivityTO, license);
            handleSaveStatus(status, false);
        }

        if (status == GeneralConstants.SUCCESSFUL) {
            fetchLicenseList();
            PrimeFaces.current().executeScript("PF('manageLicenseDialog').hide()");
            PrimeFaces.current().ajax().update("form:licenseMainPanelId", "form:messages");
        }
    }

    private void handleSaveStatus(GeneralConstants status, boolean addOperation) {
        switch (status) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(addOperation
                                ? resourceBundle.getString("licenseAddedSuccessfullyLabel")
                                : resourceBundle.getString("licenseUpdatedSuccessfullyLabel")));
                break;
            case ENTRY_ALREADY_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                                resourceBundle.getString("licenseAlreadyExistsLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                                resourceBundle.getString("licenseDoesNotExistLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"),
                                addOperation
                                        ? resourceBundle.getString("licenseAddFailedLabel")
                                        : resourceBundle.getString("licenseUpdateFailedLabel")));
                break;
        }
    }

    public void confirmDeleteLicense() {
        if (selectedLicense == null) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        GeneralConstants deleteStatus = licenseService.deleteLicense(userActivityTO, selectedLicense);
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(resourceBundle.getString("licenseDeletedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                                resourceBundle.getString("licenseDoesNotExistLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"),
                                resourceBundle.getString("licenseRemovalFailedLabel")));
                break;
        }

        fetchLicenseList();
        PrimeFaces.current().ajax().update("form:messages", "form:licenseDataTableId");
    }

    private void fetchLicenseList() {
        datatableRendered = false;
        if (CollectionUtils.isNotEmpty(licenseList)) {
            licenseList.clear();
        }
        List<Licenses> visibleLicenses = new ArrayList<>(licenseService.getLicenseList());
        if (!isApplicationAdmin()) {
            Integer currentOrganizationId = fetchCurrentOrganizationId();
            visibleLicenses.removeIf(license -> license == null
                    || license.getOrganization() == null
                    || currentOrganizationId == null
                    || license.getOrganization().getId() != currentOrganizationId);
        }
        licenseList.addAll(visibleLicenses);
        refreshDashboardMetrics();
        if (CollectionUtils.isNotEmpty(licenseList)) {
            datatableRendered = true;
            recordsCount = licenseList.size();
        }
    }

    private void refreshDashboardMetrics() {
        activeLicenseCount = 0;
        expiredLicenseCount = 0;
        expiringSoonCount = 0;
        licensedUserCount = 0;
        unlicensedOrganizationCount = 0;
        licenseHealthScore = 0;
        portfolioSummary = "No license data available yet.";
        licensePulseSummary = "Start by fetching license records.";
        mostExposedOrganizationName = "-";
        earliestExpiryLabel = "-";
        licenseDashboardRows = new ArrayList<>();

        List<Licenses> visibleLicenses = new ArrayList<>(licenseList);
        List<Organizations> visibleOrganizations = new ArrayList<>(getAccessibleOrganizations(organizationService));
        List<UserDetails> visibleUsers = new ArrayList<>(userAdministrationService.getUserDetailsList());
        if (!isApplicationAdmin()) {
            visibleUsers.removeIf(user -> user == null || !canAccessOrganization(userOrganizationId(user)));
        }

        Map<Integer, Integer> userCountByOrganization = new HashMap<>();
        for (UserDetails userDetails : visibleUsers) {
            Integer organizationId = userOrganizationId(userDetails);
            if (organizationId == null) {
                continue;
            }
            userCountByOrganization.merge(organizationId, 1, Integer::sum);
        }

        java.util.Date today = new java.util.Date();
        LicenseDashboardRow mostExposedOrganization = null;
        LicenseDashboardRow earliestExpiry = null;

        for (Licenses license : visibleLicenses) {
            if (license == null || license.getOrganization() == null) {
                continue;
            }

            int usersUnderLicense = userCountByOrganization.getOrDefault(license.getOrganization().getId(), 0);
            licensedUserCount += usersUnderLicense;

            long daysRemaining = calculateDaysRemaining(license, today);
            boolean active = license.isActive();
            boolean expiringSoon = active && daysRemaining <= 30;
            boolean expiringCritical = active && daysRemaining <= 7;

            if (active) {
                activeLicenseCount++;
            } else {
                expiredLicenseCount++;
            }
            if (expiringSoon) {
                expiringSoonCount++;
            }

            LicenseDashboardRow row = new LicenseDashboardRow(
                    license.getOrganization().getId(),
                    license.getOrganization().getOrganizationName(),
                    usersUnderLicense,
                    getFormattedDate(license.getEndDate()),
                    getLicenseStatus(license),
                    daysRemaining,
                    license.getRemarks(),
                    active ? (expiringCritical ? "Expiring" : expiringSoon ? "Attention" : "Healthy") : "Expired");
            licenseDashboardRows.add(row);

            if (mostExposedOrganization == null || row.getUserCount() > mostExposedOrganization.getUserCount()) {
                mostExposedOrganization = row;
            }

            if (earliestExpiry == null || row.getDaysRemaining() < earliestExpiry.getDaysRemaining()) {
                earliestExpiry = row;
            }
        }

        licenseDashboardRows.sort(Comparator
                .comparingLong(LicenseDashboardRow::getDaysRemaining)
                .thenComparing(LicenseDashboardRow::getOrganizationName, String.CASE_INSENSITIVE_ORDER));

        if (!visibleOrganizations.isEmpty()) {
            int licensedOrganizationCount = (int) visibleLicenses.stream()
                    .filter(license -> license != null && license.getOrganization() != null)
                    .map(license -> license.getOrganization().getId())
                    .distinct()
                    .count();
            unlicensedOrganizationCount = Math.max(0, visibleOrganizations.size() - licensedOrganizationCount);
        }

        if (!visibleLicenses.isEmpty()) {
            licenseHealthScore = (int) Math.round((activeLicenseCount * 100.0d) / visibleLicenses.size());
            portfolioSummary = activeLicenseCount + " active licenses, " + expiredLicenseCount + " expired, "
                    + expiringSoonCount + " expiring within 30 days.";
            licensePulseSummary = licensedUserCount + " users currently sit under visible licensed organizations.";
        }

        if (mostExposedOrganization != null) {
            mostExposedOrganizationName = mostExposedOrganization.getOrganizationName() + " (" + mostExposedOrganization.getUserCount() + " users)";
        }

        if (earliestExpiry != null) {
            earliestExpiryLabel = earliestExpiry.getOrganizationName() + " - "
                    + formatDaysRemaining(earliestExpiry.getDaysRemaining()) + " remaining";
        }
    }

    private Integer userOrganizationId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getOrganization() == null) {
            return null;
        }
        return userDetails.getOrganization().getId();
    }

    private long calculateDaysRemaining(Licenses license, java.util.Date today) {
        if (license == null || license.getEndDate() == null) {
            return Long.MAX_VALUE;
        }
        long diffMillis = license.getEndDate().getTime() - today.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    public String formatDaysRemaining(long daysRemaining) {
        if (daysRemaining == Long.MAX_VALUE) {
            return "-";
        }
        if (daysRemaining < 0) {
            return Math.abs(daysRemaining) + " days overdue";
        }
        if (daysRemaining == 0) {
            return "Ends today";
        }
        if (daysRemaining == 1) {
            return "1 day left";
        }
        return daysRemaining + " days left";
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), detail));
        PrimeFaces.current().ajax().update("form:messages", "form:addEditLicensePanelId");
    }

    private List<Organizations> getOrganizations() {
        if (CollectionUtils.isEmpty(organizationList)) {
            organizationList.addAll(getAccessibleOrganizations(organizationService));
        }
        return organizationList;
    }

    private List<Organizations> getSelectableOrganizations() {
        List<Organizations> organizations = new ArrayList<>(getOrganizations());
        if (!isAddOperation) {
            return organizations;
        }

        List<Integer> licensedOrganizationIds = licenseService.getLicenseList().stream()
                .filter(license -> license != null && license.getOrganization() != null)
                .map(license -> license.getOrganization().getId())
                .distinct()
                .collect(Collectors.toList());

        organizations.removeIf(organization -> organization == null
                || licensedOrganizationIds.contains(organization.getId()));
        return organizations;
    }

    private Organizations getSelectableOrganizationEntityByName(String inputOrganizationName) {
        for (Organizations organization : getSelectableOrganizations()) {
            if (organization.getOrganizationName().equalsIgnoreCase(inputOrganizationName)) {
                return organization;
            }
        }
        return null;
    }

    public UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

        if (httpSession != null) {
            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }

    public String getFormattedDate(java.util.Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public String getLicenseStatus(Licenses license) {
        return license != null && license.isActive() ? "Active" : "Expired";
    }

    public String statusSeverity(String status) {
        if ("Expired".equalsIgnoreCase(status)) {
            return "danger";
        }
        if ("Expiring".equalsIgnoreCase(status)) {
            return "danger";
        }
        if ("Attention".equalsIgnoreCase(status)) {
            return "warning";
        }
        return "success";
    }

    public String statusStyleClass(String status) {
        if ("Expiring".equalsIgnoreCase(status)) {
            return "license-tag-blink";
        }
        return "";
    }

    public boolean shouldRenderNotifyButton(String status) {
        return "Attention".equalsIgnoreCase(status) || "Expiring".equalsIgnoreCase(status);
    }

    public void notifyOrganizationUsers(LicenseDashboardRow dashboardRow) {
        if (dashboardRow == null || dashboardRow.getOrganizationId() == null || StringUtils.isBlank(dashboardRow.getOrganizationName())) {
            return;
        }

        if (!canAccessOrganization(dashboardRow.getOrganizationId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                            "Unable to resolve the organization for notification."));
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }

        String notificationMessage = String.format("%s license is %s and ends on %s. Please contact admin.",
                dashboardRow.getOrganizationName(),
                dashboardRow.getHealthLabel().toLowerCase(),
                dashboardRow.getExpiryDate());

        NotificationService.sendGrowlMessageToOrganization(dashboardRow.getOrganizationId(), notificationMessage);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Notification Sent",
                        "License expiry notification was sent to active users in " + dashboardRow.getOrganizationName() + "."));
        PrimeFaces.current().ajax().update("form:messages", "form:licenseMainPanelId");
    }

    public int getTotalLicenseCount() {
        return licenseList == null ? 0 : licenseList.size();
    }

    public int getActiveLicenseCount() {
        return activeLicenseCount;
    }

    public int getExpiredLicenseCount() {
        return expiredLicenseCount;
    }

    public int getExpiringSoonCount() {
        return expiringSoonCount;
    }

    public int getLicensedUserCount() {
        return licensedUserCount;
    }

    public int getUnlicensedOrganizationCount() {
        return unlicensedOrganizationCount;
    }

    public int getLicenseHealthScore() {
        return licenseHealthScore;
    }

    public String getLicenseHealthRingStyle() {
        int safeScore = Math.max(0, Math.min(100, licenseHealthScore));
        int activeDegrees = (int) Math.round(safeScore * 3.6d);
        return "background:conic-gradient(#16a34a 0deg, #16a34a " + activeDegrees
                + "deg, #dbeafe " + activeDegrees + "deg, #dbeafe 360deg);";
    }

    public String getPortfolioSummary() {
        return portfolioSummary;
    }

    public String getLicensePulseSummary() {
        return licensePulseSummary;
    }

    public String getMostExposedOrganizationName() {
        return mostExposedOrganizationName;
    }

    public String getEarliestExpiryLabel() {
        return earliestExpiryLabel;
    }

    public List<LicenseDashboardRow> getLicenseDashboardRows() {
        return licenseDashboardRows;
    }

    public List<Licenses> getLicenseList() {
        return licenseList;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public java.util.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public Licenses getSelectedLicense() {
        return selectedLicense;
    }

    public void setSelectedLicense(Licenses selectedLicense) {
        this.selectedLicense = selectedLicense;
    }

    public static class LicenseDashboardRow implements Serializable {
        private final Integer organizationId;
        private final String organizationName;
        private final int userCount;
        private final String expiryDate;
        private final String status;
        private final long daysRemaining;
        private final String remarks;
        private final String healthLabel;

        public LicenseDashboardRow(Integer organizationId, String organizationName, int userCount, String expiryDate, String status,
                                   long daysRemaining, String remarks, String healthLabel) {
            this.organizationId = organizationId;
            this.organizationName = organizationName;
            this.userCount = userCount;
            this.expiryDate = expiryDate;
            this.status = status;
            this.daysRemaining = daysRemaining;
            this.remarks = remarks;
            this.healthLabel = healthLabel;
        }

        public Integer getOrganizationId() {
            return organizationId;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public int getUserCount() {
            return userCount;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public String getStatus() {
            return status;
        }

        public long getDaysRemaining() {
            return daysRemaining;
        }

        public String getRemarks() {
            return remarks;
        }

        public String getHealthLabel() {
            return healthLabel;
        }
    }
}





