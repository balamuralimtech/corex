package com.persist.shipx.request.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.request.CustomerRequest;

import java.util.List;

public interface ICustomerRequestDAO {

    GeneralConstants addCustomerRequest(CustomerRequest customerRequest);

    GeneralConstants updateCustomerRequest(CustomerRequest customerRequest);

    GeneralConstants deleteCustomerRequest(CustomerRequest customerRequest);

    List<CustomerRequest> getCustomerRequestList();

    CustomerRequest getCustomerRequestById(Integer id);
}
