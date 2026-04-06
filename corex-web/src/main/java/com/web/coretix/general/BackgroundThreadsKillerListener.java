/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.web.coretix.general;

import com.module.coretix.usermanagement.IUserActivityService;
import com.web.coretix.applicationserverlogsanddb.ErrorLogMonitorSupport;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

/**
 * This class is mainly used to freeing the resources used by application it
 * should be call only when the application deploy and un-deploy
 * @since 1.0
 * @author praveen
 */
public class BackgroundThreadsKillerListener implements ServletContextListener
{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * This method is called when the application is deploy.
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        try
        {
            WebApplicationContext context = (WebApplicationContext) sce.getServletContext().getAttribute(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            if (context == null) {
                logger.warn("Spring web application context is not ready. Error log monitor startup skipped.");
                return;
            }

            IUserActivityService userActivityService = context.getBean(IUserActivityService.class);
            String serverLocation = System.getProperty("catalina.base");
            String logsLocation = serverLocation + File.separator + "logs";
            ErrorLogMonitorSupport.startBackgroundMonitoring(userActivityService, logsLocation);
        }
        catch (Exception ex)
        {
            logger.error("Unable to start background error log monitoring.", ex);
        }
    }

    /**
     * This method is called when the application is un-deploy.
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        try
        {
            logger.info("Going to close the resources used by application");
            ErrorLogMonitorSupport.stopBackgroundMonitoring();
            WebApplicationContext context = (WebApplicationContext) sce.getServletContext().getAttribute(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            logger.info("Successfully closed the all the resources created/used by application.");
        }
        catch (Exception ex)
        {
            logger.error("Error: ", ex);
        }
    }
}




