package com.web.coretix.applicationmanagement;

import com.module.coretix.commonto.ClinicRegistrationRequestTO;
import com.module.coretix.commonto.ClinicRegistrationResultTO;
import com.module.coretix.coretix.IClinicRegistrationService;
import com.module.coretix.coretix.IRazorpayPaymentService;
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

public class RazorpayPaymentVerificationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayPaymentVerificationServlet.class);
    private static final int MAX_GENERIC_LENGTH = 255;

    private IRazorpayPaymentService razorpayPaymentService;
    private IClinicRegistrationService clinicRegistrationService;

    @Override
    public void init() throws ServletException {
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        razorpayPaymentService = context.getBean(IRazorpayPaymentService.class);
        clinicRegistrationService = context.getBean(IClinicRegistrationService.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyPrivacyHeaders(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        ClinicRegistrationRequestTO registrationRequest = new ClinicRegistrationRequestTO();
        registrationRequest.setClinicName(normalize(request.getParameter("clinicName")));
        registrationRequest.setAdminUserName(normalize(request.getParameter("adminUserName")));
        registrationRequest.setAdminPasswordHash(hashPassword(normalize(request.getParameter("adminPassword"))));
        registrationRequest.setAdminEmail(normalize(request.getParameter("adminEmail")));
        registrationRequest.setAdminPhone(normalize(request.getParameter("adminPhone")));
        registrationRequest.setCountryName(normalize(request.getParameter("countryName")));
        registrationRequest.setStateName(normalize(request.getParameter("stateName")));
        registrationRequest.setCityName(normalize(request.getParameter("cityName")));
        registrationRequest.setPostalCode(normalize(request.getParameter("postalCode")));
        registrationRequest.setAddressLine1(normalize(request.getParameter("addressLine1")));
        registrationRequest.setAddressLine2(normalize(request.getParameter("addressLine2")));
        registrationRequest.setWebsite(normalize(request.getParameter("website")));
        registrationRequest.setPlanCode(normalize(request.getParameter("planCode")));
        registrationRequest.setReferralCode(normalize(request.getParameter("referralCode")));
        registrationRequest.setPaymentGatewayCode("razorpay");
        registrationRequest.setPaymentOrderId(normalize(request.getParameter("razorpay_order_id")));
        registrationRequest.setPaymentId(normalize(request.getParameter("razorpay_payment_id")));
        registrationRequest.setPaymentSignature(normalize(request.getParameter("razorpay_signature")));
        registrationRequest.setPaymentAmount(normalize(request.getParameter("paymentAmount")));

        try {
            boolean verified = razorpayPaymentService.verifySignature(
                    registrationRequest.getPaymentOrderId(),
                    registrationRequest.getPaymentId(),
                    registrationRequest.getPaymentSignature());

            if (!verified) {
                writeJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"status\":\"error\",\"message\":\"Razorpay payment signature verification failed.\"}");
                return;
            }

            ClinicRegistrationResultTO result = clinicRegistrationService.registerClinic(registrationRequest);
            if (result.getStatus() == GeneralConstants.SUCCESSFUL) {
                writeJsonResponse(response, HttpServletResponse.SC_OK,
                        "{\"status\":\"ok\",\"message\":\"" + escapeJson(result.getMessage())
                                + "\",\"adminUserName\":\"" + escapeJson(result.getAdminUserName())
                                + "\",\"licenseEndDate\":\"" + escapeJson(result.getLicenseEndDate()) + "\"}");
                return;
            }

            int statusCode = result.getStatus() == GeneralConstants.ENTRY_ALREADY_EXISTS
                    ? HttpServletResponse.SC_CONFLICT
                    : HttpServletResponse.SC_BAD_REQUEST;
            writeJsonResponse(response, statusCode,
                    "{\"status\":\"error\",\"message\":\"" + escapeJson(result.getMessage()) + "\"}");
        } catch (Exception exception) {
            logger.error("Unable to verify Razorpay payment", exception);
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"status\":\"error\",\"message\":\"Unable to verify Razorpay payment.\"}");
        }
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String sanitized = input.trim().replace("|", "/").replace("\r", " ").replace("\n", " ");
        return sanitized.length() > MAX_GENERIC_LENGTH ? sanitized.substring(0, MAX_GENERIC_LENGTH) : sanitized;
    }

    private String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "";
        }
        return GenericManagedBean.hashPassword(password.trim());
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
