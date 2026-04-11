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
package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IDemoRequestService;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.DemoRequest;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DemoRequestServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DemoRequestServlet.class);
    private static final int MAX_CLINIC_LENGTH = 150;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_NOTES_LENGTH = 1000;
    private static final String NOTIFICATION_CREATED_BY = "PUBLIC_DEMO_REQUEST";

    private IDemoRequestService demoRequestService;
    private IApplicationNotificationService applicationNotificationService;

    @Override
    public void init() throws ServletException {
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        demoRequestService = context.getBean(IDemoRequestService.class);
        applicationNotificationService = context.getBean(IApplicationNotificationService.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyPrivacyHeaders(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String clinicName = normalize(request.getParameter("clinicName"), MAX_CLINIC_LENGTH);
        String workEmail = normalize(request.getParameter("workEmail"), MAX_EMAIL_LENGTH);
        String notes = normalize(request.getParameter("notes"), MAX_NOTES_LENGTH);

        if (clinicName.isEmpty() || workEmail.isEmpty()) {
            if (isHtmlFormPost(request)) {
                response.sendRedirect(request.getContextPath() + "/landing?demo=error#contact");
                return;
            }
            writeJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"error\",\"message\":\"Clinic name and work email are required.\"}");
            return;
        }

        DemoRequest demoRequest = new DemoRequest();
        demoRequest.setClinicName(clinicName);
        demoRequest.setWorkEmail(workEmail);
        demoRequest.setNotes(notes);

        GeneralConstants result = demoRequestService.addDemoRequest(demoRequest);
        if (result == GeneralConstants.SUCCESSFUL) {
            publishAdminNotification(clinicName, workEmail);
            if (isHtmlFormPost(request)) {
                response.sendRedirect(request.getContextPath() + "/landing?demo=success#contact");
                return;
            }
            writeJsonResponse(response, HttpServletResponse.SC_OK,
                    "{\"status\":\"ok\",\"message\":\"Demo request submitted.\"}");
            return;
        }

        logger.error("Unable to persist demo request. Result={}", result);
        if (isHtmlFormPost(request)) {
            response.sendRedirect(request.getContextPath() + "/landing?demo=error#contact");
            return;
        }
        writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "{\"status\":\"error\",\"message\":\"Unable to submit demo request now.\"}");
    }

    private boolean isHtmlFormPost(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith == null || requestedWith.trim().isEmpty();
    }

    private void publishAdminNotification(String clinicName, String workEmail) {
        try {
            ApplicationNotification applicationNotification = new ApplicationNotification();
            applicationNotification.setCreatedByUserName(NOTIFICATION_CREATED_BY);
            applicationNotification.setMessage("New demo request from " + clinicName + " (" + workEmail + ")");
            applicationNotificationService.addPublicApplicationNotification(applicationNotification);
        } catch (Exception exception) {
            logger.error("Unable to create application notification for demo request", exception);
        }
    }

    private String normalize(String input, int maxLength) {
        if (input == null) {
            return "";
        }

        String sanitized = input.trim()
                .replace("|", "/")
                .replace("\r", " ")
                .replace("\n", " ");

        return sanitized.length() > maxLength ? sanitized.substring(0, maxLength) : sanitized;
    }

    private void writeJsonResponse(HttpServletResponse response, int status, String payload) throws IOException {
        response.setStatus(status);
        response.getWriter().write(payload);
    }

    private void applyPrivacyHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Content-Type-Options", "nosniff");
    }
}
