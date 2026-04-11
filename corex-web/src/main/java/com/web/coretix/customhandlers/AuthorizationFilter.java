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
package com.web.coretix.customhandlers;

import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.web.coretix.constants.CoreAppModule;
import com.web.coretix.constants.ApplicationManagementModule;
import com.web.coretix.constants.LicenseManagementModule;
import com.web.coretix.constants.ServerAndDBModule;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.SystemManagementModule;
import com.web.coretix.constants.UserManagementModule;
import com.web.coretix.constants.UserTypeConstants;
import com.web.coretix.filter.FriendlyUrlFilter;
import com.module.coretix.usermanagement.IUserAdministrationService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthorizationFilter implements Filter {

    private static final String DEFAULT_LOGIN_VIDEO = "/resources/avalon-layout/videos/home.mp4";
    private static final java.util.Set<String> PUBLIC_INTERNAL_PATHS = java.util.Set.of(
            "/pages/shipx/public/customer-request-form.xhtml",
            "/setup.xhtml");
    private static final java.util.Map<String, int[]> PAGE_ACCESS_RULES = createPageAccessRules();

    private IUserAdministrationService userAdministrationService;
    private IRoleAdministrationService roleAdministrationService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext());
        userAdministrationService = context.getBean(IUserAdministrationService.class);
        roleAdministrationService = context.getBean(IRoleAdministrationService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        String login2URI = req.getContextPath() + "/landing";
        String loginURI = req.getContextPath() + "/login";
        String setupURI = req.getContextPath() + "/setup";
        String login2InternalURI = req.getContextPath() + "/login2.xhtml";
        String loginInternalURI = req.getContextPath() + "/login.xhtml";
        String setupInternalURI = req.getContextPath() + "/setup.xhtml";
        String loginVideoPath = System.getProperty("app.login.video", DEFAULT_LOGIN_VIDEO);
        String videoURI = req.getContextPath() + loginVideoPath;
        String requestPath = req.getRequestURI().substring(req.getContextPath().length());

        boolean loggedIn = (session != null && session.getAttribute(SessionAttributes.USERNAME.getName()) != null);
        boolean loginRequest = req.getRequestURI().equals(loginURI);
        boolean setupRequest = req.getRequestURI().equals(setupURI);
        boolean login2Request = req.getRequestURI().equals(login2URI);
        boolean loginInternalRequest = req.getRequestURI().equals(loginInternalURI);
        boolean login2InternalRequest = req.getRequestURI().equals(login2InternalURI);
        boolean setupInternalRequest = req.getRequestURI().equals(setupInternalURI);
        boolean videoRequest = req.getRequestURI().equals(videoURI);
        boolean publicFriendlyRequest = FriendlyUrlFilter.PUBLIC_FRIENDLY_PATHS.contains(requestPath);
        boolean publicInternalRequest = PUBLIC_INTERNAL_PATHS.contains(requestPath);
        boolean resourceRequest = req.getRequestURI().startsWith(req.getContextPath() + "/javax.faces.resource");
        boolean bootstrapRequired = userAdministrationService != null && userAdministrationService.getUserCount() == 0;

        if (bootstrapRequired && !(setupRequest || setupInternalRequest || publicFriendlyRequest
                || publicInternalRequest || resourceRequest || videoRequest)) {
            res.sendRedirect(setupURI);
            return;
        }

        if (loggedIn || loginRequest || login2Request || loginInternalRequest || login2InternalRequest
                || setupRequest || setupInternalRequest || videoRequest || publicFriendlyRequest || publicInternalRequest || resourceRequest) {
            if (loggedIn && isProtectedPage(requestPath) && !hasPageAccess(session, requestPath)) {
                res.sendRedirect(req.getContextPath() + "/forbidden");
                return;
            }
            chain.doFilter(request, response); // User is logged in, so continue with the request.
        } else {
            res.sendRedirect(login2URI); // Not logged in, redirect to login page.
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isProtectedPage(String requestPath) {
        return PAGE_ACCESS_RULES.containsKey(requestPath);
    }

    private boolean hasPageAccess(HttpSession session, String requestPath) {
        if (session == null) {
            return false;
        }

        Object userType = session.getAttribute(SessionAttributes.USER_TYPE.getName());
        if (userType instanceof String
                && UserTypeConstants.APPLICATION_ADMIN == UserTypeConstants.fromValue((String) userType)) {
            return true;
        }

        Object roleId = session.getAttribute(SessionAttributes.ROLE_ID.getName());
        if (!(roleId instanceof Integer) || roleAdministrationService == null) {
            return false;
        }

        int[] accessRule = PAGE_ACCESS_RULES.get(requestPath);
        if (accessRule == null) {
            return true;
        }

        int moduleId = accessRule[0];
        int submoduleId = accessRule[1];
        if (moduleId == CoreAppModule.LICENCE.getId() || moduleId == CoreAppModule.SERVER_AND_DB.getId()) {
            return false;
        }
        return roleAdministrationService.getSubmodulesByRoleandModuleId((Integer) roleId, moduleId).contains(submoduleId);
    }

    private static java.util.Map<String, int[]> createPageAccessRules() {
        java.util.Map<String, int[]> rules = new java.util.HashMap<>();
        rules.put("/user-profile", new int[]{CoreAppModule.USER_MANAGEMENT.getId(), UserManagementModule.USER_PROFILE.getId()});
        rules.put("/user-activity", new int[]{CoreAppModule.USER_MANAGEMENT.getId(), UserManagementModule.USER_ACTIVITY.getId()});
        rules.put("/manage-role", new int[]{CoreAppModule.USER_MANAGEMENT.getId(), UserManagementModule.ROLE_ADMINISTRATION.getId()});
        rules.put("/change-password", new int[]{CoreAppModule.USER_MANAGEMENT.getId(), UserManagementModule.CHANGE_PASSWORD.getId()});
        rules.put("/manage-user", new int[]{CoreAppModule.USER_MANAGEMENT.getId(), UserManagementModule.USER_ADMINISTRATION.getId()});
        rules.put("/organization", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.ORGANIZATION.getId()});
        rules.put("/branch", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.BRANCH.getId()});
        rules.put("/department", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.DEPARTMENT.getId()});
        rules.put("/designation", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.DESIGNATION.getId()});
        rules.put("/country", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.COUNTRY.getId()});
        rules.put("/state", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.STATE.getId()});
        rules.put("/city", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.CITY.getId()});
        rules.put("/region", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.REGION.getId()});
        rules.put("/subregion", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.SUBREGION.getId()});
        rules.put("/currency", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.CURRENCY.getId()});
        rules.put("/bank-details", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.BANK_DETAILS.getId()});
        rules.put("/notification-settings", new int[]{CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.NOTIFICATION_SETTING.getId()});
        rules.put("/demo-requests", new int[]{CoreAppModule.APPLICATION_MANAGEMENT.getId(), ApplicationManagementModule.DEMO_REQUESTS.getId()});
        rules.put("/license", new int[]{CoreAppModule.LICENCE.getId(), LicenseManagementModule.LICENSE.getId()});
        rules.put("/server-logs", new int[]{CoreAppModule.SERVER_AND_DB.getId(), ServerAndDBModule.SERVER_LOGS.getId()});
        rules.put("/database-details", new int[]{CoreAppModule.SERVER_AND_DB.getId(), ServerAndDBModule.DATABASE.getId()});
        rules.put("/error-log-monitor", new int[]{CoreAppModule.SERVER_AND_DB.getId(), ServerAndDBModule.ERROR_LOG_MONITOR.getId()});
        return java.util.Collections.unmodifiableMap(rules);
    }
}




