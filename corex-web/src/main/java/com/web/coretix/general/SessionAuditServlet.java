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
package com.web.coretix.general;

import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionAuditServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SessionAuditServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");

        if (session == null || session.getAttribute(SessionAttributes.USERNAME.getName()) == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        if ("heartbeat".equalsIgnoreCase(action)) {
            SessionAuditSupport.touchSession(session);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        if ("browser-close".equalsIgnoreCase(action)) {
            logger.debug("Received browser close audit for session {}", session.getId());
            // Browser lifecycle events (pagehide/beacon) are not reliable enough to
            // distinguish tab close from normal in-app navigation across all clients.
            // Do not invalidate here to avoid terminating active sessions mid-workflow.
            SessionAuditSupport.auditSessionTermination(session, LoginConstants.LOGOUT_SUCCESSFUL, "BROWSER_CLOSE", false);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported session audit action");
    }
}




