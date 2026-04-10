package com.web.coretix.menu;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommonMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup userManagement = new AppMenuGroup(
                "m_usermanagement",
                "#{msg['userManagementLabel']}",
                "pi pi-fw pi-users",
                10,
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
                20,
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
                        "#{guestPreferences.notificationSettingRendered}"))
                .addItem(new AppMenuItem("m_applicationnotifications", "#{msg['applicationNotificationsLabel']}",
                        "pi pi-fw pi-envelope", "/application-notifications", 130,
                        "#{guestPreferences.notificationSettingRendered}"));

        AppMenuGroup license = new AppMenuGroup(
                "m_license",
                "#{msg['licenseLabel']}",
                "pi pi-fw pi-ticket",
                30,
                "#{guestPreferences.licenseManagementRendered}")
                .addItem(new AppMenuItem("m_licensepage", "#{msg['licenseLabel']}", "pi pi-fw pi-file-o",
                        "/license", 10, "#{guestPreferences.licenseRendered}"));

        AppMenuGroup serverLogs = new AppMenuGroup(
                "m_serverlogs",
                "#{msg['serverAndDBLabel']}",
                "pi pi-fw pi-server",
                40,
                "#{guestPreferences.dbAndServerLogRendered}")
                .addItem(new AppMenuItem("m_serverlogspage", "#{msg['serverLogsLabel']}", "pi pi-fw pi-server",
                        "/server-logs", 10, "#{guestPreferences.serverLogRendered}"))
                .addItem(new AppMenuItem("m_databasedetails", "#{msg['databaseLabel']}", "pi pi-fw pi-server",
                        "/database-details", 20, "#{guestPreferences.dbDetailsRendered}"))
                .addItem(new AppMenuItem("m_errorlogmonitor", "#{msg['errorLogMonitoringLabel']}", "pi pi-fw pi-bell",
                        "/error-log-monitor", 30, "#{guestPreferences.errorLogMonitorRendered}"));

        return Arrays.asList(userManagement, systemManagement, license, serverLogs);
    }
}
