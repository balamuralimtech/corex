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

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.filter.FriendlyUrlFilter;
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
            "/pages/shipx/public/customer-request-form.xhtml");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        String login2URI = req.getContextPath() + "/landing";
        String loginURI = req.getContextPath() + "/login";
        String login2InternalURI = req.getContextPath() + "/login2.xhtml";
        String loginInternalURI = req.getContextPath() + "/login.xhtml";
        String loginVideoPath = System.getProperty("app.login.video", DEFAULT_LOGIN_VIDEO);
        String videoURI = req.getContextPath() + loginVideoPath;
        String requestPath = req.getRequestURI().substring(req.getContextPath().length());

        boolean loggedIn = (session != null && session.getAttribute(SessionAttributes.USERNAME.getName()) != null);
        boolean loginRequest = req.getRequestURI().equals(loginURI);
        boolean login2Request = req.getRequestURI().equals(login2URI);
        boolean loginInternalRequest = req.getRequestURI().equals(loginInternalURI);
        boolean login2InternalRequest = req.getRequestURI().equals(login2InternalURI);
        boolean videoRequest = req.getRequestURI().equals(videoURI);
        boolean publicFriendlyRequest = FriendlyUrlFilter.PUBLIC_FRIENDLY_PATHS.contains(requestPath);
        boolean publicInternalRequest = PUBLIC_INTERNAL_PATHS.contains(requestPath);
        boolean resourceRequest = req.getRequestURI().startsWith(req.getContextPath() + "/javax.faces.resource");

        if (loggedIn || loginRequest || login2Request || loginInternalRequest || login2InternalRequest
                || videoRequest || publicFriendlyRequest || publicInternalRequest || resourceRequest) {
            chain.doFilter(request, response); // User is logged in, so continue with the request.
        } else {
            res.sendRedirect(login2URI); // Not logged in, redirect to login page.
        }
    }

    @Override
    public void destroy() {
    }
}




