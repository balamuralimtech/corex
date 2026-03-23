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
