/*
 * ExceptionHandlingActionListener.java
 *
 * Created on Sep 10, 2009, 10:23:01 AM
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

//import com.BMSolutions.platform.pl.systemmanagement.ModuleViewIdGenerator;
import com.sun.faces.application.ActionListenerImpl;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.el.ELResolver;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.PrimeFaces;

/**
 * This class is used to handle all kind of exceptions and navigate to the
 * corresponding exception page
 * @Since 1.0
 * @author giftsam
 */
public class ExceptionHandlingActionListener extends ActionListenerImpl 
        implements ActionListener
{
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingActionListener.class);
    private static final String GENERIC_DAO_EXCEPTION = "GenericJDBCException: Cannot open connection";
    private static final String TRANSACTION_EXCEPTION = "TransactionException: JDBC begin failed";
    private static final String RESOURCE_POOL_EXCEPTION = "resourcepool.TimeoutException";
    private static final String CONNECTION_RESET_EXCEPTION = "SQLServerException: Connection reset";

    /**
     * This method catches all kind of exceptions and include the exception page in the pageContent
     * of ModuleViewIdGenerator class
     * @param event - events in the application
     */
    @Override
    public void processAction(ActionEvent event)
    {
        try
        {            
            super.processAction(event);
        }
        catch (Exception exception)
        {
            logger.error("Exception found in action listener is: ", exception);
            FacesContext facesContext =
                    FacesContext.getCurrentInstance();
            Application application =
                    facesContext.getApplication();

            ELResolver resolver = application.getELResolver();
//            ModuleViewIdGenerator moduleViewIdGenerator =
//                    (ModuleViewIdGenerator) resolver.getValue(facesContext.getELContext(), null, "ModuleViewIdGenerator");

            String pageViewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
            logger.info("Exception occurred ajax component Page view Id is: " + pageViewId);
            /**
             * If the page view id is SSCMS Path, navigate to Exception page with header page
             */
            StringWriter exceptionSW = new StringWriter();
            PrintWriter printWriter = new PrintWriter(exceptionSW);
            exception.printStackTrace(printWriter);

            String connectionException=exceptionSW.toString();
            if (connectionException.contains(GENERIC_DAO_EXCEPTION)||
                connectionException.toString().contains(TRANSACTION_EXCEPTION)||
                connectionException.toString().contains(RESOURCE_POOL_EXCEPTION)||
                connectionException.toString().contains(CONNECTION_RESET_EXCEPTION))
            {
                NavigationHandler navigationHandler = application.getNavigationHandler();
                navigationHandler.handleNavigation(facesContext, null, "dataBase_Exception_Page_WithHeader");
            }
//            else if (!pageViewId.equalsIgnoreCase(moduleViewIdGenerator.getMainPagePath()))
//            {
//                NavigationHandler navigationHandler = application.getNavigationHandler();
//                navigationHandler.handleNavigation(facesContext, null, "exception_Page_WithHeader");
//            }
            else
            {
                /**
                 *  Set the path of the exception page in the pageContent of the class ModuleViewIdGenerator
                 */
//                moduleViewIdGenerator.setPageContent(moduleViewIdGenerator.getExceptionPagePath());
                
                //updated the main page content to navigate into error page when page gets error.
                PrimeFaces.current().ajax().update("contentOutputPanelId");
//                logger.debug("Page Navigated to: " + moduleViewIdGenerator.getPageContent());

                /**
                 * Find the UIComponent which encountered exception
                 */
                UIComponent uiComponent = event.getComponent();
                logger.info("Selected UI Component is: " + uiComponent.getId());

                /**
                 * Check the selected UIComponent is an instance of AjaxComponent, and set the
                 * reRender for the appropriate ajax component
                 */
            }
            facesContext.renderResponse();
        }
    }
}
