package com.web.shipx.request;

import com.module.coretix.systemmanagement.ICountryService;
import com.module.shipx.request.ICustomerRequestService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.request.CustomerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;

@Named("customerRequestFormBean")
@Scope("session")
public class CustomerRequestFormBean extends CustomerRequestFormSupport {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRequestFormBean.class);
    private static final long serialVersionUID = 1L;

    @Inject
    private transient ICountryService countryService;

    @Inject
    private transient ICustomerRequestService customerRequestService;

    @PostConstruct
    public void initialize() {
        initializeForm(countryService);
    }

    public void submitRequest() {
        CustomerRequest customerRequest = new CustomerRequest();
        if (!populateCustomerRequest(customerRequest, countryService)) {
            return;
        }

        customerRequest.setRequestReference(generateRequestReference());
        customerRequest.setStatus("NEW");
        customerRequest.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        customerRequest.setCreatedByUserId(null);
        customerRequest.setCreatedByUserName("Public Customer");

        GeneralConstants status = customerRequestService.submitCustomerRequest(customerRequest);
        if (status == GeneralConstants.SUCCESSFUL) {
            setLastSubmittedReference(customerRequest.getRequestReference());
            addMessage(javax.faces.application.FacesMessage.SEVERITY_INFO, "Request submitted",
                    "Reference " + customerRequest.getRequestReference() + " has been created.");
            resetForm();
            return;
        }

        logger.warn("Customer request submission failed with status {}", status);
        addMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, "Submission failed",
                "Unable to submit the request right now. Please try again.");
    }
}
