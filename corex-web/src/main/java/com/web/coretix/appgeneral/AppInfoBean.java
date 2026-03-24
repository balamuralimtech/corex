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
package com.web.coretix.appgeneral;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("appInfo")
@ApplicationScoped
public class AppInfoBean {

    private static final String DEFAULT_APP_NAME = "CoreX";
    private static final String DEFAULT_LOGIN_HEADLINE = "Simplifying Healthcare";
    private static final String DEFAULT_LOGIN_DESCRIPTION =
            "CareX is a unified healthcare management platform designed to support both clinics and hospitals in managing their daily operations efficiently.";

    public String getAppName() {
        String appName = System.getProperty("app.name");
        return appName == null || appName.trim().isEmpty() ? DEFAULT_APP_NAME : appName;
    }

    public String getLoginHeadline() {
        String loginHeadline = System.getProperty("app.login.headline");
        return loginHeadline == null || loginHeadline.trim().isEmpty()
                ? DEFAULT_LOGIN_HEADLINE
                : loginHeadline;
    }

    public String getLoginDescription() {
        String loginDescription = System.getProperty("app.login.description");
        return loginDescription == null || loginDescription.trim().isEmpty()
                ? DEFAULT_LOGIN_DESCRIPTION
                : loginDescription;
    }
}




