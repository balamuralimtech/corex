package com.web.coretix.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Named("i18n")
@Scope("session")
public class LocalizationBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LocalizationBean.class);
    private static final String CORE_BUNDLE = "coreMessages";
    private static final String CORE_APP_BUNDLE = "coreAppMessages";

    public String core(String key) {
        return resolveMessage(CORE_BUNDLE, key, true);
    }

    public String app(String key) {
        return resolveMessage(resolveAppBundleBaseName(), key, false);
    }

    private String resolveMessage(String bundleName, String key, boolean warnOnMissing) {
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, resolveLocale());
            return resourceBundle.containsKey(key) ? resourceBundle.getString(key) : handleMissing(bundleName, key, warnOnMissing);
        } catch (MissingResourceException ex) {
            return handleMissing(bundleName, key, warnOnMissing);
        }
    }

    private String handleMissing(String bundleName, String key, boolean warnOnMissing) {
        if (warnOnMissing) {
            logger.debug("Missing localization key {} in bundle {}", key, bundleName);
        }
        return key;
    }

    private Locale resolveLocale() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getViewRoot() != null && facesContext.getViewRoot().getLocale() != null) {
            return facesContext.getViewRoot().getLocale();
        }
        return Locale.ENGLISH;
    }

    private String resolveAppBundleBaseName() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return CORE_APP_BUNDLE;
        }

        String contextPath = facesContext.getExternalContext().getRequestContextPath();
        if (contextPath == null) {
            return CORE_APP_BUNDLE;
        }

        String normalizedContext = contextPath.toLowerCase(Locale.ENGLISH);
        if (normalizedContext.endsWith("/carex")) {
            return "carexMessages";
        }
        if (normalizedContext.endsWith("/shipx")) {
            return "shipxMessages";
        }
        if (normalizedContext.endsWith("/payrollx")) {
            return "payrollxMessages";
        }
        return CORE_APP_BUNDLE;
    }
}
