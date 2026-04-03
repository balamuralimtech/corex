package com.persist.carex.settings.dao;

import com.persist.carex.settings.InvoiceSettings;
import com.persist.coretix.modal.constants.GeneralConstants;

public interface IInvoiceSettingsDAO {

    GeneralConstants saveInvoiceSettings(InvoiceSettings invoiceSettings);

    InvoiceSettings getInvoiceSettingsByOrganizationId(Integer organizationId);
}
