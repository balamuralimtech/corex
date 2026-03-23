package com.web.coretix.applicationstartup;

import com.web.coretix.utils.PropertyUtils;
import java.io.File;
import java.io.IOException;
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
        PropertyUtils propertyUtils = new PropertyUtils(realPath + File.separator + "conf" +
                File.separator + APPLICATION_FILE_PATH);
        Properties appProperties = propertyUtils.getProperties();
        appProperties.setProperty("webclient.path", realPath);
        propertyUtils.setSystemProperties(appProperties);

        logger.info("-----------------------------");
        logger.info("[PL] - Application properties");
        logger.info("-----------------------------");
        PropertyUtils.printProperties(appProperties);
        logger.info("-----------------------------");
        logger.info("");

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
