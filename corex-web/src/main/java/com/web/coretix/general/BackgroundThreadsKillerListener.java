/*
 * KillBackgroundThreadsListener.java
 *
 * Created on Jul 19, 2012, 3:46:23 PM
 *
 * Copyright © 2007-2009 BMSolutions.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * BMSolutions.("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with BMSolutions.
 */
package com.web.coretix.general;

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

