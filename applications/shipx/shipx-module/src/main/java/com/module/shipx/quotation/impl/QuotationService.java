package com.module.shipx.quotation.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.INotificationSettingService;
import com.module.shipx.quotation.IQuotationService;
import com.module.shipx.quotation.model.QuotationCostLine;
import com.module.shipx.quotation.model.QuotationCostSection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.NotificationSettings;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import com.persist.shipx.quotation.Quotation;
import com.persist.shipx.quotation.dao.IQuotationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Named
@Transactional(readOnly = true)
public class QuotationService implements IQuotationService {

    private static final Logger logger = LoggerFactory.getLogger(QuotationService.class);
    private static final Gson GSON = new Gson();

    @Inject
    private IQuotationDAO quotationDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Inject
    private INotificationSettingService notificationSettingService;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addQuotation(UserActivityTO userActivityTO, Quotation quotation) {
        GeneralConstants result = quotationDAO.addQuotation(quotation);
        userActivityTO.setActivityDescription("Quotation added - " + result.getName() + " - " + quotation.getQuotationReference());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updateQuotation(UserActivityTO userActivityTO, Quotation quotation) {
        GeneralConstants result = quotationDAO.updateQuotation(quotation);
        userActivityTO.setActivityDescription("Quotation updated - " + result.getName() + " - " + quotation.getQuotationReference());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteQuotation(UserActivityTO userActivityTO, Quotation quotation) {
        GeneralConstants result = quotationDAO.deleteQuotation(quotation);
        userActivityTO.setActivityDescription("Quotation deleted - " + result.getName() + " - " + quotation.getQuotationReference());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants sendQuotationEmail(UserActivityTO userActivityTO, Integer organizationId, Quotation quotation) {
        Quotation persistentQuotation = quotationDAO.getQuotationById(quotation.getId());
        if (persistentQuotation == null) {
            userActivityTO.setActivityDescription("Quotation email send failed - quotation missing");
            addUserActivity(userActivityTO);
            return GeneralConstants.ENTRY_NOT_EXISTS;
        }

        NotificationSettings notificationSettings = notificationSettingService.getNotificationSettingByOrganizationId(
                organizationId == null ? 0 : organizationId);
        if (notificationSettings == null) {
            logger.warn("SMTP notification settings not configured for organization {}", organizationId);
            userActivityTO.setActivityDescription("Quotation email send failed - SMTP not configured - "
                    + persistentQuotation.getQuotationReference());
            addUserActivity(userActivityTO);
            return GeneralConstants.FAILED;
        }

        try {
            sendMail(notificationSettings, persistentQuotation);
            persistentQuotation.setStatus("SENT");
            persistentQuotation.setSentAt(new Timestamp(System.currentTimeMillis()));
            persistentQuotation.setSentToEmail(persistentQuotation.getRecipientEmail());
            persistentQuotation.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            GeneralConstants result = quotationDAO.updateQuotation(persistentQuotation);
            userActivityTO.setActivityDescription("Quotation emailed - " + result.getName() + " - "
                    + persistentQuotation.getQuotationReference());
            addUserActivity(userActivityTO);
            return result;
        } catch (Exception exception) {
            logger.error("Failed to send quotation email {}", persistentQuotation.getQuotationReference(), exception);
            userActivityTO.setActivityDescription("Quotation email send failed - "
                    + persistentQuotation.getQuotationReference());
            addUserActivity(userActivityTO);
            return GeneralConstants.FAILED;
        }
    }

    @Override
    public List<Quotation> getQuotationList() {
        return quotationDAO.getQuotationList();
    }

    @Override
    public Quotation getQuotationById(Integer id) {
        return quotationDAO.getQuotationById(id);
    }

    private void sendMail(NotificationSettings notificationSettings, Quotation quotation) throws Exception {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", String.valueOf(notificationSettings.isSmtpAuth()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(notificationSettings.isSmtpStarttlsEnable()));
        properties.put("mail.smtp.host", notificationSettings.getSmtpHost());
        properties.put("mail.smtp.port", notificationSettings.getSmtpPort());

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(notificationSettings.getEmailId(), notificationSettings.getPassword());
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(notificationSettings.getEmailId()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(quotation.getRecipientEmail(), false));
        message.setSubject(resolveEmailSubject(quotation), StandardCharsets.UTF_8.name());
        message.setContent(resolveEmailBody(quotation), "text/plain; charset=UTF-8");

        Transport.send(message);
    }

    private String resolveEmailSubject(Quotation quotation) {
        if (quotation.getEmailSubject() != null && !quotation.getEmailSubject().trim().isEmpty()) {
            return quotation.getEmailSubject().trim();
        }
        return "Quotation " + quotation.getQuotationReference() + " from ShipX";
    }

    private String resolveEmailBody(Quotation quotation) {
        if (quotation.getEmailBody() != null && !quotation.getEmailBody().trim().isEmpty()) {
            return quotation.getEmailBody().trim();
        }

        String validUntil = quotation.getValidUntil() == null
                ? "N/A"
                : new SimpleDateFormat("dd-MMM-yyyy").format(quotation.getValidUntil());
        String costingBreakdown = buildCostingBreakdown(quotation);
        String noteLines = buildNoteLines(quotation);

        return "Dear " + quotation.getCustomerName() + ",\n\n"
                + "Please find below the quotation details from ShipX.\n\n"
                + "Quotation Reference: " + quotation.getQuotationReference() + "\n"
                + "Service: " + quotation.getServiceCategory() + "\n"
                + "Origin: " + valueOrDefault(quotation.getOriginLocation()) + "\n"
                + "Destination: " + valueOrDefault(quotation.getDestinationLocation()) + "\n"
                + "Cargo Summary: " + valueOrDefault(quotation.getCargoSummary()) + "\n"
                + "Totals: " + valueOrDefault(quotation.getTotalSummaryLabel()) + "\n"
                + "Valid Until: " + validUntil + "\n\n"
                + costingBreakdown
                + noteLines
                + "\n"
                + "Regards,\nShipX Commercial Desk";
    }

    private String buildCostingBreakdown(Quotation quotation) {
        List<QuotationCostSection> sections = deserializeSections(quotation.getPricingBreakdownJson());
        if (sections.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("Costing Breakdown:\n");
        for (QuotationCostSection section : sections) {
            if (section.getTitle() != null && !section.getTitle().trim().isEmpty()) {
                builder.append("\n").append(section.getTitle().trim()).append("\n");
            }
            for (QuotationCostLine line : section.getLines()) {
                if (line == null || isBlank(line.getDescription())) {
                    continue;
                }
                builder.append("- ")
                        .append(line.getDescription().trim());
                if (!isBlank(line.getUnit())) {
                    builder.append(" [").append(line.getUnit().trim()).append("]");
                }
                if (line.getQuantity() != null) {
                    builder.append(" x ").append(line.getQuantity().stripTrailingZeros().toPlainString());
                }
                if (line.getUnitPrice() != null) {
                    builder.append(" @ ").append(line.getUnitPrice().stripTrailingZeros().toPlainString());
                }
                BigDecimal lineAmount = resolveLineAmount(line);
                if (!isBlank(line.getCurrencyCode()) || lineAmount != null) {
                    builder.append(": ")
                            .append(valueOrDefault(line.getCurrencyCode()))
                            .append(lineAmount == null ? "" : " " + lineAmount.stripTrailingZeros().toPlainString());
                }
                if (!isBlank(line.getBasis())) {
                    builder.append(" (").append(line.getBasis().trim()).append(")");
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private String buildNoteLines(Quotation quotation) {
        List<String> notes = deserializeNotes(quotation.getNoteLinesJson());
        List<String> sanitizedNotes = notes.stream()
                .filter(note -> !isBlank(note))
                .map(String::trim)
                .collect(Collectors.toList());
        if (sanitizedNotes.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("\nNotes:\n");
        for (String note : sanitizedNotes) {
            builder.append("- ").append(note).append("\n");
        }
        return builder.toString();
    }

    private List<QuotationCostSection> deserializeSections(String json) {
        if (isBlank(json)) {
            return java.util.Collections.emptyList();
        }
        return GSON.fromJson(json, new TypeToken<List<QuotationCostSection>>() { }.getType());
    }

    private List<String> deserializeNotes(String json) {
        if (isBlank(json)) {
            return java.util.Collections.emptyList();
        }
        return GSON.fromJson(json, new TypeToken<List<String>>() { }.getType());
    }

    private String valueOrDefault(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private BigDecimal resolveLineAmount(QuotationCostLine line) {
        if (line == null) {
            return null;
        }
        if (line.getQuantity() != null && line.getUnitPrice() != null) {
            return line.getQuantity().multiply(line.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        }
        return line.getAmount();
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        if (userActivityTO == null) {
            return;
        }

        UserActivities userActivity = new UserActivities();
        userActivity.setUserId(userActivityTO.getUserId());
        userActivity.setUserName(userActivityTO.getUserName());
        userActivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        userActivity.setIpAddress(userActivityTO.getIpAddress());
        userActivity.setLocationInfo(userActivityTO.getLocationInfo());
        userActivity.setActivityType(userActivityTO.getActivityType());
        userActivity.setActivityDescription(userActivityTO.getActivityDescription());
        userActivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userActivityDAO.addUserActivity(userActivity);
    }
}
