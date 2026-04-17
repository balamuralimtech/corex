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
package com.web.coretix.applicationstartup;

import com.web.coretix.utils.PropertyUtils;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.0
 * @author balamurali
 */
public class ApplicationStartupServlet extends HttpServlet
{

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupServlet.class);
    public static final String APPLICATION_FILE_PATH = "application.properties";
    public static final String APPLICATION_OVERRIDE_PREFIX = "application-";
    private String realPath;

    @Override
    public void init()
    {
        realPath = getServletContext().getRealPath("");

        try
        {
            loadProperties();
        }
        catch(Exception ex)
        {
            logger.error("Error occurred while loading the properties ", ex);
        }

        logger.info("=================== Application Tomcat JVM heap =============================");
        logger.info("max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        logger.info("available memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024));
        logger.info("total memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024));
        logger.info("================================================================");

        logger.info("[PL] - Preparing to start the application...");
        logger.info("[PL] - Application path: " + realPath);

        logger.info("[PL] - Application startup complete");
    }
    
    
    /**
     * This method is used to get parameters from web.xml  and call the Service Locator to look up
     *   EJB bean.
     */
    private void loadProperties()
    {
        File confDirectory = new File(realPath + File.separator + "conf");
        File basePropertiesFile = new File(confDirectory, APPLICATION_FILE_PATH);
        String contextName = resolveContextName();
        File overridePropertiesFile = new File(confDirectory, APPLICATION_OVERRIDE_PREFIX + contextName + ".properties");
        if (!overridePropertiesFile.exists()) {
            File fallbackOverrideFile = resolveFallbackOverrideFile(confDirectory, contextName);
            if (fallbackOverrideFile != null) {
                overridePropertiesFile = fallbackOverrideFile;
            }
        }

        PropertyUtils propertyUtils = new PropertyUtils(basePropertiesFile);
        Properties appProperties = propertyUtils.getProperties();
        if (appProperties == null) {
            appProperties = new Properties();
        }

        if (overridePropertiesFile.exists()) {
            logger.info("[PL] - Loading application property overrides from {}", overridePropertiesFile.getAbsolutePath());
            Properties overrideProperties = new PropertyUtils(overridePropertiesFile).getProperties();
            if (overrideProperties != null) {
                appProperties.putAll(overrideProperties);
            }
        }

        appProperties.setProperty("webclient.path", realPath);
        appProperties.setProperty("app.context", contextName);
        propertyUtils.setSystemProperties(appProperties);

        logger.info("-----------------------------");
        logger.info("[PL] - Application properties");
        logger.info("-----------------------------");
        PropertyUtils.printProperties(appProperties);
        logger.info("-----------------------------");
        logger.info("");

    }

    private String resolveContextName() {
        String contextPath = getServletContext().getContextPath();
        if (contextPath == null || contextPath.trim().isEmpty() || "/".equals(contextPath.trim())) {
            return "corex";
        }

        String normalized = contextPath.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.replace('/', '-');
    }

    private File resolveFallbackOverrideFile(File confDirectory, String contextName) {
        File[] overrideCandidates = confDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null
                        && name.startsWith(APPLICATION_OVERRIDE_PREFIX)
                        && name.endsWith(".properties")
                        && !name.equals(APPLICATION_OVERRIDE_PREFIX + contextName + ".properties");
            }
        });

        if (overrideCandidates != null && overrideCandidates.length == 1) {
            logger.info("[PL] - Using fallback application property override {}", overrideCandidates[0].getAbsolutePath());
            return overrideCandidates[0];
        }
        return null;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }

}



