package com.web.coretix.home;

import com.module.coretix.commonto.CoreDashboardTO;
import com.module.coretix.coretix.ICoreDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(HomePageBean.class);
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
        coreDashboardTO = coreDashboardService.fetchDashboardData();
        logger.info("Dashboard refreshed at: " + System.currentTimeMillis());
    }

    public void toggleTimer() {
        this.timerEnabled = !this.timerEnabled;  // Toggle the timer state
    }
//    private String memoryJson;

    public void initializePageAttributes() {
        resourceBundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        // Only load dashboard data if not already loaded (cached in session)
        if (coreDashboardTO == null) {
            coreDashboardTO = coreDashboardService.fetchDashboardData();
            logger.info("Dashboard data loaded");
        }
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

