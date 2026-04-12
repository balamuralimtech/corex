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
package com.web.coretix.filter;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserTypeConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FriendlyUrlFilter implements Filter {
    private static final String CORE_DASHBOARD_FRIENDLY_PATH = "/core-dashboard";
    private static final String CORE_DASHBOARD_INTERNAL_PATH = "/pages/home/homePage.xhtml";
    private static final String DASHBOARD_CYCLE_INDEX = "dashboardCycleIndex";

    private static final Set<String> SHARED_PAGE_DIRECTORIES = Set.of(
            "applicationmanagement",
            "errorandwarningpages",
            "home",
            "license",
            "serverlogs",
            "systemmanagement",
            "usermanagement");

    public static final java.util.Set<String> PUBLIC_FRIENDLY_PATHS = java.util.Set.of(
            "/",
            "/landing",
            "/login",
            "/setup",
            "/demo-request",
            "/clinic-register",
            "/payments/razorpay/order",
            "/payments/razorpay/verify",
            "/customer-request-form",
            "/error",
            "/bad-request",
            "/unauthorized",
            "/forbidden",
            "/not-found",
            "/session-timeout",
            "/internal-server-error",
            "/not-implemented",
            "/service-unavailable",
            "/bad-gateway",
            "/gateway-timeout",
            "/http-version-not-supported",
            "/variant-also-negotiates",
            "/bandwidth-limit-exceeded",
            "/not-extended");

    private Map<String, String> friendlyToInternalPaths;
    private Map<String, String> internalToFriendlyPaths;
    private List<String> applicationDashboardPaths;
    private String primaryApplicationDashboardPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Map<String, String> paths = new LinkedHashMap<>(buildFriendlyToInternalPaths());
        paths.putAll(discoverApplicationFriendlyPaths(filterConfig.getServletContext()));
        friendlyToInternalPaths = Collections.unmodifiableMap(paths);
        internalToFriendlyPaths = Collections.unmodifiableMap(buildInternalToFriendlyPaths(friendlyToInternalPaths));
        applicationDashboardPaths = Collections.unmodifiableList(discoverApplicationDashboardPaths(filterConfig.getServletContext()));
        primaryApplicationDashboardPath = resolvePrimaryApplicationDashboardPath(applicationDashboardPaths);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestUri.substring(contextPath.length());

        if ("/home".equals(path)) {
            httpResponse.sendRedirect(contextPath + resolveHomeTarget(httpRequest));
            return;
        }

        String internalPath = friendlyToInternalPaths.get(path);
        if (internalPath != null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(internalPath);
            dispatcher.forward(request, response);
            return;
        }

        String friendlyPath = internalToFriendlyPaths.get(path);
        if (friendlyPath != null
                && "GET".equalsIgnoreCase(httpRequest.getMethod())
                && !isErrorDispatch(httpRequest)) {
            httpResponse.sendRedirect(contextPath + friendlyPath);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isErrorDispatch(HttpServletRequest request) {
        return request.getAttribute("javax.servlet.error.status_code") != null
                || request.getAttribute("jakarta.servlet.error.status_code") != null;
    }

    private static Map<String, String> buildFriendlyToInternalPaths() {
        Map<String, String> paths = new LinkedHashMap<>();

        paths.put("/", "/landing.xhtml");
        paths.put("/landing", "/landing.xhtml");
        paths.put("/login", "/login.xhtml");
        paths.put("/setup", "/setup.xhtml");
        paths.put("/home", CORE_DASHBOARD_INTERNAL_PATH);
        paths.put(CORE_DASHBOARD_FRIENDLY_PATH, CORE_DASHBOARD_INTERNAL_PATH);

        paths.put("/user-profile", "/pages/usermanagement/userprofile.xhtml");
        paths.put("/user-activity", "/pages/usermanagement/useractivity.xhtml");
        paths.put("/manage-role", "/pages/usermanagement/roleAdministration.xhtml");
        paths.put("/change-password", "/pages/usermanagement/changePassword.xhtml");
        paths.put("/manage-user", "/pages/usermanagement/useradministration.xhtml");

        paths.put("/organization", "/pages/systemmanagement/organization.xhtml");
        paths.put("/branch", "/pages/systemmanagement/branch.xhtml");
        paths.put("/department", "/pages/systemmanagement/department.xhtml");
        paths.put("/designation", "/pages/systemmanagement/designation.xhtml");
        paths.put("/country", "/pages/systemmanagement/country.xhtml");
        paths.put("/state", "/pages/systemmanagement/state.xhtml");
        paths.put("/city", "/pages/systemmanagement/city.xhtml");
        paths.put("/region", "/pages/systemmanagement/region.xhtml");
        paths.put("/subregion", "/pages/systemmanagement/subregion.xhtml");
        paths.put("/currency", "/pages/systemmanagement/currency.xhtml");
        paths.put("/bank-details", "/pages/systemmanagement/bankdetails.xhtml");
        paths.put("/notification-settings", "/pages/systemmanagement/notificationsettings.xhtml");
        paths.put("/application-notifications", "/pages/systemmanagement/applicationnotifications.xhtml");
        paths.put("/demo-requests", "/pages/applicationmanagement/demorequests.xhtml");
        paths.put("/application-pricing", "/pages/applicationmanagement/applicationpricing.xhtml");
        paths.put("/referral-management", "/pages/applicationmanagement/referralmanagement.xhtml");
        paths.put("/referral-dashboard", "/pages/applicationmanagement/referraldashboard.xhtml");

        paths.put("/license", "/pages/license/license.xhtml");
        paths.put("/server-logs", "/pages/serverlogs/serverlogs.xhtml");
        paths.put("/database-details", "/pages/serverlogs/databaseDetails.xhtml");
        paths.put("/error-log-monitor", "/pages/serverlogs/errorLogMonitor.xhtml");

        paths.put("/error", "/pages/errorandwarningpages/error.xhtml");
        paths.put("/bad-request", "/pages/errorandwarningpages/400badrequest.xhtml");
        paths.put("/unauthorized", "/pages/errorandwarningpages/401unauthorized.xhtml");
        paths.put("/forbidden", "/pages/errorandwarningpages/403forbidden.xhtml");
        paths.put("/not-found", "/pages/errorandwarningpages/404notfound.xhtml");
        paths.put("/session-timeout", "/pages/errorandwarningpages/408sessiontimeout.xhtml");
        paths.put("/internal-server-error", "/pages/errorandwarningpages/500internalservererror.xhtml");
        paths.put("/not-implemented", "/pages/errorandwarningpages/501notimplemented.xhtml");
        paths.put("/service-unavailable", "/pages/errorandwarningpages/501serviceunavailable.xhtml");
        paths.put("/bad-gateway", "/pages/errorandwarningpages/502badgateway.xhtml");
        paths.put("/gateway-timeout", "/pages/errorandwarningpages/504gatewaytimeout.xhtml");
        paths.put("/http-version-not-supported", "/pages/errorandwarningpages/505httpversionnotsupported.xhtml");
        paths.put("/variant-also-negotiates", "/pages/errorandwarningpages/506variantalsonegotiates.xhtml");
        paths.put("/bandwidth-limit-exceeded", "/pages/errorandwarningpages/509bandwidthlimitexceeded.xhtml");
        paths.put("/not-extended", "/pages/errorandwarningpages/510notextended.xhtml");

        return Collections.unmodifiableMap(paths);
    }

    private static Map<String, String> buildInternalToFriendlyPaths(Map<String, String> friendlyToInternalPaths) {
        Map<String, String> paths = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : friendlyToInternalPaths.entrySet()) {
            paths.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(paths);
    }

    private Map<String, String> discoverApplicationFriendlyPaths(ServletContext servletContext) {
        Map<String, String> paths = new LinkedHashMap<>();
        Set<String> pageResources = new HashSet<>();
        collectPageResources(servletContext, "/pages/", pageResources);

        for (String internalPath : pageResources) {
            if (!isApplicationPage(internalPath)) {
                continue;
            }

            String friendlyPath = toApplicationFriendlyPath(internalPath);
            if (!friendlyPath.isEmpty() && !paths.containsKey(friendlyPath)) {
                paths.put(friendlyPath, internalPath);
                continue;
            }

            String fallbackFriendlyPath = toFriendlyPath(internalPath);
            if (!fallbackFriendlyPath.isEmpty() && !paths.containsKey(fallbackFriendlyPath)) {
                paths.put(fallbackFriendlyPath, internalPath);
            }
        }

        return paths;
    }

    private void collectPageResources(ServletContext servletContext, String directory, Set<String> pageResources) {
        Set<String> resourcePaths = servletContext.getResourcePaths(directory);
        if (resourcePaths == null || resourcePaths.isEmpty()) {
            return;
        }

        for (String resourcePath : resourcePaths) {
            if (resourcePath.endsWith("/")) {
                collectPageResources(servletContext, resourcePath, pageResources);
            } else if (resourcePath.endsWith(".xhtml")) {
                pageResources.add(resourcePath);
            }
        }
    }

    private boolean isApplicationPage(String internalPath) {
        if (!internalPath.startsWith("/pages/") || !internalPath.endsWith(".xhtml")) {
            return false;
        }

        String relativePath = internalPath.substring("/pages/".length());
        int separatorIndex = relativePath.indexOf('/');
        if (separatorIndex <= 0) {
            return false;
        }

        String topLevelDirectory = relativePath.substring(0, separatorIndex);
        return !SHARED_PAGE_DIRECTORIES.contains(topLevelDirectory);
    }

    private String toFriendlyPath(String internalPath) {
        String relativePath = internalPath.substring("/pages/".length(), internalPath.length() - ".xhtml".length());
        return "/" + relativePath;
    }

    private String toApplicationFriendlyPath(String internalPath) {
        String relativePath = internalPath.substring("/pages/".length(), internalPath.length() - ".xhtml".length());
        int separatorIndex = relativePath.lastIndexOf('/');
        String pageName = separatorIndex >= 0 ? relativePath.substring(separatorIndex + 1) : relativePath;
        return pageName.isEmpty() ? "" : "/" + pageName;
    }

    private String resolveHomeTarget(HttpServletRequest request) {
        if (!isApplicationAdmin(request)) {
            return primaryApplicationDashboardPath == null ? CORE_DASHBOARD_FRIENDLY_PATH : primaryApplicationDashboardPath;
        }

        List<String> dashboardPaths = new ArrayList<>();
        dashboardPaths.add(CORE_DASHBOARD_FRIENDLY_PATH);
        if (primaryApplicationDashboardPath != null) {
            dashboardPaths.add(primaryApplicationDashboardPath);
        }

        if (dashboardPaths.isEmpty()) {
            return CORE_DASHBOARD_FRIENDLY_PATH;
        }

        javax.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            return dashboardPaths.get(0);
        }

        Object currentIndexValue = session.getAttribute(DASHBOARD_CYCLE_INDEX);
        int currentIndex = currentIndexValue instanceof Integer ? (Integer) currentIndexValue : -1;
        int nextIndex = (currentIndex + 1) % dashboardPaths.size();
        session.setAttribute(DASHBOARD_CYCLE_INDEX, nextIndex);
        return dashboardPaths.get(nextIndex);
    }

    private boolean isApplicationAdmin(HttpServletRequest request) {
        javax.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object userType = session.getAttribute(SessionAttributes.USER_TYPE.getName());
        return userType instanceof String
                && UserTypeConstants.APPLICATION_ADMIN == UserTypeConstants.fromValue((String) userType);
    }

    private List<String> discoverApplicationDashboardPaths(ServletContext servletContext) {
        Set<String> pageResources = new HashSet<>();
        collectPageResources(servletContext, "/pages/", pageResources);

        List<String> dashboardPaths = new ArrayList<>();
        for (String internalPath : pageResources) {
            if (!isDashboardPage(internalPath)) {
                continue;
            }
            String friendlyPath = toApplicationFriendlyPath(internalPath);
            if (friendlyPath == null || friendlyPath.isEmpty()) {
                friendlyPath = toFriendlyPath(internalPath);
            }
            if (friendlyPath != null && !friendlyPath.isEmpty()) {
                dashboardPaths.add(friendlyPath);
            }
        }
        Collections.sort(dashboardPaths);
        return dashboardPaths;
    }

    private String resolvePrimaryApplicationDashboardPath(List<String> dashboardPaths) {
        if (dashboardPaths == null || dashboardPaths.isEmpty()) {
            return null;
        }

        for (String dashboardPath : dashboardPaths) {
            if ("/dashboard".equals(dashboardPath)) {
                return dashboardPath;
            }
        }

        for (String dashboardPath : dashboardPaths) {
            if (dashboardPath != null && dashboardPath.endsWith("/dashboard")) {
                return dashboardPath;
            }
        }

        return dashboardPaths.get(0);
    }

    private boolean isDashboardPage(String internalPath) {
        if (internalPath == null || !internalPath.endsWith(".xhtml")) {
            return false;
        }
        if (CORE_DASHBOARD_INTERNAL_PATH.equals(internalPath)) {
            return false;
        }

        String normalizedPath = internalPath.toLowerCase();
        return normalizedPath.contains("dashboard")
                && !normalizedPath.contains("dashboardgateway");
    }
}
