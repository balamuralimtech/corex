package com.web.coretix.applicationmanagement;

import com.module.coretix.commonto.RazorpayOrderRequestTO;
import com.module.coretix.commonto.RazorpayOrderResultTO;
import com.module.coretix.coretix.IRazorpayPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RazorpayOrderServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayOrderServlet.class);
    private static final int MAX_GENERIC_LENGTH = 150;

    private IRazorpayPaymentService razorpayPaymentService;

    @Override
    public void init() throws ServletException {
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        razorpayPaymentService = context.getBean(IRazorpayPaymentService.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyPrivacyHeaders(response);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        RazorpayOrderRequestTO orderRequest = new RazorpayOrderRequestTO();
        orderRequest.setClinicName(normalize(request.getParameter("clinicName")));
        orderRequest.setAdminEmail(normalize(request.getParameter("adminEmail")));
        orderRequest.setAdminPhone(normalize(request.getParameter("adminPhone")));
        orderRequest.setPlanCode(normalize(request.getParameter("planCode")));
        orderRequest.setCountryCode("IN");

        try {
            RazorpayOrderResultTO result = razorpayPaymentService.createOrder(orderRequest);
            if (!result.isSuccessful()) {
                writeJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"status\":\"error\",\"message\":\"" + escapeJson(result.getMessage()) + "\"}");
                return;
            }

            writeJsonResponse(response, HttpServletResponse.SC_OK,
                    "{\"status\":\"ok\",\"orderId\":\"" + escapeJson(result.getOrderId())
                            + "\",\"keyId\":\"" + escapeJson(result.getKeyId())
                            + "\",\"amount\":\"" + escapeJson(result.getAmount())
                            + "\",\"currency\":\"" + escapeJson(result.getCurrency())
                            + "\",\"businessName\":\"" + escapeJson(result.getBusinessName())
                            + "\",\"description\":\"" + escapeJson(result.getDescription()) + "\"}");
        } catch (Exception exception) {
            logger.error("Unable to create Razorpay order", exception);
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"status\":\"error\",\"message\":\"Unable to create Razorpay order.\"}");
        }
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String sanitized = input.trim().replace("|", "/").replace("\r", " ").replace("\n", " ");
        return sanitized.length() > MAX_GENERIC_LENGTH ? sanitized.substring(0, MAX_GENERIC_LENGTH) : sanitized;
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
