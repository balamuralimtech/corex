package com.module.coretix.coretix;

import com.module.coretix.commonto.ClinicRegistrationRequestTO;
import com.module.coretix.commonto.ClinicRegistrationResultTO;

public interface IClinicRegistrationService {

    ClinicRegistrationResultTO registerClinic(ClinicRegistrationRequestTO request);
}
