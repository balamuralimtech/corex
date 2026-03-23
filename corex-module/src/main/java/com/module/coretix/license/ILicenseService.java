package com.module.coretix.license;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.license.Licenses;

import java.util.List;

public interface ILicenseService {

    GeneralConstants addLicense(UserActivityTO userActivityTO, Licenses license);

    GeneralConstants updateLicense(UserActivityTO userActivityTO, Licenses license);

    GeneralConstants deleteLicense(UserActivityTO userActivityTO, Licenses license);

    Licenses getLicenseById(int id);

    Licenses getLicenseByOrganizationId(int organizationId);

    List<Licenses> getLicenseList();

    boolean isLicenseActiveForOrganization(int organizationId);
}
