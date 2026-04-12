package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.ClinicRegistrationRequestTO;
import com.module.coretix.commonto.ClinicRegistrationResultTO;
import com.module.coretix.commonto.ReferralResolutionTO;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.module.coretix.coretix.IClinicRegistrationService;
import com.module.coretix.coretix.IApplicationPricingService;
import com.module.coretix.coretix.IReferralManagementService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.persist.coretix.modal.coretix.ApplicationPricing;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.usermanagement.Roles;
import com.persist.coretix.modal.usermanagement.UserDetails;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Named
@Transactional(readOnly = true)
public class ClinicRegistrationService implements IClinicRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicRegistrationService.class);
    private static final String DEFAULT_COUNTRY_NAME = "India";
    private static final String DEFAULT_ROLE_NAME = "CareX Clinic Admin";
    private static final String GENERAL_USER_TYPE = "GENERAL_USER";
    private static final int ORGANIZATION_ACCESS_RIGHT_ID = 1;
    private static final int NEVER_LOGGED_IN_STATUS_ID = 3;
    private static final DateTimeFormatter LICENSE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Inject
    private SessionFactory sessionFactory;

    @Inject
    private IApplicationNotificationService applicationNotificationService;

    @Inject
    private IApplicationPricingService applicationPricingService;

    @Inject
    private IReferralManagementService referralManagementService;

    @Override
    @Transactional(readOnly = false)
    public ClinicRegistrationResultTO registerClinic(ClinicRegistrationRequestTO request) {
        ClinicRegistrationResultTO result = new ClinicRegistrationResultTO();

        if (request == null) {
            return failure(result, "Clinic registration payload is missing.");
        }

        Session session = sessionFactory.getCurrentSession();

        try {
            String clinicName = safeValue(request.getClinicName());
            String adminUserName = safeValue(request.getAdminUserName());
            String adminEmail = safeValue(request.getAdminEmail());
            String planCode = safeValue(request.getPlanCode());

            if (clinicName.isEmpty() || adminUserName.isEmpty() || adminEmail.isEmpty()
                    || safeValue(request.getAdminPasswordHash()).isEmpty() || planCode.isEmpty()) {
                return failure(result, "Clinic, admin login, email, password, and plan are required.");
            }

            if (findOrganizationByName(session, clinicName) != null) {
                result.setStatus(GeneralConstants.ENTRY_ALREADY_EXISTS);
                result.setMessage("A clinic with this name already exists.");
                return result;
            }

            if (findUserByUserName(session, adminUserName) != null) {
                result.setStatus(GeneralConstants.ENTRY_ALREADY_EXISTS);
                result.setMessage("The admin login is already in use.");
                return result;
            }

            if (findUserByEmail(session, adminEmail) != null) {
                result.setStatus(GeneralConstants.ENTRY_ALREADY_EXISTS);
                result.setMessage("The admin email is already linked to another user.");
                return result;
            }

            Countries country = resolveCountry(session, request.getCountryName());
            if (country == null) {
                return failure(result, "Unable to resolve the selected country.");
            }

            Roles clinicRole = resolveClinicRole(session);
            ReferralResolutionTO referralResolutionTO = referralManagementService.resolveReferralCode(request.getReferralCode());
            Organizations organization = createOrganization(session, request, country);
            LicenseCreationResult licenseCreationResult = createLicense(session, organization, request, referralResolutionTO);
            createClinicAdminUser(session, request, organization, country, clinicRole);
            referralManagementService.createReferralAttribution(referralResolutionTO, organization.getId(),
                    request.getPlanCode(), licenseCreationResult.getSubscriptionAmount(),
                    request.getPaymentGatewayCode(), request.getPaymentOrderId(), request.getPaymentId());
            session.flush();

            publishNotification(request, licenseCreationResult.getLicenseEndDate(), referralResolutionTO);

            result.setStatus(GeneralConstants.SUCCESSFUL);
            result.setMessage("Clinic registration completed. You can now log in with the admin account.");
            result.setAdminUserName(adminUserName);
            result.setLicenseEndDate(LICENSE_DATE_FORMATTER.format(licenseCreationResult.getLicenseEndDate()));
            result.setPaymentGatewayLabel(resolveGatewayLabel(request.getPaymentGatewayCode()));
            return result;
        } catch (IllegalArgumentException exception) {
            logger.warn("Clinic registration validation failed", exception);
            return failure(result, exception.getMessage());
        } catch (Exception exception) {
            logger.error("Clinic registration failed", exception);
            return failure(result, "Unable to register the clinic right now.");
        }
    }

    private Organizations createOrganization(Session session, ClinicRegistrationRequestTO request, Countries country) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Organizations organization = new Organizations();
        organization.setOrganizationName(safeValue(request.getClinicName()));
        organization.setCountry(country);
        organization.setState(safeValue(request.getStateName()));
        organization.setCity(safeValue(request.getCityName()));
        organization.setPostalCode(safeValue(request.getPostalCode()));
        organization.setAddressLine1(safeValue(request.getAddressLine1()));
        organization.setAddressLine2(safeValue(request.getAddressLine2()));
        organization.setPhoneNumber(safeValue(request.getAdminPhone()));
        organization.setEmail(safeValue(request.getAdminEmail()));
        organization.setWebsite(safeValue(request.getWebsite()));
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);

        session.save(organization);
        return organization;
    }

    private LicenseCreationResult createLicense(Session session, Organizations organization, ClinicRegistrationRequestTO request,
            ReferralResolutionTO referralResolutionTO) {
        LicensePlan plan = LicensePlan.fromCode(request.getPlanCode());
        LocalDate startDate = LocalDate.now();
        int extraFreeMonths = referralResolutionTO != null ? referralResolutionTO.getExtraFreeMonths() : 0;
        LocalDate endDate = startDate.plusMonths(plan.getBillableMonths() + plan.getFreeMonths() + extraFreeMonths).minusDays(1);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ApplicationPricing applicationPricing = applicationPricingService.getApplicationPricingByApplicationAndCountry("carex", "IN");
        java.math.BigDecimal subscriptionAmount = resolvePlanAmount(applicationPricing, request.getPlanCode());

        Licenses license = new Licenses();
        license.setOrganization(organization);
        license.setStartDate(Date.valueOf(startDate));
        license.setEndDate(Date.valueOf(endDate));
        license.setRemarks(buildLicenseRemarks(plan, request, extraFreeMonths));
        license.setCreatedAt(now);
        license.setUpdatedAt(now);

        session.save(license);
        return new LicenseCreationResult(endDate, subscriptionAmount == null ? java.math.BigDecimal.ZERO : subscriptionAmount);
    }

    private void createClinicAdminUser(Session session, ClinicRegistrationRequestTO request, Organizations organization,
            Countries country, Roles clinicRole) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        UserDetails userDetails = new UserDetails();
        userDetails.setUserName(safeValue(request.getAdminUserName()));
        userDetails.setPassword(safeValue(request.getAdminPasswordHash()));
        userDetails.setEmailId(safeValue(request.getAdminEmail()));
        userDetails.setContact(safeValue(request.getAdminPhone()));
        userDetails.setUserType(GENERAL_USER_TYPE);
        userDetails.setRole(clinicRole);
        userDetails.setOrganization(organization);
        userDetails.setCountry(country);
        userDetails.setState(null);
        userDetails.setCity(null);
        userDetails.setBranch(null);
        userDetails.setAddress(joinAddressLines(request.getAddressLine1(), request.getAddressLine2()));
        userDetails.setAccessRight(ORGANIZATION_ACCESS_RIGHT_ID);
        userDetails.setStatus(NEVER_LOGGED_IN_STATUS_ID);
        userDetails.setCreatedAt(now);
        userDetails.setUpdatedAt(now);

        session.save(userDetails);
    }

    private Roles resolveClinicRole(Session session) {
        Roles role = findRoleByName(session, DEFAULT_ROLE_NAME);
        if (role != null) {
            return role;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Roles clinicRole = new Roles();
        clinicRole.setRoleName(DEFAULT_ROLE_NAME);
        clinicRole.setCreatedAt(now);
        clinicRole.setUpdatedAt(now);
        session.save(clinicRole);
        return clinicRole;
    }

    private Countries resolveCountry(Session session, String requestedCountryName) {
        String requested = safeValue(requestedCountryName);
        if (!requested.isEmpty()) {
            Countries exactCountry = findCountryByName(session, requested);
            if (exactCountry != null) {
                return exactCountry;
            }
        }
        return findCountryByName(session, DEFAULT_COUNTRY_NAME);
    }

    private Countries findCountryByName(Session session, String countryName) {
        List<Countries> countries = session.createQuery(
                        "from Countries where lower(name) = :name", Countries.class)
                .setParameter("name", countryName.toLowerCase(Locale.ENGLISH))
                .setMaxResults(1)
                .list();
        return countries.isEmpty() ? null : countries.get(0);
    }

    private Organizations findOrganizationByName(Session session, String clinicName) {
        List<Organizations> organizations = session.createQuery(
                        "from Organizations where lower(organizationName) = :clinicName", Organizations.class)
                .setParameter("clinicName", clinicName.toLowerCase(Locale.ENGLISH))
                .setMaxResults(1)
                .list();
        return organizations.isEmpty() ? null : organizations.get(0);
    }

    private UserDetails findUserByUserName(Session session, String userName) {
        List<UserDetails> users = session.createQuery(
                        "from UserDetails where lower(userName) = :userName", UserDetails.class)
                .setParameter("userName", userName.toLowerCase(Locale.ENGLISH))
                .setMaxResults(1)
                .list();
        return users.isEmpty() ? null : users.get(0);
    }

    private UserDetails findUserByEmail(Session session, String emailId) {
        List<UserDetails> users = session.createQuery(
                        "from UserDetails where lower(emailId) = :emailId", UserDetails.class)
                .setParameter("emailId", emailId.toLowerCase(Locale.ENGLISH))
                .setMaxResults(1)
                .list();
        return users.isEmpty() ? null : users.get(0);
    }

    private Roles findRoleByName(Session session, String roleName) {
        List<Roles> roles = session.createQuery(
                        "from Roles where lower(roleName) = :roleName", Roles.class)
                .setParameter("roleName", roleName.toLowerCase(Locale.ENGLISH))
                .setMaxResults(1)
                .list();
        return roles.isEmpty() ? null : roles.get(0);
    }

    private String buildLicenseRemarks(LicensePlan plan, ClinicRegistrationRequestTO request, int extraFreeMonths) {
        return plan.getLabel() + " plan"
                + (extraFreeMonths > 0 ? " | Referral bonus months: " + extraFreeMonths : "")
                + " | Payment gateway: " + resolveGatewayLabel(request.getPaymentGatewayCode())
                + appendPaymentReference(request);
    }

    private String appendPaymentReference(ClinicRegistrationRequestTO request) {
        String paymentId = safeValue(request.getPaymentId());
        String orderId = safeValue(request.getPaymentOrderId());
        String amount = safeValue(request.getPaymentAmount());
        if (paymentId.isEmpty() && orderId.isEmpty() && amount.isEmpty()) {
            return "";
        }
        return " | Payment order: " + orderId + " | Payment id: " + paymentId + " | Amount: " + amount;
    }

    private void publishNotification(ClinicRegistrationRequestTO request, LocalDate licenseEndDate, ReferralResolutionTO referralResolutionTO) {
        try {
            ApplicationNotification notification = new ApplicationNotification();
            notification.setCreatedByUserName("PUBLIC_CLINIC_REGISTRATION");
            notification.setMessage("New clinic registration: " + safeValue(request.getClinicName())
                    + " | Admin: " + safeValue(request.getAdminUserName())
                    + " | Plan: " + LicensePlan.fromCode(request.getPlanCode()).getLabel()
                    + " | License until: " + LICENSE_DATE_FORMATTER.format(licenseEndDate)
                    + " | Gateway: " + resolveGatewayLabel(request.getPaymentGatewayCode())
                    + buildReferralMessage(referralResolutionTO));
            applicationNotificationService.addPublicApplicationNotification(notification);
        } catch (Exception exception) {
            logger.error("Unable to publish clinic registration notification", exception);
        }
    }

    private ClinicRegistrationResultTO failure(ClinicRegistrationResultTO result, String message) {
        result.setStatus(GeneralConstants.FAILED);
        result.setMessage(message);
        return result;
    }

    private String safeValue(String input) {
        return input == null ? "" : input.trim();
    }

    private String joinAddressLines(String addressLine1, String addressLine2) {
        String firstLine = safeValue(addressLine1);
        String secondLine = safeValue(addressLine2);
        if (firstLine.isEmpty()) {
            return secondLine;
        }
        if (secondLine.isEmpty()) {
            return firstLine;
        }
        return firstLine + ", " + secondLine;
    }

    private String resolveGatewayLabel(String paymentGatewayCode) {
        String gatewayCode = safeValue(paymentGatewayCode);
        if ("razorpay".equalsIgnoreCase(gatewayCode)) {
            return "Razorpay Test Mode";
        }
        if ("cashfree".equalsIgnoreCase(gatewayCode)) {
            return "Cashfree Sandbox";
        }
        return "Manual activation";
    }

    private String buildReferralMessage(ReferralResolutionTO referralResolutionTO) {
        if (referralResolutionTO == null || !referralResolutionTO.isMatched()) {
            return "";
        }
        return " | Referral: " + referralResolutionTO.getReferralCode() + " (" + referralResolutionTO.getBenefitType() + ")";
    }

    private java.math.BigDecimal resolvePlanAmount(ApplicationPricing applicationPricing, String planCode) {
        if (applicationPricing == null) {
            return java.math.BigDecimal.ZERO;
        }
        LicensePlan plan = LicensePlan.fromCode(planCode);
        if (plan == LicensePlan.MONTHLY) {
            return applicationPricing.getOneMonthPrice();
        }
        if (plan == LicensePlan.SIX_MONTHS) {
            return applicationPricing.getSixMonthPrice();
        }
        return applicationPricing.getOneYearPrice();
    }

    private static class LicenseCreationResult {
        private final LocalDate licenseEndDate;
        private final java.math.BigDecimal subscriptionAmount;

        private LicenseCreationResult(LocalDate licenseEndDate, java.math.BigDecimal subscriptionAmount) {
            this.licenseEndDate = licenseEndDate;
            this.subscriptionAmount = subscriptionAmount;
        }

        private LocalDate getLicenseEndDate() {
            return licenseEndDate;
        }

        private java.math.BigDecimal getSubscriptionAmount() {
            return subscriptionAmount;
        }
    }

    private enum LicensePlan {
        MONTHLY("monthly", "1 Month", 1, 0),
        SIX_MONTHS("six_months", "6 Months", 6, 1),
        YEARLY("yearly", "1 Year", 12, 2);

        private final String code;
        private final String label;
        private final int billableMonths;
        private final int freeMonths;

        LicensePlan(String code, String label, int billableMonths, int freeMonths) {
            this.code = code;
            this.label = label;
            this.billableMonths = billableMonths;
            this.freeMonths = freeMonths;
        }

        public String getLabel() {
            return label;
        }

        public int getBillableMonths() {
            return billableMonths;
        }

        public int getFreeMonths() {
            return freeMonths;
        }

        public static LicensePlan fromCode(String code) {
            for (LicensePlan plan : values()) {
                if (plan.code.equalsIgnoreCase(code == null ? "" : code.trim())) {
                    return plan;
                }
            }
            throw new IllegalArgumentException("Please select a valid pricing plan.");
        }
    }
}
