package com.web.shipx.request;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.ICountryService;
import com.module.shipx.request.ICustomerRequestService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.request.CustomerRequest;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named("manageCustomerRequestBean")
@Scope("session")
public class ManageCustomerRequestBean extends CustomerRequestFormSupport {

    private static final Logger logger = LoggerFactory.getLogger(ManageCustomerRequestBean.class);
    private static final long serialVersionUID = 1L;

    @Inject
    private ICountryService countryService;

    @Inject
    private ICustomerRequestService customerRequestService;

    private List<CustomerRequest> customerRequestList = new ArrayList<>();
    private CustomerRequest selectedCustomerRequest = new CustomerRequest();
    private boolean addOperation = true;
    private boolean viewMode;
    private boolean datatableRendered;
    private int recordsCount;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback()) {
            if (CollectionUtils.isEmpty(getCountryNames())) {
                initializeForm(countryService);
            }
            return;
        }

        initializeForm(countryService);
        addOperation = true;
        viewMode = false;
        datatableRendered = false;
        recordsCount = 0;
        selectedCustomerRequest = new CustomerRequest();
        resetForm();
        if (CollectionUtils.isNotEmpty(customerRequestList)) {
            customerRequestList.clear();
        }
    }

    public void addButtonAction() {
        addOperation = true;
        viewMode = false;
        selectedCustomerRequest = new CustomerRequest();
        resetForm();
    }

    public void searchButtonAction() {
        fetchCustomerRequestList();
    }

    public void confirmEditButtonAction() {
        if (selectedCustomerRequest == null || selectedCustomerRequest.getId() == null) {
            return;
        }

        addOperation = false;
        viewMode = false;
        CustomerRequest persistentRequest = customerRequestService.getCustomerRequestById(selectedCustomerRequest.getId());
        if (persistentRequest == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Customer request not found.");
            return;
        }

        selectedCustomerRequest = persistentRequest;
        initializeForm(countryService);
        loadFromCustomerRequest(persistentRequest);
    }

    public void viewCustomerRequestAction() {
        if (selectedCustomerRequest == null || selectedCustomerRequest.getId() == null) {
            return;
        }

        addOperation = false;
        viewMode = true;
        CustomerRequest persistentRequest = customerRequestService.getCustomerRequestById(selectedCustomerRequest.getId());
        if (persistentRequest == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Customer request not found.");
            return;
        }

        selectedCustomerRequest = persistentRequest;
        initializeForm(countryService);
        loadFromCustomerRequest(persistentRequest);
    }

    public void saveCustomerRequest() {
        CustomerRequest customerRequest = addOperation ? new CustomerRequest() : selectedCustomerRequest;
        if (!populateCustomerRequest(customerRequest, countryService)) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;

        if (addOperation) {
            customerRequest.setRequestReference(generateInternalRequestReference());
            customerRequest.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            customerRequest.setCreatedByUserId(userActivityTO.getUserId());
            customerRequest.setCreatedByUserName(userActivityTO.getUserName());
            customerRequest.setStatus("NEW");
            userActivityTO.setActivityType("Add");
            result = customerRequestService.addCustomerRequest(userActivityTO, customerRequest);
        } else {
            customerRequest.setCreatedByUserId(selectedCustomerRequest.getCreatedByUserId());
            customerRequest.setCreatedByUserName(selectedCustomerRequest.getCreatedByUserName());
            customerRequest.setCreatedAt(selectedCustomerRequest.getCreatedAt());
            customerRequest.setStatus(selectedCustomerRequest.getStatus());
            userActivityTO.setActivityType("Update");
            result = customerRequestService.updateCustomerRequest(userActivityTO, customerRequest);
        }

        handleSaveResult(result, addOperation ? "created" : "updated");
    }

    public void confirmDeleteCustomerRequest() {
        if (selectedCustomerRequest == null || selectedCustomerRequest.getId() == null) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = customerRequestService.deleteCustomerRequest(userActivityTO, selectedCustomerRequest);

        switch (result) {
            case SUCCESSFUL:
                fetchCustomerRequestList();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Customer request deleted successfully.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Customer request does not exist.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete customer request.");
                break;
        }
    }

    private void handleSaveResult(GeneralConstants result, String actionLabel) {
        switch (result) {
            case SUCCESSFUL:
                fetchCustomerRequestList();
                addMessage(FacesMessage.SEVERITY_INFO, "Success",
                        "Customer request " + actionLabel + " successfully.");
                resetForm();
                selectedCustomerRequest = new CustomerRequest();
                addOperation = true;
                viewMode = false;
                break;
            case ENTRY_ALREADY_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Request reference already exists.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Customer request does not exist.");
                break;
            default:
                logger.warn("Unable to save customer request. Result={}", result);
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to save customer request.");
                break;
        }
    }

    private void fetchCustomerRequestList() {
        customerRequestList = new ArrayList<>(customerRequestService.getCustomerRequestList());
        datatableRendered = CollectionUtils.isNotEmpty(customerRequestList);
        recordsCount = customerRequestList.size();
    }

    private UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UserActivityTO userActivityTO = new UserActivityTO();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

        if (sessionMap != null) {
            Integer userId = (Integer) sessionMap.get("userAccountId");
            userActivityTO.setUserId(userId == null ? 0 : userId);
            userActivityTO.setUserName((String) sessionMap.get("username"));
            userActivityTO.setIpAddress((String) sessionMap.get("Machine IP"));
            userActivityTO.setDeviceInfo((String) sessionMap.get("Machine Name"));
            userActivityTO.setLocationInfo((String) sessionMap.get("browserClientInfo"));
            userActivityTO.setCreatedAt(new Date());
        }

        return userActivityTO;
    }

    private String generateInternalRequestReference() {
        return "INT-CRF-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    public List<CustomerRequest> getCustomerRequestList() {
        return customerRequestList;
    }

    public CustomerRequest getSelectedCustomerRequest() {
        return selectedCustomerRequest;
    }

    public void setSelectedCustomerRequest(CustomerRequest selectedCustomerRequest) {
        this.selectedCustomerRequest = selectedCustomerRequest;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public boolean isAddOperation() {
        return addOperation;
    }

    public boolean isViewMode() {
        return viewMode;
    }
}
