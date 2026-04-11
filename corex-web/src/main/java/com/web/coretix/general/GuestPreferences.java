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
package com.web.coretix.general;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IApplicationNotificationService;
import com.module.coretix.coretix.IApplicationThemeService;
import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ApplicationNotification;
import com.persist.coretix.modal.coretix.ApplicationTheme;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.web.coretix.appgeneral.GenericManagedBean;
import com.web.coretix.constants.*;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.PrimeFaces;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.io.Serializable;
import java.util.Base64;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.faces.model.SelectItem;

import org.springframework.context.annotation.Scope;

@Named("guestPreferences")
@Scope("session")
public class GuestPreferences extends GenericManagedBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(GuestPreferences.class);
    private static final String CORE_BUNDLE_BASE_NAME = "coreAppMessages";
    private static final String DEFAULT_LANGUAGE_CODE = "en";
    private static final String FILE_EXTENSION = ".properties";
    private static final String CLASSES_RESOURCE_PATH = "/WEB-INF/classes/";
    private static final List<LanguageOption> KNOWN_LANGUAGE_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            new LanguageOption("en", "English (US)", "us"),
            new LanguageOption("hi", "Hindi", "in"),
            new LanguageOption("ta", "Tamil", "in"),
            new LanguageOption("te", "Telugu", "in"),
            new LanguageOption("kn", "Kannada", "in"),
            new LanguageOption("ml", "Malayalam", "in"),
            new LanguageOption("bn", "Bengali", "in"),
            new LanguageOption("mr", "Marathi", "in"),
            new LanguageOption("gu", "Gujarati", "in"),
            new LanguageOption("pa", "Punjabi", "in"),
            new LanguageOption("or", "Odia", "in"),
            new LanguageOption("as", "Assamese", "in"),
            new LanguageOption("ur", "Urdu", "pk"),
            new LanguageOption("si", "Sinhala", "lk"),
            new LanguageOption("ms", "Malay", "my"),
            new LanguageOption("zh", "Chinese", "cn"),
            new LanguageOption("th", "Thai", "th"),
            new LanguageOption("ja", "Japanese", "jp"),
            new LanguageOption("fr", "French", "fr"),
            new LanguageOption("es", "Spanish", "es"),
            new LanguageOption("de", "German", "de"),
            new LanguageOption("ru", "Russian", "ru"),
            new LanguageOption("uk", "Ukrainian", "ua"),
            new LanguageOption("pl", "Polish", "pl"),
            new LanguageOption("pt", "Portuguese", "pt"),
            new LanguageOption("it", "Italian", "it"),
            new LanguageOption("ko", "Korean", "kr"),
            new LanguageOption("vi", "Vietnamese", "vn"),
            new LanguageOption("id", "Indonesian", "id"),
            new LanguageOption("nl", "Dutch", "nl"),
            new LanguageOption("tr", "Turkish", "tr")
    ));

    private Map<String, String> themeColors;

    private String theme = "blue";

    private String layout = "joomla";

    private String menuClass = "layout-menu-light";

    //private String profileMode = "inline";
    private String profileMode = "overlay";

    private String menuLayout = "static";

    private String inputStyle = "outlined";

    private final List<ComponentTheme> componentThemes = new ArrayList<>();

    private final List<LayoutTheme> layoutThemes = new ArrayList<>();

    private final List<LayoutSpecialTheme> layoutSpecialThemes = new ArrayList<>();

    private String userName;
    private String role;
    private int userId;

    private Locale locale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale();

    private List<SelectItem> languageItems;


    private String selectedLanguage;
    private boolean userManagementRendered;
    private boolean systemManagementRendered;
    private boolean applicationManagementRendered;
    private boolean licenseManagementRendered;
    private boolean dbAndServerLogRendered;

    private boolean userProfileRendered;
    private boolean userActivityRendered;
    private boolean roleAdministrationRendered;
    private boolean changePasswordRendered;
    private boolean userAdministrationRendered;

    private boolean organizationRendered;
    private boolean branchRendered;
    private boolean departmentRendered;
    private boolean designationRendered;
    private boolean countryRendered;
    private boolean stateRendered;
    private boolean cityRendered;
    private boolean regionRendered;
    private boolean subregionRendered;
    private boolean currencyRendered;
    private boolean bankDetailsRendered;
    private boolean notificationSettingRendered;
    private boolean demoRequestsRendered;



    private boolean licenseRendered;

    private boolean serverLogRendered;
    private boolean dbDetailsRendered;
    private boolean errorLogMonitorRendered;



    @Inject
    private IUserAdministrationService userAdministrationService;

    @Inject
    private IUserActivityService userActivityService;

    @Inject
    private IRoleAdministrationService roleAdministrationService;

    @Inject
    private IApplicationThemeService applicationThemeService;

    @Inject
    private IApplicationNotificationService applicationNotificationService;

    private String growlMessage;
    private List<String> topbarMessages = new ArrayList<>();
    private int topbarUnreadMessageCount;
    private List<String> applicationMessages = new ArrayList<>();
    private int applicationUnreadMessageCount;
    private int latestApplicationNotificationId;
    private boolean applicationNotificationTrackerInitialized;

    public void initializePageAttributes() {

        componentThemes.clear();
        layoutThemes.clear();
        layoutSpecialThemes.clear();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = facesContext == null
                ? null
                : (HttpSession) facesContext.getExternalContext().getSession(false);

        if (httpSession == null) {
            logger.warn("Unable to initialize guest preferences because the HTTP session is not available.");
            return;
        }

        Object sessionLanguage = httpSession.getAttribute(SessionAttributes.LANGUAGE.getName());
        if (sessionLanguage instanceof String && !((String) sessionLanguage).trim().isEmpty()) {
            selectedLanguage = (String) sessionLanguage;
            locale = Locale.forLanguageTag(selectedLanguage);
            if (facesContext.getViewRoot() != null) {
                facesContext.getViewRoot().setLocale(locale);
            }
        } else {
            selectedLanguage = locale == null ? "en" : locale.toLanguageTag();
        }

        selectedLanguage = normalizeLanguageCode(selectedLanguage);
        ensureSelectedLanguageIsAvailable(httpSession, facesContext);

        userName = (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName());
        logger.debug("Username retrieved from session: " + userName);
        role = (String) httpSession.getAttribute(SessionAttributes.ROLE.getName());
        logger.debug("Role retrieved from session: " + role);

        Integer sessionUserId = (Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());
        userId = sessionUserId != null ? sessionUserId : 0;
        logger.debug("User Id retrieved from session: " + userId);

        themeColors = new HashMap<>();
        themeColors.put("indigo", "#6610F2");

        componentThemes.add(new ComponentTheme("Amber", "amber", "#F8BD0C"));
        componentThemes.add(new ComponentTheme("Blue", "blue", "#007bff"));
        componentThemes.add(new ComponentTheme("Cyan", "cyan", "#17A2B8"));
        componentThemes.add(new ComponentTheme("Indigo", "indigo", "#6610F2"));
        componentThemes.add(new ComponentTheme("Purple", "purple", "#883cae"));
        componentThemes.add(new ComponentTheme("Teal", "teal", "#20C997"));
        componentThemes.add(new ComponentTheme("Orange", "orange", "#FD7E14"));
        componentThemes.add(new ComponentTheme("Deep Purple", "deeppurple", "#612FBE"));
        componentThemes.add(new ComponentTheme("Light Blue", "lightblue", "#4DA3FF"));
        componentThemes.add(new ComponentTheme("Green", "green", "#28A745"));
        componentThemes.add(new ComponentTheme("Light Green", "lightgreen", "#61CC79"));
        componentThemes.add(new ComponentTheme("Brown", "brown", "#986839"));
        componentThemes.add(new ComponentTheme("Dark Grey", "darkgrey", "#6C757D"));
        componentThemes.add(new ComponentTheme("Pink", "pink", "#E83E8C"));
        componentThemes.add(new ComponentTheme("Lime", "lime", "#74CD32"));

        layoutThemes.add(new LayoutTheme("Blue", "blue", "#146fd7"));
        layoutThemes.add(new LayoutTheme("Cyan", "cyan", "#0A616F"));
        layoutThemes.add(new LayoutTheme("Indigo", "indigo", "#470EA2"));
        layoutThemes.add(new LayoutTheme("Purple", "purple", "#391F68"));
        layoutThemes.add(new LayoutTheme("Teal", "teal", "#136E52"));
        layoutThemes.add(new LayoutTheme("Pink", "pink", "#771340"));
        layoutThemes.add(new LayoutTheme("Lime", "lime", "#407916"));
        layoutThemes.add(new LayoutTheme("Green", "green", "#1F8E38"));
        layoutThemes.add(new LayoutTheme("Amber", "amber", "#7A5E06"));
        layoutThemes.add(new LayoutTheme("Brown", "brown", "#593E22"));
        layoutThemes.add(new LayoutTheme("Orange", "orange", "#904100"));
        layoutThemes.add(new LayoutTheme("Deep Purple", "deeppurple", "#341A64"));
        layoutThemes.add(new LayoutTheme("Light Blue", "lightblue", "#14569D"));
        layoutThemes.add(new LayoutTheme("Light Green", "lightgreen", "#2E8942"));
        layoutThemes.add(new LayoutTheme("Dark Grey", "darkgrey", "#343A40"));

        layoutSpecialThemes.add(new LayoutSpecialTheme("Influenza", "influenza", "#a83279", "#f38e00"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Calm", "calm", "#5f2c82", "#0DA9A4"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Crimson", "crimson", "#521c52", "#c6426e"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Night", "night", "#2c0747", "#6441a5"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Skyline", "skyline", "#2b32b2", "#1488cc"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Sunkist", "sunkist", "#ee8a21", "#f2c94c"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Little Leaf", "littleleaf", "#4DB865", "#80D293"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Joomla", "joomla", "#1e3c72", "#2a5298"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Firewatch", "firewatch", "#cb2d3e", "#ef473a"));
        layoutSpecialThemes.add(new LayoutSpecialTheme("Suzy", "suzy", "#834d9b", "#d04ed6"));

        loadUserThemePreferences();
        fetchModuleRenderList();
        List<ApplicationNotification> notifications = refreshPersistentNotifications(httpSession);
        updateApplicationNotificationTracker(notifications, false);
        syncTopbarMessagesFromSession(httpSession);
    }

    public void changeLocale(){
        logger.debug("selectedLanguage : "+selectedLanguage);
        if (selectedLanguage != null) {
            logger.debug(" if (selectedLanguage != null) {");
            locale = Locale.forLanguageTag(selectedLanguage);
            FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            if (session != null) {
                session.setAttribute(SessionAttributes.LANGUAGE.getName(), selectedLanguage);
            }
        }

    }

    public String changeLocaleAndReload() {
        changeLocale();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null || facesContext.getViewRoot() == null) {
            return null;
        }

        String viewId = facesContext.getViewRoot().getViewId();
        return viewId + "?faces-redirect=true";
    }

    public List<LanguageOption> getTopbarLanguageOptions() {
        return resolveTopbarLanguageOptions();
    }

    public String getSelectedLanguageLabel() {
        return findLanguageOption(selectedLanguage).getLabel();
    }

    public String getSelectedLanguageFlag() {
        return findLanguageOption(selectedLanguage).getFlagCode();
    }

    private LanguageOption findLanguageOption(String languageCode) {
        List<LanguageOption> availableOptions = resolveTopbarLanguageOptions();
        if (languageCode != null) {
            for (LanguageOption languageOption : availableOptions) {
                if (languageOption.getCode().equalsIgnoreCase(languageCode)) {
                    return languageOption;
                }
            }
        }

        return availableOptions.isEmpty() ? KNOWN_LANGUAGE_OPTIONS.get(0) : availableOptions.get(0);
    }


    private void fetchModuleRenderList() {

        userManagementRendered = false;
        systemManagementRendered = false;
        applicationManagementRendered = false;
        licenseManagementRendered = false;
        dbAndServerLogRendered = false;
        demoRequestsRendered = false;

        List<CoreAppModule> modules = getRoleModuleList();

        if (CollectionUtils.isNotEmpty(modules)) {

            for (CoreAppModule module : modules) {

                switch (module) {
                    case USER_MANAGEMENT:
                        userManagementRendered = true;
                        getUserManagementPageList();
                        logger.debug("User management available");
                        break;
                    case SYSTEM_MANAGEMENT:
                        systemManagementRendered = true;
                        getSystemManagementPageList();
                        logger.debug("System management available");
                        break;
                    case APPLICATION_MANAGEMENT:
                        applicationManagementRendered = true;
                        getApplicationManagementPageList();
                        logger.debug("Application management available");
                        break;
                    case LICENCE:
                        licenseManagementRendered = true;
                        getLicensePageList();
                        logger.debug("Licence available");
                        break;
                    case SERVER_AND_DB:
                        dbAndServerLogRendered = true;
                        getDbAndServerLogPageList();
                        logger.debug("Server and DB available");
                        break;
                    default:
                        logger.error("Unsupported module: " + module);
                        break;
                }
            }
        }
    }

    private void getUserManagementPageList() {
        logger.debug("getUserManagementPageList");
        List<Integer> pageList = getSubModuleDetailsByRoleandModuleId(CoreAppModule.USER_MANAGEMENT.getId());

        if (CollectionUtils.isNotEmpty(pageList)) {
            for (Integer pageId : pageList) {

                logger.debug("getUserManagementPageList : pageId : " + pageId);
                logger.debug("{}", UserManagementModule.getById(pageId));

                switch (UserManagementModule.getById(pageId)) {
                    case USER_PROFILE :
                        userProfileRendered = true;
                        logger.debug("User Profile available");
                        break;
                    case USER_ACTIVITY :
                        userActivityRendered = true;
                        logger.debug("User Activity available");
                        break;
                    case ROLE_ADMINISTRATION:
                        roleAdministrationRendered = true;
                        logger.debug("Role Administration available");
                        break;
                    case CHANGE_PASSWORD:
                        changePasswordRendered = true;
                        logger.debug("Change Password available");
                        break;
                        case USER_ADMINISTRATION:
                        userAdministrationRendered = true;
                        logger.debug("User Administration available");
                        break;
                    default:
                        logger.error("Unsupported module: " + UserManagementModule.getById(pageId));
                }

            }
        }

    }

    private void getSystemManagementPageList() {
        logger.debug("getSystemManagementPageList");
        List<Integer> pageList = getSubModuleDetailsByRoleandModuleId(CoreAppModule.SYSTEM_MANAGEMENT.getId());
        if (CollectionUtils.isNotEmpty(pageList)) {
            for (Integer pageId : pageList) {
                logger.debug("getSystemManagementPageList : pageId : " + pageId);
                logger.debug("{}", SystemManagementModule.getById(pageId));
                switch (SystemManagementModule.getById(pageId)) {
                    case ORGANIZATION:
                        organizationRendered = true;
                        logger.debug("Organization available");
                        break;
                    case BRANCH:
                        branchRendered = true;
                        logger.debug("Branch available");
                        break;
                    case DEPARTMENT:
                        departmentRendered = true;
                        logger.debug("Department available");
                        break;
                    case DESIGNATION:
                        designationRendered = true;
                        logger.debug("Designation available");
                        break;
                    case COUNTRY:
                        countryRendered = true;
                        logger.debug("Country available");
                        break;
                    case STATE:
                        stateRendered = true;
                        logger.debug("State available");
                        break;
                    case CITY:
                        cityRendered = true;
                        logger.debug("City available");
                        break;
                    case REGION:
                        regionRendered = true;
                        logger.debug("Region available");
                        break;
                    case SUBREGION:
                        subregionRendered = true;
                        logger.debug("Subregion available");
                        break;
                    case CURRENCY:
                        currencyRendered = true;
                        logger.debug("Currency available");
                        break;
                    case BANK_DETAILS:
                        bankDetailsRendered = true;
                        logger.debug("Bank Details available");
                        break;
                    case NOTIFICATION_SETTING:
                        notificationSettingRendered = true;
                        logger.debug("Notification Setting available");
                        break;
                    default:
                        logger.error("Unsupported module: " + SystemManagementModule.getById(pageId));
                }
            }
        }
    }

    private void getLicensePageList() {
        logger.debug("getLicensePageList");
        List<Integer> pageList = getSubModuleDetailsByRoleandModuleId(CoreAppModule.LICENCE.getId());

        if (CollectionUtils.isNotEmpty(pageList)) {
            for (Integer pageId : pageList) {
                logger.debug("getLicensePageList : pageId : " + pageId);
                logger.debug("{}", LicenseManagementModule.getById(pageId));
                switch (LicenseManagementModule.getById(pageId)){
                    case LICENSE:
                        licenseRendered = true;
                        logger.debug("License page available");
                        break;
                    default:
                        logger.error("Unsupported module: " + LicenseManagementModule.getById(pageId));
                }
            }
        }
    }

    private void getDbAndServerLogPageList() {
        logger.debug("getDbAndServerLogPageList");
        serverLogRendered = false;
        dbDetailsRendered = false;
        errorLogMonitorRendered = false;
        List<Integer> pageList = getSubModuleDetailsByRoleandModuleId(CoreAppModule.SERVER_AND_DB.getId());
        if (CollectionUtils.isNotEmpty(pageList)) {
            for (Integer pageId : pageList) {
                logger.debug("getDbAndServerLogPageList : pageId : " + pageId);
                logger.debug("{}", ServerAndDBModule.getById(pageId));
                switch (ServerAndDBModule.getById(pageId)){
                    case SERVER_LOGS:
                        serverLogRendered = true;
                        logger.debug("License page available");
                        break;
                    case DATABASE:
                        dbDetailsRendered = true;
                        logger.debug("Database page available");
                        break;
                    case ERROR_LOG_MONITOR:
                        errorLogMonitorRendered = true;
                        logger.debug("Error log monitor page available");
                        break;
                    default:
                        logger.error("Unsupported module: " + LicenseManagementModule.getById(pageId));
                }
            }
        }
    }

    private UserDetails getUserDetailsByUsername(String username) {
        return userAdministrationService.getUserDetailEntityByUserName(username);
    }

    public void onLayoutChange() {
        menuLayout = normalizeMenuLayout(menuLayout);
        persistCurrentThemePreferences();
        PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuLayout('" + getMenuLayout() + "')");
    }

    public void onMenuThemeChange() {
        persistCurrentThemePreferences();
        if ("layout-menu-dark".equals(menuClass)) {
            PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuToDark()");
        } else {
            PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuToLight()");
        }
    }

    public void onProfileModeChange() {
        persistCurrentThemePreferences();
    }

    public void applyThemeSelection(String selectedTheme) {
        this.theme = selectedTheme;
        persistCurrentThemePreferences();
    }

    public void applyLayoutSelection(String selectedLayout, boolean special) {
        this.layout = selectedLayout;
        if (special) {
            this.menuClass = "layout-menu-dark";
        }
        persistCurrentThemePreferences();
    }

    public String navigateToHomepage()
    {
        logger.debug("entered into navigateToHomepage !!");

        return "homepage";
    }

    public String logoutAction()
    {
        logger.debug("entered into logoutAction !!");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        if (session != null) {
            logger.debug("session.getId() !!"+session.getId());
            SessionAuditSupport.auditSessionTermination(session, LoginConstants.LOGOUT_SUCCESSFUL, "USER_LOGOUT", true);
        }

        return "loginpage";

    }

    public UserActivities populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivities userActivityTO = new UserActivities();

        if (httpSession != null) {
            logger.debug("httpSession.getId() : " + httpSession.getId());
            logger.debug("#############################################################################");
            logger.debug("{}", (Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            logger.debug("{}", (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            logger.debug("{}", (String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            logger.debug("{}", (String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            logger.debug("#############################################################################");

            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            // Assuming appropriate keys for the following attributes
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }

    public  List<SelectItem> getAvailableResourceBundles() {
        logger.debug("entered into getAvailableResourceBundles !!");
        List<SelectItem> localeItems = new ArrayList<>();
        for (LanguageOption languageOption : resolveTopbarLanguageOptions()) {
            localeItems.add(new SelectItem(languageOption.getCode(), languageOption.getLabel()));
        }
        logger.debug("end of getAvailableResourceBundles !!");
        return localeItems;
    }

    public String getMenuClass() {
        return this.menuClass;
    }

    public void setMenuClass(String menuClass) {
        this.menuClass = menuClass;
    }

    public String getProfileMode() {
        return this.profileMode;
    }

    public void setProfileMode(String profileMode) {
        this.profileMode = profileMode;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout, boolean special) {
        this.layout = layout;
        if (special) {
            this.menuClass = "layout-menu-dark";
        }
    }

    public String getMenuLayout() {
        this.menuLayout = normalizeMenuLayout(this.menuLayout);
        return this.menuLayout;
    }

    public String getMenu() {
        switch (getMenuLayout()) {
            case "overlay":
                return "menu-layout-overlay";
            case "horizontal":
                this.profileMode = "overlay";
                return "menu-layout-static menu-layout-horizontal";
            case "slim":
                return "menu-layout-static menu-layout-slim";
            default:
                return "menu-layout-static";
        }
    }

    public void setMenuLayout(String menuLayout) {
        this.menuLayout = normalizeMenuLayout(menuLayout);

        if ("horizontal".equals(this.menuLayout)) {
            this.profileMode = "overlay";
        }
    }

    public boolean isHorizontalMenuLayout() {
        return "horizontal".equals(getMenuLayout());
    }

    public String getInputStyleClass() {
        return this.inputStyle.equals("filled") ? "ui-input-filled" : "";
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }

    public Map getThemeColors() {
        return this.themeColors;
    }

    public String getActiveLayoutColor() {
        for (LayoutTheme layoutTheme : layoutThemes) {
            if (layoutTheme.getFile().equals(layout)) {
                return layoutTheme.getColor();
            }
        }

        for (LayoutSpecialTheme layoutSpecialTheme : layoutSpecialThemes) {
            if (layoutSpecialTheme.getFile().equals(layout)) {
                return layoutSpecialTheme.getColor1();
            }
        }

        return "#146fd7";
    }

    public List<LayoutTheme> getLayoutThemes() {
        return layoutThemes;
    }

    public List<LayoutSpecialTheme> getLayoutSpecialThemes() {
        return layoutSpecialThemes;
    }

    public List<ComponentTheme> getComponentThemes() {
        return componentThemes;
    }

    public static class ComponentTheme {

        String name;
        String file;
        String color;

        public ComponentTheme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor() {
            return this.color;
        }
    }

    public static class LayoutTheme {

        String name;
        String file;
        boolean special = false;
        String color;

        public LayoutTheme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public boolean isSpecial() {
            return this.special;
        }

        public String getColor() {
            return color;
        }
    }

    public void checkForGrowlMessage() {
        HttpSession session = SessionUtils.getSession();
        if (session == null) {
            return;
        }

        List<ApplicationNotification> notifications = refreshPersistentNotifications(session);
        updateApplicationNotificationTracker(notifications, true);
        syncTopbarMessagesFromSession(session);
        growlMessage = (String) session.getAttribute(SessionAttributes.APPLICATION_NOTIFICATION_GROWL.getName());
        if (growlMessage != null) {
            PrimeFaces.current().executeScript(
                    "PF('growlWidget').renderMessage({severity: 'info', summary: 'Notification', detail: '"
                            + escapeForJavascript(growlMessage) + "', life: 10000, sticky: false});"
            );
            session.removeAttribute(SessionAttributes.APPLICATION_NOTIFICATION_GROWL.getName());
        }

        String roleUpdateLogoutMessage =
                (String) session.getAttribute(SessionAttributes.ROLE_UPDATE_LOGOUT_NOTIFICATION.getName());
        if (roleUpdateLogoutMessage != null) {
            PrimeFaces.current().executeScript(
                    "showForcedRoleUpdateDialog('" + escapeForJavascript(roleUpdateLogoutMessage) + "', 10);"
            );
            session.removeAttribute(SessionAttributes.ROLE_UPDATE_LOGOUT_NOTIFICATION.getName());
        }
    }

    public void handleForcedRoleUpdateLogout() {
        logoutAction();
    }

    private String escapeForJavascript(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    public String getGrowlMessage() {
        return growlMessage;
    }

    public List<String> getTopbarMessages() {
        return topbarMessages;
    }

    public List<String> getDisplayedTopbarMessages() {
        return topbarMessages;
    }

    public int getTopbarMessageCount() {
        return applicationUnreadMessageCount;
    }

    public List<String> getDisplayedApplicationMessages() {
        return applicationMessages;
    }

    public int getTopbarAlertCount() {
        return topbarUnreadMessageCount;
    }

    public void markTopbarMessagesAsSeen() {
        HttpSession session = SessionUtils.getSession();
        if (session == null) {
            return;
        }

        if (userId > 0) {
            applicationNotificationService.markAllNotificationsAsSeen(userId);
        }

        List<ApplicationNotification> notifications = refreshPersistentNotifications(session);
        updateApplicationNotificationTracker(notifications, false);
        syncTopbarMessagesFromSession(session);
    }

    public void markTopbarAlertsAsSeen() {
        HttpSession session = SessionUtils.getSession();
        if (session == null) {
            return;
        }

        topbarUnreadMessageCount = 0;
        session.setAttribute(SessionAttributes.APPLICATION_NOTIFICATION_UNREAD_COUNT.getName(), 0);
    }

    public static class LayoutSpecialTheme {

        String name;
        String file;
        boolean special = true;
        String color1;
        String color2;

        public LayoutSpecialTheme(String name, String file, String color1, String color2) {
            this.name = name;
            this.file = file;
            this.color1 = color1;
            this.color2 = color2;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor1() {
            return this.color1;
        }

        public String getColor2() {
            return this.color2;
        }

        public boolean isSpecial() {
            return this.special;
        }
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = normalizeLanguageCode(selectedLanguage);
    }

    private List<LanguageOption> resolveTopbarLanguageOptions() {
        Set<String> availableLanguageCodes = discoverAvailableLanguageCodes();
        List<LanguageOption> options = new ArrayList<>();
        for (LanguageOption languageOption : KNOWN_LANGUAGE_OPTIONS) {
            if (availableLanguageCodes.contains(languageOption.getCode())) {
                options.add(languageOption);
            }
        }

        if (options.isEmpty()) {
            options.add(KNOWN_LANGUAGE_OPTIONS.get(0));
        }

        return options;
    }

    private Set<String> discoverAvailableLanguageCodes() {
        Set<String> coreLanguageCodes = discoverBundleLanguageCodes(CORE_BUNDLE_BASE_NAME);
        String appBundleBaseName = resolveAppBundleBaseName();
        Set<String> appLanguageCodes = CORE_BUNDLE_BASE_NAME.equals(appBundleBaseName)
                ? new LinkedHashSet<>(coreLanguageCodes)
                : discoverBundleLanguageCodes(appBundleBaseName);

        if (coreLanguageCodes.isEmpty() && appLanguageCodes.isEmpty()) {
            return new LinkedHashSet<>(Collections.singleton(DEFAULT_LANGUAGE_CODE));
        }
        if (coreLanguageCodes.isEmpty()) {
            return appLanguageCodes;
        }
        if (appLanguageCodes.isEmpty()) {
            return coreLanguageCodes;
        }

        Set<String> intersection = new LinkedHashSet<>(coreLanguageCodes);
        intersection.retainAll(appLanguageCodes);
        return intersection.isEmpty() ? new LinkedHashSet<>(Collections.singleton(DEFAULT_LANGUAGE_CODE)) : intersection;
    }

    private Set<String> discoverBundleLanguageCodes(String bundleBaseName) {
        Set<String> languageCodes = new LinkedHashSet<>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            languageCodes.add(DEFAULT_LANGUAGE_CODE);
            return languageCodes;
        }

        ExternalContext externalContext = facesContext.getExternalContext();
        Set<String> resourcePaths = externalContext.getResourcePaths(CLASSES_RESOURCE_PATH);
        if (resourcePaths == null || resourcePaths.isEmpty()) {
            languageCodes.add(DEFAULT_LANGUAGE_CODE);
            return languageCodes;
        }

        String defaultBundleFileName = bundleBaseName + FILE_EXTENSION;
        String localizedBundlePrefix = bundleBaseName + "_";
        for (String resourcePath : resourcePaths) {
            if (resourcePath == null || !resourcePath.endsWith(FILE_EXTENSION)) {
                continue;
            }

            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            if (defaultBundleFileName.equals(fileName)) {
                languageCodes.add(DEFAULT_LANGUAGE_CODE);
                continue;
            }

            if (!fileName.startsWith(localizedBundlePrefix)) {
                continue;
            }

            String localeToken = fileName.substring(localizedBundlePrefix.length(), fileName.length() - FILE_EXTENSION.length());
            String normalizedCode = extractLanguageCode(localeToken);
            if (!normalizedCode.isEmpty()) {
                languageCodes.add(normalizedCode);
            }
        }

        if (languageCodes.isEmpty()) {
            languageCodes.add(DEFAULT_LANGUAGE_CODE);
        }

        return languageCodes;
    }

    private String extractLanguageCode(String localeToken) {
        if (localeToken == null || localeToken.trim().isEmpty()) {
            return "";
        }

        String normalizedToken = localeToken.trim().replace('-', '_');
        String[] localeParts = normalizedToken.split("_");
        return localeParts.length == 0 ? "" : normalizeLanguageCode(localeParts[0]);
    }

    private String normalizeLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return DEFAULT_LANGUAGE_CODE;
        }

        String normalized = languageCode.trim().replace('-', '_');
        String[] localeParts = normalized.split("_");
        return localeParts.length == 0 ? DEFAULT_LANGUAGE_CODE : localeParts[0].toLowerCase(Locale.ENGLISH);
    }

    private void ensureSelectedLanguageIsAvailable(HttpSession session, FacesContext facesContext) {
        List<LanguageOption> availableOptions = resolveTopbarLanguageOptions();
        if (availableOptions.isEmpty()) {
            selectedLanguage = DEFAULT_LANGUAGE_CODE;
            return;
        }

        for (LanguageOption languageOption : availableOptions) {
            if (languageOption.getCode().equalsIgnoreCase(selectedLanguage)) {
                return;
            }
        }

        selectedLanguage = availableOptions.get(0).getCode();
        locale = Locale.forLanguageTag(selectedLanguage);
        if (facesContext != null && facesContext.getViewRoot() != null) {
            facesContext.getViewRoot().setLocale(locale);
        }
        if (session != null) {
            session.setAttribute(SessionAttributes.LANGUAGE.getName(), selectedLanguage);
        }
    }

    private void getApplicationManagementPageList() {
        logger.debug("getApplicationManagementPageList");
        List<Integer> pageList = getSubModuleDetailsByRoleandModuleId(CoreAppModule.APPLICATION_MANAGEMENT.getId());

        if (CollectionUtils.isNotEmpty(pageList)) {
            for (Integer pageId : pageList) {
                logger.debug("getApplicationManagementPageList : pageId : " + pageId);
                logger.debug("{}", ApplicationManagementModule.getById(pageId));

                switch (ApplicationManagementModule.getById(pageId)) {
                    case DEMO_REQUESTS:
                        demoRequestsRendered = true;
                        logger.debug("Demo Requests available");
                        break;
                    default:
                        logger.error("Unsupported module: " + ApplicationManagementModule.getById(pageId));
                        break;
                }
            }
        }
    }

    private String resolveAppBundleBaseName() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return CORE_BUNDLE_BASE_NAME;
        }

        String contextPath = facesContext.getExternalContext().getRequestContextPath();
        if (contextPath == null) {
            return CORE_BUNDLE_BASE_NAME;
        }

        String normalizedContext = contextPath.toLowerCase(Locale.ENGLISH);
        if (normalizedContext.endsWith("/carex")) {
            return "carexAppMessages";
        }
        if (normalizedContext.endsWith("/shipx")) {
            return "shipxMessages";
        }
        if (normalizedContext.endsWith("/payrollx")) {
            return "payrollxMessages";
        }
        return CORE_BUNDLE_BASE_NAME;
    }

    public List<SelectItem> getLanguageItems() {
        return languageItems;
    }

    public void setLanguageItems(List<SelectItem> languageItems) {
        this.languageItems = languageItems;
    }

    private static String buildTopbarLanguageLabel(String languageCode, String englishLabel) {
        if (languageCode == null || englishLabel == null) {
            return englishLabel;
        }
        if ("en".equalsIgnoreCase(languageCode)) {
            return englishLabel;
        }

        Locale locale = Locale.forLanguageTag(languageCode);
        String nativeLabel = locale.getDisplayLanguage(locale);
        if (nativeLabel == null || nativeLabel.trim().isEmpty()) {
            return englishLabel;
        }
        if (nativeLabel.equalsIgnoreCase(englishLabel)) {
            return englishLabel;
        }
        return nativeLabel + "/" + englishLabel;
    }

    public static final class LanguageOption implements Serializable {
        private final String code;
        private final String label;
        private final String flagCode;

        public LanguageOption(String code, String label, String flagCode) {
            this.code = code;
            this.label = label;
            this.flagCode = flagCode;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return buildTopbarLanguageLabel(code, label);
        }

        public String getFlagCode() {
            return flagCode;
        }
    }

    public boolean isUserManagementRendered() {
        return userManagementRendered;
    }

    public void setUserManagementRendered(boolean userManagementRendered) {
        this.userManagementRendered = userManagementRendered;
    }

    public boolean isSystemManagementRendered() {
        return systemManagementRendered;
    }

    public void setSystemManagementRendered(boolean systemManagementRendered) {
        this.systemManagementRendered = systemManagementRendered;
    }

    public boolean isApplicationManagementRendered() {
        return applicationManagementRendered;
    }

    public void setApplicationManagementRendered(boolean applicationManagementRendered) {
        this.applicationManagementRendered = applicationManagementRendered;
    }

    public boolean isLicenseManagementRendered() {
        return licenseManagementRendered;
    }

    public void setLicenseManagementRendered(boolean licenseManagementRendered) {
        this.licenseManagementRendered = licenseManagementRendered;
    }

    public boolean isDbAndServerLogRendered() {
        return dbAndServerLogRendered;
    }

    public void setDbAndServerLogRendered(boolean dbAndServerLogRendered) {
        this.dbAndServerLogRendered = dbAndServerLogRendered;
    }

    public void setGrowlMessage(String growlMessage) {
        this.growlMessage = growlMessage;
    }

    public void setTopbarMessages(List<String> topbarMessages) {
        this.topbarMessages = topbarMessages;
    }

    public int getTopbarUnreadMessageCount() {
        return topbarUnreadMessageCount;
    }

    public void setTopbarUnreadMessageCount(int topbarUnreadMessageCount) {
        this.topbarUnreadMessageCount = topbarUnreadMessageCount;
    }

    public boolean isUserProfileRendered() {
        return userProfileRendered;
    }

    public void setUserProfileRendered(boolean userProfileRendered) {
        this.userProfileRendered = userProfileRendered;
    }

    public boolean isUserActivityRendered() {
        return userActivityRendered;
    }

    public void setUserActivityRendered(boolean userActivityRendered) {
        this.userActivityRendered = userActivityRendered;
    }

    public boolean isRoleAdministrationRendered() {
        return roleAdministrationRendered;
    }

    public void setRoleAdministrationRendered(boolean roleAdministrationRendered) {
        this.roleAdministrationRendered = roleAdministrationRendered;
    }

    public boolean isChangePasswordRendered() {
        return changePasswordRendered;
    }

    public void setChangePasswordRendered(boolean changePasswordRendered) {
        this.changePasswordRendered = changePasswordRendered;
    }

    public boolean isUserAdministrationRendered() {
        return userAdministrationRendered;
    }

    public void setUserAdministrationRendered(boolean userAdministrationRendered) {
        this.userAdministrationRendered = userAdministrationRendered;
    }

    public boolean isOrganizationRendered() {
        return organizationRendered;
    }

    public void setOrganizationRendered(boolean organizationRendered) {
        this.organizationRendered = organizationRendered;
    }

    public boolean isBranchRendered() {
        return branchRendered;
    }

    public void setBranchRendered(boolean branchRendered) {
        this.branchRendered = branchRendered;
    }

    public boolean isDepartmentRendered() {
        return departmentRendered;
    }

    public void setDepartmentRendered(boolean departmentRendered) {
        this.departmentRendered = departmentRendered;
    }

    public boolean isDesignationRendered() {
        return designationRendered;
    }

    public void setDesignationRendered(boolean designationRendered) {
        this.designationRendered = designationRendered;
    }

    public boolean isCountryRendered() {
        return countryRendered;
    }

    public void setCountryRendered(boolean countryRendered) {
        this.countryRendered = countryRendered;
    }

    public boolean isStateRendered() {
        return stateRendered;
    }

    public void setStateRendered(boolean stateRendered) {
        this.stateRendered = stateRendered;
    }

    public boolean isCityRendered() {
        return cityRendered;
    }

    public void setCityRendered(boolean cityRendered) {
        this.cityRendered = cityRendered;
    }

    public boolean isRegionRendered() {
        return regionRendered;
    }

    public void setRegionRendered(boolean regionRendered) {
        this.regionRendered = regionRendered;
    }

    public boolean isSubregionRendered() {
        return subregionRendered;
    }

    public void setSubregionRendered(boolean subregionRendered) {
        this.subregionRendered = subregionRendered;
    }

    public boolean isCurrencyRendered() {
        return currencyRendered;
    }

    public void setCurrencyRendered(boolean currencyRendered) {
        this.currencyRendered = currencyRendered;
    }

    public boolean isBankDetailsRendered() {
        return bankDetailsRendered;
    }

    public void setBankDetailsRendered(boolean bankDetailsRendered) {
        this.bankDetailsRendered = bankDetailsRendered;
    }

    public boolean isNotificationSettingRendered() {
        return notificationSettingRendered;
    }

    public void setNotificationSettingRendered(boolean notificationSettingRendered) {
        this.notificationSettingRendered = notificationSettingRendered;
    }

    public boolean isDemoRequestsRendered() {
        return demoRequestsRendered;
    }

    public void setDemoRequestsRendered(boolean demoRequestsRendered) {
        this.demoRequestsRendered = demoRequestsRendered;
    }

    public boolean isLicenseRendered() {
        return licenseRendered;
    }

    public void setLicenseRendered(boolean licenseRendered) {
        this.licenseRendered = licenseRendered;
    }

    public boolean isServerLogRendered() {
        return serverLogRendered;
    }

    public void setServerLogRendered(boolean serverLogRendered) {
        this.serverLogRendered = serverLogRendered;
    }

    public boolean isDbDetailsRendered() {
        return dbDetailsRendered;
    }

    public boolean isErrorLogMonitorRendered() {
        return errorLogMonitorRendered;
    }

    public void setErrorLogMonitorRendered(boolean errorLogMonitorRendered) {
        this.errorLogMonitorRendered = errorLogMonitorRendered;
    }

    public void setDbDetailsRendered(boolean dbDetailsRendered) {
        this.dbDetailsRendered = dbDetailsRendered;
    }

    public String getTopbarProfileImageSrc() {
        if (userId <= 0) {
            return null;
        }

        try {
            UserDetails currentUserDetails = userAdministrationService.getUserDetailById(userId);
            if (currentUserDetails == null || currentUserDetails.getProfileImage() == null
                    || currentUserDetails.getProfileImage().length == 0
                    || currentUserDetails.getProfileImageContentType() == null) {
                return null;
            }

            return "data:" + currentUserDetails.getProfileImageContentType() + ";base64,"
                    + Base64.getEncoder().encodeToString(currentUserDetails.getProfileImage());
        } catch (Exception ex) {
            logger.error("Unable to load topbar profile image for user {}", userId, ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void syncTopbarMessagesFromSession(HttpSession session) {
        Object notificationMessages = session.getAttribute(SessionAttributes.APPLICATION_NOTIFICATION_MESSAGES.getName());
        if (notificationMessages instanceof List) {
            topbarMessages = new ArrayList<>((List<String>) notificationMessages);
        } else {
            topbarMessages = new ArrayList<>();
        }

        Object unreadCount = session.getAttribute(SessionAttributes.APPLICATION_NOTIFICATION_UNREAD_COUNT.getName());
        topbarUnreadMessageCount = unreadCount instanceof Integer ? (Integer) unreadCount : 0;
    }

    private List<ApplicationNotification> refreshPersistentNotifications(HttpSession session) {
        if (session == null || userId <= 0) {
            return Collections.emptyList();
        }

        List<ApplicationNotification> notifications = applicationNotificationService.getRecentNotifications(10);
        List<String> notificationMessages = new ArrayList<>();
        for (ApplicationNotification notification : notifications) {
            notificationMessages.add(notification.getMessage());
        }

        applicationMessages = notificationMessages;
        applicationUnreadMessageCount = applicationNotificationService.getUnreadNotificationCountForUser(userId);
        return notifications;
    }

    private void updateApplicationNotificationTracker(List<ApplicationNotification> notifications, boolean showGrowl) {
        int newestNotificationId = CollectionUtils.isEmpty(notifications) ? 0 : notifications.get(0).getId();
        if (!applicationNotificationTrackerInitialized) {
            latestApplicationNotificationId = newestNotificationId;
            applicationNotificationTrackerInitialized = true;
            return;
        }

        if (showGrowl && newestNotificationId > latestApplicationNotificationId && CollectionUtils.isNotEmpty(notifications)) {
            PrimeFaces.current().executeScript(
                    "PF('growlWidget').renderMessage({severity: 'info', summary: 'Customer Request', detail: '"
                            + escapeForJavascript(notifications.get(0).getMessage()) + "', life: 10000, sticky: false});"
            );
        }

        latestApplicationNotificationId = newestNotificationId;
    }

    private void loadUserThemePreferences() {
        if (userId <= 0) {
            return;
        }

        try {
            ApplicationTheme savedTheme = applicationThemeService.getApplicationThemeByUserid(userId);
            if (savedTheme == null) {
                return;
            }

            if (savedTheme.getTheme() != null && !savedTheme.getTheme().trim().isEmpty()) {
                theme = savedTheme.getTheme().trim();
            }
            if (savedTheme.getLayout() != null && !savedTheme.getLayout().trim().isEmpty()) {
                layout = savedTheme.getLayout().trim();
            }
            if (savedTheme.getMenuClass() != null && !savedTheme.getMenuClass().trim().isEmpty()) {
                menuClass = savedTheme.getMenuClass().trim();
            }
            if (savedTheme.getProfileMode() != null && !savedTheme.getProfileMode().trim().isEmpty()) {
                profileMode = savedTheme.getProfileMode().trim();
            }
            if (savedTheme.getMenuLayout() != null && !savedTheme.getMenuLayout().trim().isEmpty()) {
                menuLayout = normalizeMenuLayout(savedTheme.getMenuLayout());
            }
            if (savedTheme.getInputStyle() != null && !savedTheme.getInputStyle().trim().isEmpty()) {
                inputStyle = savedTheme.getInputStyle().trim();
            }
        } catch (Exception e) {
            logger.error("Unable to load saved theme configuration for user {}", userId, e);
        }
    }

    public void persistCurrentThemePreferences() {
        if (userId <= 0) {
            logger.warn("Skipping theme persistence because no logged-in user is available.");
            return;
        }

        try {
            ApplicationTheme existingTheme = applicationThemeService.getApplicationThemeByUserid(userId);
            ApplicationTheme applicationTheme = existingTheme == null ? new ApplicationTheme() : existingTheme;

            applicationTheme.setUserId(userId);
            applicationTheme.setTheme(theme);
            applicationTheme.setLayout(layout);
            applicationTheme.setMenuClass(menuClass);
            applicationTheme.setProfileMode(profileMode);
            applicationTheme.setMenuLayout(getMenuLayout());
            applicationTheme.setInputStyle(inputStyle);

            UserActivityTO userActivityTO = buildThemeUserActivity();
            GeneralConstants result;
            if (existingTheme == null) {
                result = applicationThemeService.addApplicationTheme(userActivityTO, applicationTheme);
            } else {
                result = applicationThemeService.updateApplicationTheme(userActivityTO, applicationTheme);
            }
            logger.info("Theme persistence result for user {}: {} [theme={}, layout={}, menuClass={}, profileMode={}, menuLayout={}, inputStyle={}]",
                    userId, result, theme, layout, menuClass, profileMode, menuLayout, inputStyle);
        } catch (Exception e) {
            logger.error("Unable to persist theme configuration for user {}", userId, e);
        }
    }

    private String normalizeMenuLayout(String rawMenuLayout) {
        if (rawMenuLayout == null || rawMenuLayout.trim().isEmpty()) {
            return "static";
        }

        String normalized = rawMenuLayout.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.contains("horizontal")) {
            return "horizontal";
        }
        if (normalized.contains("overlay")) {
            return "overlay";
        }
        if (normalized.contains("slim")) {
            return "slim";
        }
        return "static";
    }

    private UserActivityTO buildThemeUserActivity() {
        UserActivityTO userActivityTO = new UserActivityTO();
        HttpSession session = SessionUtils.getSession();

        userActivityTO.setUserId(userId);
        userActivityTO.setUserName(userName);
        userActivityTO.setActivityType("config");
        userActivityTO.setActivityDescription("User interface preferences updated");
        userActivityTO.setCreatedAt(new Date());

        if (session != null) {
            userActivityTO.setIpAddress((String) session.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) session.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) session.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }
}





