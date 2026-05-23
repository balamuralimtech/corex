package com.web.coretix.menu;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

@Named
public class CommonMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup userManagement = new AppMenuGroup(
                "m_usermanagement",
                "#{msg['userManagementLabel']}",
                "pi pi-fw pi-users",
                20,
                "#{guestPreferences.userManagementRendered}")
                .addItem(new AppMenuItem("m_userprofile", "#{msg['userProfileLabel']}", "pi pi-fw pi-user",
                        "/user-profile", 10, "#{guestPreferences.userProfileRendered}"))
                .addItem(new AppMenuItem("m_useractivity", "#{msg['userActivityLabel']}",
                        "pi pi-fw pi-align-justify", "/user-activity", 20,
                        "#{guestPreferences.userActivityRendered}"))
                .addItem(new AppMenuItem("m_roleadministration", "#{msg['roleAdministrationLabel']}",
                        "pi pi-fw pi-briefcase", "/manage-role", 30,
                        "#{guestPreferences.roleAdministrationRendered}"))
                .addItem(new AppMenuItem("m_changepassword", "#{msg['changePasswordLabel']}", "pi pi-fw pi-key",
                        "/change-password", 40, "#{guestPreferences.changePasswordRendered}"))
                .addItem(new AppMenuItem("m_useradministration", "#{msg['userAdministrationLabel']}",
                        "pi pi-fw pi-id-card", "/manage-user", 50,
                        "#{guestPreferences.userAdministrationRendered}"));

        AppMenuGroup systemManagement = new AppMenuGroup(
                "m_systemmanagement",
                "#{msg['systemManagementLabel']}",
                "pi pi-fw pi-cog",
                30,
                "#{guestPreferences.systemManagementRendered}")
                .addItem(new AppMenuItem("m_organization", "#{msg['organizationLabel']}", "pi pi-fw pi-building",
                        "/organization", 10, "#{guestPreferences.organizationRendered}"))
                .addItem(new AppMenuItem("m_branch", "#{msg['branchLabel']}", "pi pi-fw pi-map-marker",
                        "/branch", 20, "#{guestPreferences.branchRendered}"))
                .addItem(new AppMenuItem("m_department", "#{msg['departmentLabel']}", "pi pi-fw pi-sitemap",
                        "/department", 30, "#{guestPreferences.departmentRendered}"))
                .addItem(new AppMenuItem("m_designation", "#{msg['designationLabel']}", "pi pi-fw pi-id-card",
                        "/designation", 40, "#{guestPreferences.designationRendered}"))
                .addItem(new AppMenuItem("m_country", "#{msg['countryLabel']}", "pi pi-fw pi-flag",
                        "/country", 50, "#{guestPreferences.countryRendered}"))
                .addItem(new AppMenuItem("m_state", "#{msg['stateLabel']}", "pi pi-fw pi-flag",
                        "/state", 60, "#{guestPreferences.stateRendered}"))
                .addItem(new AppMenuItem("m_city", "#{msg['cityLabel']}", "pi pi-fw pi-flag",
                        "/city", 70, "#{guestPreferences.cityRendered}"))
                .addItem(new AppMenuItem("m_region", "#{msg['regionLabel']}", "pi pi-fw pi-flag",
                        "/region", 80, "#{guestPreferences.regionRendered}"))
                .addItem(new AppMenuItem("m_subregion", "#{msg['subregionLabel']}", "pi pi-fw pi-flag",
                        "/subregion", 90, "#{guestPreferences.subregionRendered}"))
                .addItem(new AppMenuItem("m_currency", "#{msg['currencyLabel']}", "pi pi-fw pi-dollar",
                        "/currency", 100, "#{guestPreferences.currencyRendered}"))
                .addItem(new AppMenuItem("m_bankdetails", "#{msg['bankDetailsLabel']}", "pi pi-fw pi-dollar",
                        "/bank-details", 110, "#{guestPreferences.bankDetailsRendered}"))
                .addItem(new AppMenuItem("m_notificationsettings", "#{msg['notificationSettingsLabel']}",
                        "pi pi-fw pi-at", "/notification-settings", 120,
                        "#{guestPreferences.notificationSettingRendered}"));

        AppMenuGroup applicationManagement = new AppMenuGroup(
                "m_applicationmanagement",
                "#{msg['applicationManagementLabel']}",
                "pi pi-fw pi-briefcase",
                10,
                "#{guestPreferences.applicationManagementRendered}")
                .addItem(new AppMenuItem("m_demorequests", "#{msg['demoRequestsLabel']}", "pi pi-fw pi-inbox",
                        "/demo-requests", 10, "#{guestPreferences.demoRequestsRendered}"))
                .addItem(new AppMenuItem("m_applicationpricing", "#{msg['applicationPricingLabel']}", "pi pi-fw pi-tags",
                        "/application-pricing", 20, "#{guestPreferences.applicationPricingRendered}"))
                .addItem(new AppMenuItem("m_referralmanagement", "#{msg['referralManagementLabel']}", "pi pi-fw pi-share-alt",
                        "/referral-management", 30, "#{guestPreferences.referralManagementRendered}"))
                .addItem(new AppMenuItem("m_referraldashboard", "#{msg['referralDashboardLabel']}", "pi pi-fw pi-chart-line",
                        "/referral-dashboard", 40, "#{guestPreferences.referralDashboardRendered}"))
                .addItem(new AppMenuItem("m_feedbackinbox", "Feedback Inbox", "pi pi-fw pi-comments",
                        "/feedback-inbox", 42, "#{homePageBean.applicationAdmin}"))
                .addItem(new AppMenuItem("m_applicationnotifications", "#{msg['applicationNotificationsLabel']}",
                        "pi pi-fw pi-envelope", "/application-notifications", 45,
                        "#{guestPreferences.notificationSettingRendered}"))
                .addItem(new AppMenuItem("m_licensepage", "#{msg['licenseLabel']}", "pi pi-fw pi-ticket",
                        "/license", 50, "#{guestPreferences.licenseRendered}"))
                .addItem(new AppMenuItem("m_serverlogspage", "#{msg['serverLogsLabel']}", "pi pi-fw pi-server",
                        "/server-logs", 60, "#{guestPreferences.serverLogRendered}"))
                .addItem(new AppMenuItem("m_databasedetails", "#{msg['databaseLabel']}", "pi pi-fw pi-database",
                        "/database-details", 70, "#{guestPreferences.dbDetailsRendered}"))
                .addItem(new AppMenuItem("m_errorlogmonitor", "#{msg['errorLogMonitoringLabel']}", "pi pi-fw pi-bell",
                        "/error-log-monitor", 80, "#{guestPreferences.errorLogMonitorRendered}"));

        return Arrays.asList(userManagement, systemManagement, applicationManagement);
    }
}
