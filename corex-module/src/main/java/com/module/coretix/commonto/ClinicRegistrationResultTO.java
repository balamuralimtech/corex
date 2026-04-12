package com.module.coretix.commonto;

import com.persist.coretix.modal.constants.GeneralConstants;

public class ClinicRegistrationResultTO {

    private GeneralConstants status;
    private String message;
    private String adminUserName;
    private String licenseEndDate;
    private String paymentGatewayLabel;

    public GeneralConstants getStatus() {
        return status;
    }

    public void setStatus(GeneralConstants status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getLicenseEndDate() {
        return licenseEndDate;
    }

    public void setLicenseEndDate(String licenseEndDate) {
        this.licenseEndDate = licenseEndDate;
    }

    public String getPaymentGatewayLabel() {
        return paymentGatewayLabel;
    }

    public void setPaymentGatewayLabel(String paymentGatewayLabel) {
        this.paymentGatewayLabel = paymentGatewayLabel;
    }
}
