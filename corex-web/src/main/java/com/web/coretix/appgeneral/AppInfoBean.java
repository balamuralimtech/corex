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

import com.web.coretix.applicationstartup.ApplicationStartupServlet;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.util.Properties;

@Named("appInfo")
public class AppInfoBean {

    private static final String DEFAULT_APP_NAME = "CoreX";
    private static final String DEFAULT_BRAND_ICON_RESOURCE = "demo:images/coretix/app-brand.svg";
    private static final String DEFAULT_COMPANY_NAME = DEFAULT_APP_NAME;
    private static final String DEFAULT_LOGIN_HEADLINE = "Simplifying Healthcare";
    private static final String DEFAULT_LOGIN_DESCRIPTION =
            "CareX is a unified healthcare management platform designed to support both clinics and hospitals in managing their daily operations efficiently.";
    private static final String DEFAULT_LOGIN_CTA = "Let's Begin!";
    private static final String DEFAULT_LOGIN_VIDEO = "/resources/avalon-layout/videos/home.mp4";

    public String getAppName() {
        return getConfiguredValue("app.name", DEFAULT_APP_NAME);
    }

    public String getBrandIconResource() {
        return getConfiguredValue("app.brand.icon", DEFAULT_BRAND_ICON_RESOURCE);
    }

    public String getCompanyName() {
        return getConfiguredValue("company.name", DEFAULT_COMPANY_NAME);
    }

    public String getLoginHeadline() {
        return getConfiguredValue("app.login.headline", DEFAULT_LOGIN_HEADLINE);
    }

    public String getLoginDescription() {
        return getConfiguredValue("app.login.description", DEFAULT_LOGIN_DESCRIPTION);
    }

    public String getLoginCta() {
        return getConfiguredValue("app.login.cta", DEFAULT_LOGIN_CTA);
    }

    public String getLoginVideo() {
        return getConfiguredValue("app.login.video", DEFAULT_LOGIN_VIDEO);
    }

    private String getConfiguredValue(String key, String defaultValue) {
        String contextValue = getContextProperty(key);
        if (contextValue != null && !contextValue.trim().isEmpty()) {
            return contextValue;
        }

        String systemValue = System.getProperty(key);
        return systemValue == null || systemValue.trim().isEmpty() ? defaultValue : systemValue;
    }

    private String getContextProperty(String key) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Object context = facesContext.getExternalContext().getContext();
        if (!(context instanceof ServletContext)) {
            return null;
        }

        Object propertiesObject = ((ServletContext) context)
                .getAttribute(ApplicationStartupServlet.APPLICATION_PROPERTIES_ATTRIBUTE);
        if (!(propertiesObject instanceof Properties)) {
            return null;
        }

        return ((Properties) propertiesObject).getProperty(key);
    }
}

