package com.module.shipx.request.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.module.shipx.request.ICustomerRequestService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import com.persist.shipx.request.CustomerRequest;
import com.persist.shipx.request.dao.ICustomerRequestDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class CustomerRequestService implements ICustomerRequestService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRequestService.class);
    private static final String SYSTEM_USER_NAME = "ShipX Customer Request";

    @Inject
    private ICustomerRequestDAO customerRequestDAO;

    @Inject
    private IApplicationNotificationService applicationNotificationService;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants submitCustomerRequest(CustomerRequest customerRequest) {
        GeneralConstants result = customerRequestDAO.addCustomerRequest(customerRequest);
        if (result == GeneralConstants.SUCCESSFUL) {
            applicationNotificationService.addApplicationNotification(
                    buildSystemUserActivity(customerRequest),
                    buildApplicationNotification(customerRequest));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addCustomerRequest(UserActivityTO userActivityTO, CustomerRequest customerRequest) {
        GeneralConstants result = customerRequestDAO.addCustomerRequest(customerRequest);
        userActivityTO.setActivityDescription("Customer request added - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updateCustomerRequest(UserActivityTO userActivityTO, CustomerRequest customerRequest) {
        GeneralConstants result = customerRequestDAO.updateCustomerRequest(customerRequest);
        userActivityTO.setActivityDescription("Customer request updated - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants deleteCustomerRequest(UserActivityTO userActivityTO, CustomerRequest customerRequest) {
        GeneralConstants result = customerRequestDAO.deleteCustomerRequest(customerRequest);
        userActivityTO.setActivityDescription("Customer request deleted - " + result.getName());
        addUserActivity(userActivityTO);
        return result;
    }

    @Override
    public List<CustomerRequest> getCustomerRequestList() {
        return customerRequestDAO.getCustomerRequestList();
    }

    @Override
    public CustomerRequest getCustomerRequestById(Integer id) {
        return customerRequestDAO.getCustomerRequestById(id);
    }

    private ApplicationNotification buildApplicationNotification(CustomerRequest customerRequest) {
        ApplicationNotification notification = new ApplicationNotification();
        notification.setMessage("A customer has submitted a request form. Reference no: "
                + customerRequest.getRequestReference() + ". Please check the request.");
        notification.setCreatedByUserId(null);
        notification.setCreatedByUserName(SYSTEM_USER_NAME);
        return notification;
    }

    private UserActivityTO buildSystemUserActivity(CustomerRequest customerRequest) {
        UserActivityTO userActivityTO = new UserActivityTO();
        userActivityTO.setUserId(0);
        userActivityTO.setUserName(SYSTEM_USER_NAME);
        userActivityTO.setActivityType("Add");
        userActivityTO.setActivityDescription("Customer request notification created for reference "
                + customerRequest.getRequestReference());
        userActivityTO.setDeviceInfo("Public Form");
        userActivityTO.setIpAddress("N/A");
        userActivityTO.setLocationInfo("Customer Request Form");
        userActivityTO.setCreatedAt(new Date());
        return userActivityTO;
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        if (userActivityTO == null) {
            return;
        }

        logger.debug("Customer Request Activity - UserId: {}", userActivityTO.getUserId());
        UserActivities userActivity = new UserActivities();
        userActivity.setUserId(userActivityTO.getUserId());
        userActivity.setUserName(userActivityTO.getUserName());
        userActivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        userActivity.setIpAddress(userActivityTO.getIpAddress());
        userActivity.setLocationInfo(userActivityTO.getLocationInfo());
        userActivity.setActivityType(userActivityTO.getActivityType());
        userActivity.setActivityDescription(userActivityTO.getActivityDescription());
        userActivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userActivityDAO.addUserActivity(userActivity);
    }
}
