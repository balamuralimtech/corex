package com.web.coretix.appgeneral;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("appInfo")
@ApplicationScoped
public class AppInfoBean {

    private static final String DEFAULT_APP_NAME = "CoreX";

    public String getAppName() {
        String appName = System.getProperty("app.name");
        return appName == null || appName.trim().isEmpty() ? DEFAULT_APP_NAME : appName;
    }
}
