/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.web.coretix.applicationserverlogsanddb;

import java.io.Serializable;
import javax.servlet.ServletContext;
import javax.faces.context.FacesContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author balamurali
 */
@Named("databaseDetailsBean")
@Scope("session")
public class DatabaseDetailsBean implements Serializable {
    private static final long serialVersionUID = 1354353434334535435L;
    private final Logger logger = Logger.getLogger(getClass());
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcProductName;
    private String jdbcProductVersion;

    // Called after bean creation
    public void initializePageAttributes() {
        // Access ServletContext
        logger.debug("inside DatabaseDetailsBean initializePageAttributes !!!");
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        // Get Spring's WebApplicationContext
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        // Retrieve the DataSource bean
        DataSource dataSource = (DataSource) ctx.getBean("DataSource");

        // Get the JDBC details from the DataSource
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            jdbcUrl = metaData.getURL();
            jdbcUsername = metaData.getUserName();
            jdbcProductName = metaData.getDatabaseProductName();
            jdbcProductVersion = metaData.getDatabaseProductVersion();
            
            
            logger.debug("jdbcUrl : "+jdbcUrl);
            logger.debug("jdbcUsername : "+jdbcUsername);
            logger.debug("jdbcProductName : "+jdbcProductName);
            logger.debug("jdbcProductVersion : "+jdbcProductVersion);
        } catch (Exception e) {
            logger.error("Error while fetching the Datasource connection !");
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    /**
     * @return the jdbcProductName
     */
    public String getJdbcProductName() {
        return jdbcProductName;
    }

    /**
     * @param jdbcProductName the jdbcProductName to set
     */
    public void setJdbcProductName(String jdbcProductName) {
        this.jdbcProductName = jdbcProductName;
    }

    /**
     * @return the jdbcProductVersion
     */
    public String getJdbcProductVersion() {
        return jdbcProductVersion;
    }

    /**
     * @param jdbcProductVersion the jdbcProductVersion to set
     */
    public void setJdbcProductVersion(String jdbcProductVersion) {
        this.jdbcProductVersion = jdbcProductVersion;
    }

}
