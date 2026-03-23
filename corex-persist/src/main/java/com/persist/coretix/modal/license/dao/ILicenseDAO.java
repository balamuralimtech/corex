package com.persist.coretix.modal.license.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.license.Licenses;

import java.util.List;

public interface ILicenseDAO {

    GeneralConstants addLicense(Licenses license);

    GeneralConstants updateLicense(Licenses license);

    GeneralConstants deleteLicense(Licenses license);

    Licenses getLicense(int id);

    Licenses getLicenseByOrganizationId(int organizationId);

    List<Licenses> getLicenseList();

    boolean isLicenseActiveForOrganization(int organizationId);
}

