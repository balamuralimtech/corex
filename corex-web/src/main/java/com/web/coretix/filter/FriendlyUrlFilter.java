package com.web.coretix.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FriendlyUrlFilter implements Filter {

    public static final Map<String, String> FRIENDLY_TO_INTERNAL_PATHS = buildFriendlyToInternalPaths();
    public static final Map<String, String> INTERNAL_TO_FRIENDLY_PATHS = buildInternalToFriendlyPaths();
    public static final java.util.Set<String> PUBLIC_FRIENDLY_PATHS = java.util.Set.of(
            "/",
            "/landing",
            "/login",
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestUri.substring(contextPath.length());

        String internalPath = FRIENDLY_TO_INTERNAL_PATHS.get(path);
        if (internalPath != null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(internalPath);
            dispatcher.forward(request, response);
            return;
        }

        String friendlyPath = INTERNAL_TO_FRIENDLY_PATHS.get(path);
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

        paths.put("/", "/login2.xhtml");
        paths.put("/landing", "/login2.xhtml");
        paths.put("/login", "/login.xhtml");
        paths.put("/home", "/pages/home/homePage.xhtml");

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

        paths.put("/license", "/pages/license/license.xhtml");
        paths.put("/server-logs", "/pages/serverlogs/serverlogs.xhtml");
        paths.put("/database-details", "/pages/serverlogs/databaseDetails.xhtml");

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

    private static Map<String, String> buildInternalToFriendlyPaths() {
        Map<String, String> paths = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : FRIENDLY_TO_INTERNAL_PATHS.entrySet()) {
            paths.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(paths);
    }
}
