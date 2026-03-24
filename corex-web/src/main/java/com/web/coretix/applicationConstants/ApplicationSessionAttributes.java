/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
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
 * Project: `app.name`
 */
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





