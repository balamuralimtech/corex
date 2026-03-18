package com.web.coretix.home;

import com.module.coretix.commonto.CoreDashboardTO;
import com.module.coretix.coretix.ICoreDashboardService;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Named("homePageBean")
@Scope("session")
public class HomePageBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private final Logger logger = Logger.getLogger(getClass());
    private ResourceBundle resourceBundle;
    @Inject
    private ICoreDashboardService coreDashboardService;

    private CoreDashboardTO coreDashboardTO;


    private boolean timerEnabled = true;  // Timer is enabled by default

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public void refreshForm() {
        // Logic to refresh or reload data can go here
        logger.debug("Form refreshed at: " + System.currentTimeMillis());
        coreDashboardTO = coreDashboardService.fetchDashboardData();
        logger.debug("organizationCount: " + coreDashboardTO.getOrganizationCount());
        logger.debug("branchCount: " + coreDashboardTO.getBranchCount());
        logger.debug("departmentCount: " + coreDashboardTO.getDepartmentCount());
        logger.debug("designationCount: " + coreDashboardTO.getDesignationCount());
        logger.debug("countryCount: " + coreDashboardTO.getCountryCount());
        logger.debug("stateCount: " + coreDashboardTO.getStateCount());
        logger.debug("cityCount: " + coreDashboardTO.getCityCount());
        logger.debug("currencyCount: " + coreDashboardTO.getCurrencyCount());
        logger.debug("roleCount: " + coreDashboardTO.getRoleCount());
        logger.debug("userCount: " + coreDashboardTO.getUserCount());
        logger.debug("userActivityCount: " + coreDashboardTO.getUserActivityCount());
        logger.debug("Login count"+coreDashboardTO.getLoginCount());
        logger.debug("LogoutCount "+coreDashboardTO.getLogoutCount());
        logger.debug("AddCount "+coreDashboardTO.getAddCount());
        logger.debug("UpdateCount "+coreDashboardTO.getUpdateCount());
        logger.debug("DeleteCount "+coreDashboardTO.getDeleteCount());

        logger.debug("RolesUsedCount "+coreDashboardTO.getRolesUsedCount());
        logger.debug("RolesNotUsedCount "+coreDashboardTO.getRolesNotUsedCount());

        logger.debug("UsersNeverLoggedinCount "+coreDashboardTO.getUsersNeverLoggedinCount());
        logger.debug("UsersLoggedInCount "+coreDashboardTO.getUsersLoggedInCount());
        logger.debug("UsersLoggedOutCount "+coreDashboardTO.getUsersLoggedOutCount());


    }

    public void toggleTimer() {
        this.timerEnabled = !this.timerEnabled;  // Toggle the timer state
    }
//    private String memoryJson;

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        resourceBundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String test = resourceBundle.getString("homeLabel");
        logger.debug("test: " + test);

        // Create a map to hold the memory data
        Map<String, Integer> memoryData = new LinkedHashMap<>();
        memoryData.put("Max Memory", 1820); // In MB
        memoryData.put("Available Memory", 509); // In MB
        memoryData.put("Total Memory", 787); // In MB

        refreshForm();

        //PrimeFaces.current().executeScript("startCountdown()");
//        try {
//            // Convert the map to JSON using PrimeFaces' JSONObject
//            JSONObject jsonObject = new JSONObject(memoryData);
//            memoryJson = jsonObject.toString(); // Convert JSON object to string
//        } catch (Exception e) {
//            logger.error("Error while creating JSON: ", e);
//        }
//
//        logger.debug("memoryJson : " + memoryJson);
        logger.debug("end of initializePageAttributes !!!");
    }

    public CoreDashboardTO getCoreDashboardTO() {
        return coreDashboardTO;
    }

    public void setCoreDashboardTO(CoreDashboardTO coreDashboardTO) {
        this.coreDashboardTO = coreDashboardTO;
    }

    // Getter for the memory data as JSON
//    public String getMemoryJson() {
//        return memoryJson;
//    }
}
