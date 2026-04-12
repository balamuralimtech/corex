package com.web.coretix.applicationmanagement;

import com.module.coretix.commonto.ClinicRegistrationRequestTO;
import com.module.coretix.commonto.ClinicRegistrationResultTO;
import com.module.coretix.coretix.IClinicRegistrationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.web.coretix.appgeneral.GenericManagedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ClinicRegistrationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ClinicRegistrationServlet.class);
    private static final int MAX_GENERIC_LENGTH = 150;
    private static final int MAX_ADDRESS_LENGTH = 255;
    private static final int MAX_PLAN_LENGTH = 40;

    private IClinicRegistrationService clinicRegistrationService;

    @Override
    public void init() throws ServletException {
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        clinicRegistrationService = context.getBean(IClinicRegistrationService.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyPrivacyHeaders(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        ClinicRegistrationRequestTO registrationRequest = new ClinicRegistrationRequestTO();
        registrationRequest.setClinicName(normalize(request.getParameter("clinicName"), MAX_GENERIC_LENGTH));
        registrationRequest.setAdminUserName(normalize(request.getParameter("adminUserName"), MAX_GENERIC_LENGTH));
        registrationRequest.setAdminPasswordHash(hashPassword(normalize(request.getParameter("adminPassword"), MAX_GENERIC_LENGTH)));
        registrationRequest.setAdminEmail(normalize(request.getParameter("adminEmail"), MAX_GENERIC_LENGTH));
        registrationRequest.setAdminPhone(normalize(request.getParameter("adminPhone"), MAX_GENERIC_LENGTH));
        registrationRequest.setCountryName(normalize(request.getParameter("countryName"), MAX_GENERIC_LENGTH));
        registrationRequest.setStateName(normalize(request.getParameter("stateName"), MAX_GENERIC_LENGTH));
        registrationRequest.setCityName(normalize(request.getParameter("cityName"), MAX_GENERIC_LENGTH));
        registrationRequest.setPostalCode(normalize(request.getParameter("postalCode"), MAX_GENERIC_LENGTH));
        registrationRequest.setAddressLine1(normalize(request.getParameter("addressLine1"), MAX_ADDRESS_LENGTH));
        registrationRequest.setAddressLine2(normalize(request.getParameter("addressLine2"), MAX_ADDRESS_LENGTH));
        registrationRequest.setWebsite(normalize(request.getParameter("website"), MAX_GENERIC_LENGTH));
        registrationRequest.setPlanCode(normalize(request.getParameter("planCode"), MAX_PLAN_LENGTH));
        registrationRequest.setPaymentGatewayCode(normalize(request.getParameter("paymentGatewayCode"), MAX_PLAN_LENGTH));
        registrationRequest.setReferralCode(normalize(request.getParameter("referralCode"), MAX_GENERIC_LENGTH));

        if (registrationRequest.getClinicName().isEmpty()
                || registrationRequest.getAdminUserName().isEmpty()
                || registrationRequest.getAdminEmail().isEmpty()
                || registrationRequest.getAdminPasswordHash().isEmpty()
                || registrationRequest.getPlanCode().isEmpty()) {
            writeJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"status\":\"error\",\"message\":\"Clinic, admin login, email, password, and plan are required.\"}");
            return;
        }

        try {
            ClinicRegistrationResultTO result = clinicRegistrationService.registerClinic(registrationRequest);
            if (result.getStatus() == GeneralConstants.SUCCESSFUL) {
                writeJsonResponse(response, HttpServletResponse.SC_OK,
                        "{\"status\":\"ok\",\"message\":\"" + escapeJson(result.getMessage())
                                + "\",\"adminUserName\":\"" + escapeJson(result.getAdminUserName())
                                + "\",\"licenseEndDate\":\"" + escapeJson(result.getLicenseEndDate())
                                + "\",\"paymentGateway\":\"" + escapeJson(result.getPaymentGatewayLabel()) + "\"}");
                return;
            }

            int statusCode = result.getStatus() == GeneralConstants.ENTRY_ALREADY_EXISTS
                    ? HttpServletResponse.SC_CONFLICT
                    : HttpServletResponse.SC_BAD_REQUEST;
            writeJsonResponse(response, statusCode,
                    "{\"status\":\"error\",\"message\":\"" + escapeJson(result.getMessage()) + "\"}");
        } catch (Exception exception) {
            logger.error("Unable to register clinic", exception);
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"status\":\"error\",\"message\":\"Unable to register the clinic right now.\"}");
        }
    }

    private String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "";
        }
        return GenericManagedBean.hashPassword(password.trim());
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

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
