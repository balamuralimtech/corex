package com.module.carex.settings;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.carex.settings.InvoiceSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IInvoiceSettingsService {

    GeneralConstants saveInvoiceSettings(UserActivityTO userActivityTO, InvoiceSettings invoiceSettings);

    InvoiceSettings getInvoiceSettingsByOrganizationId(Integer organizationId);
}
