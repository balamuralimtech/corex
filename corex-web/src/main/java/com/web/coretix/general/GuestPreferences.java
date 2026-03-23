package com.web.coretix.general;

import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.module.coretix.usermanagement.IUserActivityService;
import com.module.coretix.usermanagement.IUserAdministrationService;
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import javax.faces.model.SelectItem;

import org.springframework.context.annotation.Scope;

@Named("guestPreferences")
@Scope("session")
public class GuestPreferences extends GenericManagedBean implements Serializable {

    private final Logger logger = Logger.getLogger(getClass());

    private Map<String, String> themeColors;

    private String theme = "blue";

    private String layout = "joomla";

    private String menuClass = "layout-menu-light";

    //private String profileMode = "inline";
    private String profileMode = "overlay";

    private String menuLayout = "horizontal";
    //private String menuLayout = "static";

    private String inputStyle = "outlined";

    private final List<ComponentTheme> componentThemes = new ArrayList<>();

    private final List<LayoutTheme> layoutThemes = new ArrayList<>();

    private final List<LayoutSpecialTheme> layoutSpecialThemes = new ArrayList<>();

    private String userName;
    private String role;
    private int userId;

    private Locale locale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale();

    private List<SelectItem> languageItems;


    private  String BUNDLE_BASE_NAME = "messages";
    private  String FILE_EXTENSION = ".properties";

    private String selectedLanguage;
    private boolean userManagementRendered;
    private boolean systemManagementRendered;
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



    private boolean licenseRendered;

    private boolean serverLogRendered;
    private boolean dbDetailsRendered;



    @Inject
    private IUserAdministrationService userAdministrationService;

    @Inject
    private IUserActivityService userActivityService;

    @Inject
    private IRoleAdministrationService roleAdministrationService;

    private String growlMessage;

    public void initializePageAttributes() {

        componentThemes.clear();
        layoutThemes.clear();
        layoutSpecialThemes.clear();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);

        //languageItems = getAvailableResourceBundles(); commented to use it in the future

        userName = (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName());
        logger.debug("Username retrieved from session: " + userName);
        role = (String) httpSession.getAttribute(SessionAttributes.ROLE.getName());
        logger.debug("Role retrieved from session: " + role);

        userId = (int) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());
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

        fetchModuleRenderList();
    }

    public void changeLocale(){
        logger.debug("selectedLanguage : "+selectedLanguage);
        if (selectedLanguage != null) {
            logger.debug(" if (selectedLanguage != null) {");
            locale = Locale.forLanguageTag(selectedLanguage);
            FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        }

    }


    private void fetchModuleRenderList() {

        userManagementRendered = false;
        systemManagementRendered = false;
        licenseManagementRendered = false;
        dbAndServerLogRendered = false;

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
                logger.debug(UserManagementModule.getById(pageId));

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
                logger.debug(SystemManagementModule.getById(pageId));
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
                logger.debug(LicenseManagementModule.getById(pageId));
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
        List<Integer> pageList = getSubModuleDetailsByRoleandModuleId(CoreAppModule.SERVER_AND_DB.getId());
        if (CollectionUtils.isNotEmpty(pageList)) {
            for (Integer pageId : pageList) {
                logger.debug("getDbAndServerLogPageList : pageId : " + pageId);
                logger.debug(ServerAndDBModule.getById(pageId));
                switch (ServerAndDBModule.getById(pageId)){
                    case SERVER_LOGS:
                        serverLogRendered = true;
                        logger.debug("License page available");
                        break;
                    case DATABASE:
                        dbDetailsRendered = true;
                        logger.debug("Database page available");
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
        PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuLayout('" + menuLayout + "')");
    }

    public void onMenuThemeChange() {
        if ("layout-menu-dark".equals(menuClass)) {
            PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuToDark()");
        } else {
            PrimeFaces.current().executeScript("PrimeFaces.AvalonConfigurator.changeMenuToLight()");
        }
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

        // Invalidate the session
        if (session != null) {
            logger.debug("session.getId() !!"+session.getId());


            UserActivities userActivityTO = populateUserActivityTO();
            userActivityTO.setActivityType(UserActivityConstants.LOGOUT.getValue());
            userActivityTO.setActivityDescription("User Logged Out");
            userActivityTO.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            userActivityService.addUserActivity(userActivityTO);
            userAdministrationService.updateUserStatus(userId, LoginConstants.LOGOUT_SUCCESSFUL.getId());
            session.invalidate();
            logger.debug("Going to remove Session "+ session.getId() +" from Session Map !!");
            SessionListeners.removeSessionFromSessionMap(session.getId());
        }

        // Redirect to login page
        return "loginpage";

    }

    public UserActivities populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivities userActivityTO = new UserActivities();

        if (httpSession != null) {
            logger.debug("httpSession.getId() : " + httpSession.getId());
            logger.debug("#############################################################################");
            logger.debug((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            logger.debug((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            logger.debug((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            logger.debug((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
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

        // Get the path to the resources folder from the classpath
        URL resourceUrl = GuestPreferences.class.getClassLoader().getResource("");
        if (resourceUrl != null) {
            File resourceFolder = new File(resourceUrl.getPath());

            // Filter files to only include those with the correct naming pattern
            File[] files = resourceFolder.listFiles((dir, name) -> name.startsWith(BUNDLE_BASE_NAME) && name.endsWith(FILE_EXTENSION));

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    logger.debug("fileName : " + fileName);
                    String localeCode;
                    if (fileName.equals(BUNDLE_BASE_NAME + FILE_EXTENSION)) {
                        // Default locale if no locale code is in the file name
                        localeCode = "";  // You can set to "" or use Locale.getDefault().toString();
                    } else {
                        // Extract locale part from file name when locale code is present
                        localeCode = fileName.substring(BUNDLE_BASE_NAME.length() + 1, fileName.length() - FILE_EXTENSION.length());
                    }

                    logger.debug("localeCode : " + localeCode);
                    Locale locale = localeCode.isEmpty() ? Locale.getDefault() : new Locale(localeCode);
                    localeItems.add(new SelectItem(locale.toString(), locale.getDisplayName(locale)));
                }
            }
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
        return this.menuLayout;
    }

    public String getMenu() {
        switch (this.menuLayout) {
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
        if (menuLayout.equals("horizontal")) {
            this.profileMode = "overlay";
        }

        this.menuLayout = menuLayout;
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
        growlMessage = (String) session.getAttribute(SessionAttributes.APPLICATION_NOTIFICATION_GROWL.getName());
        if (growlMessage != null) {
            PrimeFaces.current().executeScript(
                    "PF('growlWidget').renderMessage({severity: 'info', summary: 'Notification', detail: '"
                            + growlMessage + "'});"
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
        this.selectedLanguage = selectedLanguage;
    }

    public List<SelectItem> getLanguageItems() {
        return languageItems;
    }

    public void setLanguageItems(List<SelectItem> languageItems) {
        this.languageItems = languageItems;
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

    public void setDbDetailsRendered(boolean dbDetailsRendered) {
        this.dbDetailsRendered = dbDetailsRendered;
    }
}
