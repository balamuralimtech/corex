package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.RazorpayOrderRequestTO;
import com.module.coretix.commonto.RazorpayOrderResultTO;
import com.module.coretix.coretix.IApplicationPricingService;
import com.module.coretix.coretix.IRazorpayPaymentService;
import com.persist.coretix.modal.coretix.ApplicationPricing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Named;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
@Transactional(readOnly = true)
public class RazorpayPaymentService implements IRazorpayPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayPaymentService.class);
    private static final Pattern JSON_STRING_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern JSON_NUMBER_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*(\\d+)");

    @javax.inject.Inject
    private IApplicationPricingService applicationPricingService;

    @Override
    public RazorpayOrderResultTO createOrder(RazorpayOrderRequestTO request) {
        RazorpayOrderResultTO result = new RazorpayOrderResultTO();

        if (!isConfigured()) {
            result.setMessage("Razorpay keys are not configured on the server.");
            return result;
        }

        try {
            long amountInPaise = resolveAmountInPaise(request);
            String currency = getCurrency();
            String orderPayload = buildOrderPayload(request, amountInPaise, currency);
            String credentials = getKeyId() + ":" + getKeySecret();
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/orders"))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(orderPayload))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error("Razorpay order creation failed. Status={} Body={}", response.statusCode(), response.body());
                result.setMessage("Unable to create Razorpay order.");
                return result;
            }

            String responseBody = response.body();
            result.setSuccessful(true);
            result.setOrderId(extractJsonString(responseBody, "id"));
            result.setAmount(String.valueOf(extractJsonNumber(responseBody, "amount")));
            result.setCurrency(extractJsonString(responseBody, "currency"));
            result.setKeyId(getKeyId());
            result.setBusinessName(getBusinessName());
            result.setDescription(buildDescription(request));
            result.setMessage("Razorpay order created.");
            return result;
        } catch (IllegalArgumentException exception) {
            logger.warn("Razorpay order validation failed", exception);
            result.setMessage(exception.getMessage());
            return result;
        } catch (Exception exception) {
            logger.error("Unable to create Razorpay order", exception);
            result.setMessage("Unable to create Razorpay order.");
            return result;
        }
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (!isConfigured() || isBlank(orderId) || isBlank(paymentId) || isBlank(signature)) {
            return false;
        }

        try {
            String payload = orderId + "|" + paymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = toHex(hash);
            return generatedSignature.equals(signature);
        } catch (Exception exception) {
            logger.error("Unable to verify Razorpay signature", exception);
            return false;
        }
    }

    @Override
    public boolean isConfigured() {
        return !isBlank(getKeyId()) && !isBlank(getKeySecret());
    }

    private String buildOrderPayload(RazorpayOrderRequestTO request, long amountInPaise, String currency) {
        String receipt = sanitizeForReceipt(request.getClinicName()) + "_" + System.currentTimeMillis();
        return "{"
                + "\"amount\":" + amountInPaise + ","
                + "\"currency\":\"" + escapeJson(currency) + "\","
                + "\"receipt\":\"" + escapeJson(receipt.substring(0, Math.min(receipt.length(), 40))) + "\","
                + "\"notes\":{"
                + "\"clinic\":\"" + escapeJson(safeValue(request.getClinicName())) + "\","
                + "\"plan\":\"" + escapeJson(safeValue(request.getPlanCode())) + "\","
                + "\"country\":\"" + escapeJson(safeValue(request.getCountryCode())) + "\""
                + "}"
                + "}";
    }

    private String buildDescription(RazorpayOrderRequestTO request) {
        return "CareX " + safeValue(request.getPlanCode()).replace('_', ' ') + " subscription";
    }

    private long resolveAmountInPaise(RazorpayOrderRequestTO request) {
        ApplicationPricing applicationPricing = applicationPricingService.getApplicationPricingByApplicationAndCountry(
                resolveApplicationCode(), resolveCountryCode(request));
        if (applicationPricing == null) {
            throw new IllegalArgumentException("Pricing is not configured for this application and country.");
        }
        java.math.BigDecimal price = resolvePlanPrice(applicationPricing, request.getPlanCode());
        if (price == null || price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Selected plan price is not configured.");
        }
        return price.movePointRight(2).longValue();
    }

    private java.math.BigDecimal resolvePlanPrice(ApplicationPricing applicationPricing, String planCode) {
        if ("monthly".equalsIgnoreCase(safeValue(planCode))) {
            return applicationPricing.getOneMonthPrice();
        }
        if ("six_months".equalsIgnoreCase(safeValue(planCode))) {
            return applicationPricing.getSixMonthPrice();
        }
        if ("yearly".equalsIgnoreCase(safeValue(planCode))) {
            return applicationPricing.getOneYearPrice();
        }
        throw new IllegalArgumentException("Please select a valid pricing plan.");
    }

    private String sanitizeForReceipt(String input) {
        String value = safeValue(input).replaceAll("[^A-Za-z0-9]", "");
        return value.isEmpty() ? "carex" : value;
    }

    private String extractJsonString(String json, String key) {
        Matcher matcher = Pattern.compile(String.format(Locale.ENGLISH, JSON_STRING_PATTERN.pattern(), key)).matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private long extractJsonNumber(String json, String key) {
        Matcher matcher = Pattern.compile(String.format(Locale.ENGLISH, JSON_NUMBER_PATTERN.pattern(), key)).matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private String getBaseUrl() {
        return System.getProperty("razorpay.api.base-url", "https://api.razorpay.com/v1");
    }

    private String getKeyId() {
        return System.getProperty("razorpay.key.id", "").trim();
    }

    private String getKeySecret() {
        return System.getProperty("razorpay.key.secret", "").trim();
    }

    private String getCurrency() {
        String currency = System.getProperty("razorpay.currency", "INR").trim();
        return currency.isEmpty() ? "INR" : currency;
    }

    private String getBusinessName() {
        String businessName = System.getProperty("razorpay.business.name", "CareX").trim();
        return businessName.isEmpty() ? "CareX" : businessName;
    }

    private String resolveApplicationCode() {
        String applicationCode = System.getProperty("app.context", "").trim();
        return applicationCode.isEmpty() ? "carex" : applicationCode;
    }

    private String resolveCountryCode(RazorpayOrderRequestTO request) {
        String countryCode = safeValue(request.getCountryCode());
        return countryCode.isEmpty() ? "IN" : countryCode.toUpperCase(Locale.ENGLISH);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String safeValue(String input) {
        return input == null ? "" : input.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
