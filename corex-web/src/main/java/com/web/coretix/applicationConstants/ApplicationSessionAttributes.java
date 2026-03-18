package com.web.coretix.applicationConstants;
import java.io.File;

/**
 * @since
 * @author balamurali
 */
public enum ApplicationSessionAttributes
{
    USER_LOGIN_NAME("userLoginName"), 
    USER_NAME("userName"),
    DOWNLOADS_TEMPLATES_DIR("downloads" + File.separator + "templates"),
    TEMP("temp"),
    DOWNLOAD_FILE_LOCATION("Download File Location"),
    DOWNLOAD_FILE_NAME("Download File Name"),
    NETWORK_ADDRESS("Network Address"),
    BROWSER_CLIENT_INFO("Browser Client Info"),
    MACHINE_NAME("Machine Name"),
    ORGANIZATION_ID("Organization Id"),
    COUNTRY_ID("Country Id"),
    ORGANIZATION_NAME("Organization Name"),
    USER_ACCOUNT_ID("User Account Id"),
    AUTO_LOGOUT_TIME("Auto Logout Time"),
    LOGIN_STATUS("Login Status"),
    SESSION_CHANGE("Session Change"),
    LANGUAGE("Language");
    private String name;

    ApplicationSessionAttributes(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}

