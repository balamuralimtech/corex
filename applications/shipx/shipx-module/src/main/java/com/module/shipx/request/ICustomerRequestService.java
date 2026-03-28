package com.module.shipx.request;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.request.CustomerRequest;

import java.util.List;

public interface ICustomerRequestService {

    GeneralConstants submitCustomerRequest(CustomerRequest customerRequest);

    GeneralConstants addCustomerRequest(UserActivityTO userActivityTO, CustomerRequest customerRequest);

    GeneralConstants updateCustomerRequest(UserActivityTO userActivityTO, CustomerRequest customerRequest);

    GeneralConstants deleteCustomerRequest(UserActivityTO userActivityTO, CustomerRequest customerRequest);

    List<CustomerRequest> getCustomerRequestList();

    CustomerRequest getCustomerRequestById(Integer id);
}
