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
package com.web.coretix.usermanagement;

import com.module.coretix.commonto.UsersStatusCountTO;
import com.persist.coretix.modal.systemmanagement.Branches;
import com.persist.coretix.modal.systemmanagement.Cities;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.systemmanagement.States;
import com.persist.coretix.modal.usermanagement.Roles;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.module.coretix.systemmanagement.IBranchService;
import com.module.coretix.systemmanagement.ICityService;
import com.module.coretix.systemmanagement.ICountryService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.module.coretix.systemmanagement.IStateService;
import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.SessionAttributes;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.CroppedImage;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import com.web.coretix.appgeneral.GenericManagedBean;
import com.web.coretix.general.NotificationService;
import com.web.coretix.general.SessionAuditSupport;
import com.web.coretix.general.SessionListeners;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import com.web.coretix.constants.AccessRightConstants;
import com.web.coretix.constants.UserTypeConstants;
import javax.servlet.http.HttpSession;

/**
 *
 * @author admin
 */
@Named("userAdministrationBean")
@Scope("session")
public class UserAdministrationBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13543434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(UserAdministrationBean.class);
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
    private static final int AVATAR_IMAGE_SIZE = 320;
    private static final String TEMP_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final byte[] EMPTY_CROPPER_IMAGE = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4////fwAJ+wP9KobjigAAAABJRU5ErkJggg==");
    private static final String ALL_ORGANIZATIONS = "__ALL_ORGANIZATIONS__";
    private static final String ALL_USERS = "__ALL_USERS__";
    private List<UserDetails> usersList = new ArrayList<>();
    private List<UserDetails> selectedUsers = new ArrayList<>();
    
    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;
    
    private UserDetails selectedUserDetail;
    private String adminResetPassword;
    private String adminResetPasswordConfirm;
    private String customNotificationMessage;
    private String customPopupMessage;
    private String pendingAdminAction;


    private String userName;
    private String password;
    private String emailId;
    private String contact;
    
    private String role;
    private String organization;
    private String branch;
    private String country;
    private String state;
    private String city;
    private String address;
    private String accessRight;
    private String selectedOrganizationFilter;
    private String selectedUserFilter;
    private UploadedFile profileImageFile;
    private byte[] uploadedProfileImageBytes;
    private String uploadedProfileImageContentType;
    private CroppedImage croppedProfileImage;
    private boolean removeProfileImage;

    private int usersLoggedInCount;
    private int usersLoggedOutCount;
    private int usersNeverLoggedinCount;
    
    @Inject
    private transient IUserAdministrationService userAdministrationService;
    
    @Inject
    private transient IRoleAdministrationService roleAdministrationService;
    
    @Inject
    private transient IOrganizationService organizationService;
    
    @Inject
    private transient IBranchService branchService;
    
    @Inject
    private transient ICountryService countryService;

    @Inject
    private transient IStateService stateService;

    @Inject
    private transient ICityService cityService;


    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().isPostback()) {
            logger.debug("skipping initializePageAttributes during postback");
            return;
        }
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        usersLoggedInCount = 0;
        usersLoggedOutCount = 0;
        usersNeverLoggedinCount = 0;
        logger.debug("usersLoggedInCount : "+usersLoggedInCount);
        logger.debug("usersLoggedOutCount : "+usersLoggedOutCount);
        logger.debug("usersNeverLoggedinCount : "+usersNeverLoggedinCount);
        
        setUserName("");
        setPassword("");
        setEmailId("");
        setContact("");
        
        setRole("");
        setOrganization(resolveScopedOrganizationName());
        setBranch("");
        setCountry("");
        setState("");
        setCity("");
        setAddress("");
        setAccessRight("");
        selectedOrganizationFilter = resolveDefaultOrganizationFilter();
        selectedUserFilter = ALL_USERS;
        adminResetPassword = "";
        adminResetPasswordConfirm = "";
        customNotificationMessage = "";
        customPopupMessage = "";
        profileImageFile = null;
        uploadedProfileImageBytes = null;
        uploadedProfileImageContentType = null;
        croppedProfileImage = null;
        removeProfileImage = false;

        if (CollectionUtils.isNotEmpty(usersList)) {
            logger.debug("inside  usersList clear");
            usersList.clear();
        }
        selectedUsers.clear();

        PrimeFaces.current().ajax().update("userform:usersMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");

        setUserName("");
        setPassword("");
        setEmailId("");
        setContact("");
        
        setRole("");
        setOrganization(resolveScopedOrganizationName());
        setBranch("");
        setCountry("");
        setState("");
        setCity("");
        setAddress("");
        setAccessRight("");
        adminResetPassword = "";
        adminResetPasswordConfirm = "";
        customNotificationMessage = "";
        customPopupMessage = "";
        profileImageFile = null;
        uploadedProfileImageBytes = null;
        uploadedProfileImageContentType = null;
        croppedProfileImage = null;
        removeProfileImage = false;
        selectedUsers.clear();
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    private String resolveScopedOrganizationName() {
        return isApplicationAdmin() ? "" : getCurrentOrganizationName();
    }

    private String resolveDefaultOrganizationFilter() {
        return isApplicationAdmin() ? ALL_ORGANIZATIONS : getCurrentOrganizationName();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchUserDetailsList();
        PrimeFaces.current().executeScript(
                "var filterInput=document.getElementById('userform:globalFilter');"
                        + "if(filterInput){filterInput.value='';}"
                        + "if (PF('countDataTable')) { PF('countDataTable').clearFilters(); PF('countDataTable').getPaginator().setPage(0); }");
        logger.debug("end of searchButtonAction !!!");
    }

    public void onOrganizationFilterChange() {
        selectedUserFilter = ALL_USERS;
        searchButtonAction();
    }

    public void onUserFilterChange() {
        searchButtonAction();
    }

    public void confirmEditButtonAction() {
        editCountry();
    }

    public void confirmEditButtonAction(UserDetails user) {
        selectedUserDetail = user;
        confirmEditButtonAction();
    }

    public void confirmEditSelectedUsersAction() {
        if (!isSingleSelectionAvailable()) {
            addAdminActionError(getMessage("userAdminSelectSingleUserLabel"));
            return;
        }
        selectedUserDetail = selectedUsers.get(0);
        confirmEditButtonAction();
    }

    private void editCountry() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedUserDetail.getId() : " + getSelectedUserDetail().getUserId());
        
        logger.debug("userName : " + getUserName());
        logger.debug("password : " + getPassword());
        logger.debug("emailId : " + getEmailId());
        logger.debug("contact : " + getContact());
        logger.debug("role : " + getRole());
        logger.debug("organization : " + getOrganization());
        logger.debug("branch : " + getBranch());
        logger.debug("country : " + getCountry());
        logger.debug("state : " + getState());
        logger.debug("city : " + getCity());
        logger.debug("accessRight : " + getAccessRight());
        
        setUserName(getSelectedUserDetail().getUserName());
        setPassword("");
        setEmailId(getSelectedUserDetail().getEmailId());
        setContact(getSelectedUserDetail().getContact());
        
        setRole(getSelectedUserDetail().getRole() == null ? "" : getSelectedUserDetail().getRole().getRoleName());
        setOrganization(getSelectedUserDetail().getOrganization() == null ? "" : getSelectedUserDetail().getOrganization().getOrganizationName());
        setBranch(getSelectedUserDetail().getBranch() == null ? "" : getSelectedUserDetail().getBranch().getBranchName());
        setCountry(getSelectedUserDetail().getCountry() == null ? "" : getSelectedUserDetail().getCountry().getName());
        setState(getSelectedUserDetail().getState() == null ? "" : getSelectedUserDetail().getState().getName());
        setCity(getSelectedUserDetail().getCity() == null ? "" : getSelectedUserDetail().getCity().getName());
        setAddress(getSelectedUserDetail().getAddress());
        setAccessRight(AccessRightConstants.getById(getSelectedUserDetail().getAccessRight()).getValue());
        profileImageFile = null;
        uploadedProfileImageBytes = null;
        uploadedProfileImageContentType = null;
        croppedProfileImage = null;
        removeProfileImage = false;

    }
    
    public List<String> completeRole(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> roleList = new ArrayList<>();
        List<Roles> roles = roleAdministrationService.getRolesList();
        for (Roles role : roles) {
            roleList.add(role.getRoleName());
        }

        return roleList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    public List<String> completeAccessRight(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> roleList = AccessRightConstants.getAllValues();

        return roleList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
     public List<String> completeCountry(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> countryList = new ArrayList<>();
        List<Countries> countries = countryService.getCountriesList();
        for (Countries country : countries) {
            countryList.add(country.getName());
        }

        return countryList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
     
     
     public List<String> completeState(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> stateList = new ArrayList<>();
        
        Countries tempCountry = getCountriesByCountryName(country);
        if (tempCountry == null) {
            return stateList;
        }
        List<States> states = stateService.getStatesListByCountryId(tempCountry.getId());
        for (States state : states) {
            stateList.add(state.getName());
        }

        return stateList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
     
     public void onStateSelect()
     {
         logger.debug("entered into onStateSelect !!!"+state);
         
         
     }
     
    public List<String> completeCity(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> cityList = new ArrayList<>();
        Countries tempCountry = getCountriesByCountryName(country);
        States tempStates = getStateEntityByStateName(state);
        if (tempCountry == null || tempStates == null) {
            return cityList;
        }
        List<Cities> cities = cityService.getCitiesListByCountryIdAndStateId(tempCountry.getId(), tempStates.getId());
        for (Cities city : cities) {
            cityList.add(city.getName());
        }

        return cityList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizationList = new ArrayList<>();
        List<Organizations> countries = getAccessibleOrganizations(organizationService);
        for (Organizations Organization : countries) {
            organizationList.add(Organization.getOrganizationName());
        }

        return organizationList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    public void onOrgSelect()
    {
        logger.debug("inside onOrgSelect !!!!");
        logger.debug("organization : "+organization);
        logger.debug("country : "+country);
        logger.debug("state : "+state);
        
        
    }
    
    public List<String> completeBranch(String query) {
        logger.debug("inside completeBranch"+query);
        String queryLowerCase = query.toLowerCase();
        List<String> branchList = new ArrayList<>();
        logger.debug("organization : "+organization);
        Organizations tempOrg = getOrganizationsByOrganizationName(organization);
        if (tempOrg == null) {
            return branchList;
        }
        logger.debug("tempOrg.getId() : "+tempOrg.getId());
        List<Branches> branches = branchService.getBranchesListByOrgId(tempOrg.getId());
        for (Branches branch : branches) {
            logger.debug("branch : "+branch);
            branchList.add(branch.getBranchName());
        }

        return branchList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public List<String> getOrganizationFilterOptions() {
        List<String> organizationFilterOptions = new ArrayList<>();
        List<Organizations> organizations = getAccessibleOrganizations(organizationService);
        for (Organizations organizationEntity : organizations) {
            if (organizationEntity != null && organizationEntity.getOrganizationName() != null) {
                organizationFilterOptions.add(organizationEntity.getOrganizationName());
            }
        }
        return organizationFilterOptions;
    }

    public List<String> getUserFilterOptions() {
        List<String> userFilterOptions = new ArrayList<>();
        List<UserDetails> candidateUsers = new ArrayList<>(userAdministrationService.getUserDetailsList());
        candidateUsers.removeIf(user -> user == null || user.getUserName() == null || user.getUserName().trim().isEmpty());
        candidateUsers.removeIf(user -> !matchesOrganizationFilter(user));
        candidateUsers.stream()
                .map(UserDetails::getUserName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(userFilterOptions::add);
        return userFilterOptions;
    }
    
    

    private Roles getRoleEntityByRoleName(String roleName) {
        return roleAdministrationService.getRoleEntityByRoleName(roleName);
    }
    
    private Countries getCountriesByCountryName(String countryName)
    {
        return countryService.getCountryEntityByCountryName(countryName);
    }
    
    private States getStateEntityByStateName(String stateName)
    {
        return stateService.getStateEntityByStateName(stateName);
    }
    
    private Cities getCityEntityByCityName(String cityName)
    {
        return cityService.getCityEntityByCityName(cityName);
    }
    
    private Organizations getOrganizationsByOrganizationName(String organziationName)
    {
        return organizationService.getOrganizationsEntityByOrganizationName(organziationName);
    }
    
    private Branches getBranchEntityByBranchName(String branchName)
    {
        return branchService.getBranchEntityByBranchName(branchName);
    }
    

    public void saveUserDetails() {
        try {
            saveUserDetailsInternal();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.warn("Unable to save user details", ex);
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "errorLabel", ex.getMessage());
            PrimeFaces.current().ajax().update("userform:messages", "userform:addEditCountPanelId");
        }
    }

    public void handleProfileImageUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event == null ? null : event.getFile();
        profileImageFile = null;
        uploadedProfileImageBytes = null;
        uploadedProfileImageContentType = null;
        croppedProfileImage = null;
        removeProfileImage = false;

        if (uploadedFile == null || uploadedFile.getContent() == null || uploadedFile.getContent().length == 0) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "errorLabel", getMessage("userAdminSelectedProfileImageEmptyDetailLabel"));
            PrimeFaces.current().ajax().update("userform:messages", "userform:cropperPanel", "userform:imagePreviewPanel");
            return;
        }

        validateProfileImage(uploadedFile);
        profileImageFile = uploadedFile;
        uploadedProfileImageBytes = uploadedFile.getContent();
        uploadedProfileImageContentType = uploadedFile.getContentType();

        addFacesMessage(FacesMessage.SEVERITY_INFO, "userAdminProfileImageSummaryLabel",
                getMessage("userAdminProfileImageUploadedDetailLabel", uploadedFile.getFileName()));
        PrimeFaces.current().ajax().update("userform:messages", "userform:cropperPanel", "userform:imagePreviewPanel");
    }

    private void saveUserDetailsInternal() {
        logger.debug("Inside save organization method ");
        logger.debug("isAddOperation : " + isAddOperation);

        String hashedPassword = resolvePasswordForSave();
        logger.debug("Password resolved successfully");

        logger.debug("userName : " + getUserName());

        logger.debug("emailId : " + getEmailId());
        logger.debug("contact : " + getContact());
        logger.debug("role : " + getRole());
        logger.debug("organization : " + getOrganization());
        logger.debug("branch : " + getBranch());
        logger.debug("country : " + getCountry());
        logger.debug("state : " + getState());
        logger.debug("city : " + getCity());
        logger.debug("accessRight : " + getAccessRight());

        UserDetails userDetails = new UserDetails();
        userDetails.setUserName(getUserName());
        userDetails.setPassword(hashedPassword);
        userDetails.setEmailId(getEmailId());
        userDetails.setContact(getContact());
        userDetails.setUserType(UserTypeConstants.GENERAL_USER.getValue());
        
        Roles addRole = getRoleEntityByRoleName(getRole());
        if(addRole != null)
        {
            userDetails.setRole(addRole);
        }
        
        Organizations addOrganization = getOrganizationsByOrganizationName(getOrganization());
        if (addOrganization != null) {
            logger.debug("organization name " + addOrganization.getOrganizationName());
            userDetails.setOrganization(addOrganization);
        }
        
        Branches addBranch = getBranchEntityByBranchName(getBranch());
        if(addBranch != null) {
            userDetails.setBranch(addBranch);
        }
        
        Countries addCountry = getCountriesByCountryName(getCountry());
        
        if(addCountry != null) {
            userDetails.setCountry(addCountry);
        }
        
        States addState = getStateEntityByStateName(getState());
        if(addState != null) {
            userDetails.setState(addState);
        }
        
        Cities addCity = getCityEntityByCityName(getCity());
        if(addCity != null) {
            userDetails.setCity(addCity);
        }
        
        userDetails.setAccessRight(AccessRightConstants.getByValue(getAccessRight()).getId());
        
        userDetails.setAddress(getAddress());
        applyProfileImage(userDetails);
        Integer organizationId = resolveCurrentOrganizationId();

        if (isAddOperation) {
            userDetails.setStatus(LoginConstants.NEVER_LOGIN_BEFORE.getId());
            userDetails.setAccountDisabled(false);
            userDetails.setAccountLocked(false);
            logger.debug("if (isAddOperation) {");
            userAdministrationService.addUserDetail(userDetails);
            notifyActiveOrganizationUsers(organizationId,
                    buildUserChangeNotification(userDetails.getUserName(), "added"));
            addFacesMessage(FacesMessage.SEVERITY_INFO, "successLabel", getMessage("userAdminUserAddedLabel"));
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedUserDetail.getUserId() : " + getSelectedUserDetail().getUserId());
            //country.setId(selectedCountry.getId());
            userDetails.setUserId(getSelectedUserDetail().getUserId());
            preserveExistingAuditFields(userDetails, getSelectedUserDetail());
            userAdministrationService.updateUserDetail(userDetails);
            notifyActiveOrganizationUsers(organizationId,
                    buildUserChangeNotification(userDetails.getUserName(), "edited"));
            addFacesMessage(FacesMessage.SEVERITY_INFO, "successLabel", getMessage("userAdminUserUpdatedLabel"));
        }

        fetchUserDetailsList();
        logger.debug("crossed fetchUserDetailsList !!!");
        PrimeFaces.current().executeScript("PF('ManageUserDialog').hide()");
        PrimeFaces.current().executeScript(
                "var filterInput=document.getElementById('userform:globalFilter');"
                        + "if(filterInput){filterInput.value='';}"
                        + "if (PF('countDataTable')) { PF('countDataTable').clearFilters(); PF('countDataTable').getPaginator().setPage(0); }");
        logger.debug("crossed dialog hide method !!!");
        PrimeFaces.current().ajax().update("userform:messages", "userform:usersDataTableId");
        
        logger.debug("end of add/edit save button action");
    }

    // Method to get value by id from LoginConstants
    public String getLoginStatusValueById(int id) {
        return LoginConstants.getValueById(id);
    }

    public void confirmDeleteCountry() {
        deleteCountry();
    }

    public void confirmDeleteCountry(UserDetails user) {
        selectedUserDetail = user;
    }

    public void prepareDeleteSelectedUsersAction() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = null;
        pendingAdminAction = "delete";
    }

    private void deleteCountry() {
        logger.debug("inside delete user ");
        logger.debug("selectedUserDetail.getId() : " + getSelectedUserDetail().getUserId());

        String deletedUserName = selectedUserDetail != null ? selectedUserDetail.getUserName() : getMessage("userAdminUnknownUserLabel");
        Integer organizationId = resolveCurrentOrganizationId();
        userAdministrationService.deleteUserDetail(selectedUserDetail);
        notifyActiveOrganizationUsers(organizationId,
                buildUserChangeNotification(deletedUserName, "deleted"));
        fetchUserDetailsList();
        addFacesMessage(FacesMessage.SEVERITY_INFO, "successLabel", getMessage("userAdminUserRemovedLabel"));
        PrimeFaces.current().executeScript(
                "var filterInput=document.getElementById('userform:globalFilter');"
                        + "if(filterInput){filterInput.value='';}"
                        + "if (PF('countDataTable')) { PF('countDataTable').clearFilters(); PF('countDataTable').getPaginator().setPage(0); }");
        PrimeFaces.current().ajax().update("userform:messages", "userform:usersDataTableId");
    }

    private void fetchUserDetailsList() {
        datatableRendered = false;
        logger.debug("inside fetchUserDetailsList ");
        selectedUsers.clear();
        if (CollectionUtils.isNotEmpty(usersList)) {
            logger.debug("inside fetchUserDetailsList clear");
            usersList.clear();
        }
        List<UserDetails> visibleUsers = new ArrayList<>(userAdministrationService.getUserDetailsList());
        visibleUsers.removeIf(user -> !matchesOrganizationFilter(user) || !matchesUserFilter(user));
        usersList.addAll(visibleUsers);

        recalculateDashboardCounts();
        if (CollectionUtils.isNotEmpty(usersList)) {
            logger.debug("countriesList.size() : " + usersList.size());
            datatableRendered = true;
        }
    }

    private boolean matchesOrganizationFilter(UserDetails user) {
        if (user == null) {
            return false;
        }
        if (!isApplicationAdmin()) {
            return user.getOrganization() != null && canAccessOrganization(user.getOrganization().getId());
        }
        if (selectedOrganizationFilter == null || selectedOrganizationFilter.trim().isEmpty()
                || ALL_ORGANIZATIONS.equals(selectedOrganizationFilter)) {
            return true;
        }
        return user.getOrganization() != null
                && selectedOrganizationFilter.equalsIgnoreCase(user.getOrganization().getOrganizationName());
    }

    private boolean matchesUserFilter(UserDetails user) {
        if (user == null) {
            return false;
        }
        return selectedUserFilter == null || selectedUserFilter.trim().isEmpty()
                || ALL_USERS.equals(selectedUserFilter)
                || selectedUserFilter.equalsIgnoreCase(user.getUserName());
    }

    private void recalculateDashboardCounts() {
        recordsCount = usersList.size();
        usersLoggedInCount = 0;
        usersLoggedOutCount = 0;
        usersNeverLoggedinCount = 0;

        for (UserDetails user : usersList) {
            if (user == null) {
                continue;
            }
            if (user.getStatus() == LoginConstants.SUCCESSFUL_LOGIN.getId()) {
                usersLoggedInCount++;
            } else if (user.getStatus() == LoginConstants.LOGOUT_SUCCESSFUL.getId()) {
                usersLoggedOutCount++;
            } else if (user.getStatus() == LoginConstants.NEVER_LOGIN_BEFORE.getId()) {
                usersNeverLoggedinCount++;
            }
        }
    }


    /**
     * @return the datatableRendered
     */
    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    /**
     * @param datatableRendered the datatableRendered to set
     */
    public void setDatatableRendered(boolean datatableRendered) {
        this.datatableRendered = datatableRendered;
    }

    /**
     * @return the recordsCount
     */
    public int getRecordsCount() {
        return recordsCount;
    }

    /**
     * @param recordsCount the recordsCount to set
     */
    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    /**
     * @return the usersList
     */
    public List<UserDetails> getUsersList() {
        return usersList;
    }

    /**
     * @param usersList the usersList to set
     */
    public void setUsersList(List<UserDetails> usersList) {
        this.usersList = usersList;
    }

    public String getSelectedOrganizationFilter() {
        return selectedOrganizationFilter;
    }

    public void setSelectedOrganizationFilter(String selectedOrganizationFilter) {
        this.selectedOrganizationFilter = selectedOrganizationFilter;
    }

    public String getSelectedUserFilter() {
        return selectedUserFilter;
    }

    public void setSelectedUserFilter(String selectedUserFilter) {
        this.selectedUserFilter = selectedUserFilter;
    }

    public String getSelectedOrganizationFilterLabel() {
        if (!isApplicationAdmin()) {
            return getCurrentOrganizationName();
        }
        if (selectedOrganizationFilter == null || selectedOrganizationFilter.trim().isEmpty()
                || ALL_ORGANIZATIONS.equals(selectedOrganizationFilter)) {
            return getMessage("allOrganizationsLabel");
        }
        return selectedOrganizationFilter;
    }

    public String getSelectedUserFilterLabel() {
        if (selectedUserFilter == null || selectedUserFilter.trim().isEmpty() || ALL_USERS.equals(selectedUserFilter)) {
            return getMessage("allUsersLabel");
        }
        return selectedUserFilter;
    }

    /**
     * @return the selectedUserDetail
     */
    public UserDetails getSelectedUserDetail() {
        return selectedUserDetail;
    }

    /**
     * @param selectedUserDetail the selectedUserDetail to set
     */
    public void setSelectedUserDetail(UserDetails selectedUserDetail) {
        this.selectedUserDetail = selectedUserDetail;
    }

    public List<UserDetails> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<UserDetails> selectedUsers) {
        this.selectedUsers = selectedUsers == null ? new ArrayList<>() : selectedUsers;
    }

    public int getSelectedUsersCount() {
        return selectedUsers == null ? 0 : selectedUsers.size();
    }

    public boolean isAnyUserSelected() {
        return getSelectedUsersCount() > 0;
    }

    public boolean isSingleSelectionAvailable() {
        return getSelectedUsersCount() == 1;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the emailId
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * @param emailId the emailId to set
     */
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return the branch
     */
    public String getBranch() {
        return branch;
    }

    /**
     * @param branch the branch to set
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the accessRight
     */
    public String getAccessRight() {
        return accessRight;
    }

    /**
     * @param accessRight the accessRight to set
     */
    public void setAccessRight(String accessRight) {
        this.accessRight = accessRight;
    }

    public int getUsersLoggedInCount() {
        return usersLoggedInCount;
    }

    public void setUsersLoggedInCount(int usersLoggedInCount) {
        this.usersLoggedInCount = usersLoggedInCount;
    }

    public int getUsersLoggedOutCount() {
        return usersLoggedOutCount;
    }

    public void setUsersLoggedOutCount(int usersLoggedOutCount) {
        this.usersLoggedOutCount = usersLoggedOutCount;
    }

    public int getUsersNeverLoggedinCount() {
        return usersNeverLoggedinCount;
    }

    public void setUsersNeverLoggedinCount(int usersNeverLoggedinCount) {
        this.usersNeverLoggedinCount = usersNeverLoggedinCount;
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Object session = facesContext.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return null;
        }

        Object organizationId = ((HttpSession) session).getAttribute(SessionAttributes.ORGANIZATION_ID.getName());
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private void notifyActiveOrganizationUsers(Integer organizationId, String message) {
        NotificationService.sendGrowlMessageToOrganization(organizationId, message);
    }

    private String buildUserChangeNotification(String changedUserName, String action) {
        String actorUserName = resolveCurrentUserName();
        String formattedDateTime = LocalDateTime.now().format(NOTIFICATION_DATE_TIME_FORMATTER);
        return getMessage("userAdminUserChangeNotificationPatternLabel", changedUserName,
                resolveUserChangeActionLabel(action), actorUserName, formattedDateTime);
    }

    private String resolveCurrentUserName() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return getMessage("userAdminUnknownUserLabel");
        }

        Object session = facesContext.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return getMessage("userAdminUnknownUserLabel");
        }

        Object username = ((HttpSession) session).getAttribute(SessionAttributes.USERNAME.getName());
        return username instanceof String && !((String) username).trim().isEmpty()
                ? ((String) username).trim()
                : getMessage("userAdminUnknownUserLabel");
    }

    public void prepareResetPasswordAction() {
        adminResetPassword = generateTemporaryPassword(12);
        adminResetPasswordConfirm = adminResetPassword;
    }

    public void prepareResetPasswordAction(UserDetails user) {
        selectedUserDetail = user;
        prepareResetPasswordAction();
    }

    public void prepareResetPasswordForSelectedUsers() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = isSingleSelectionAvailable() ? selectedUsers.get(0) : null;
        prepareResetPasswordAction();
    }

    public void prepareCustomNotificationAction() {
        customNotificationMessage = "";
    }

    public void prepareCustomNotificationAction(UserDetails user) {
        selectedUserDetail = user;
        prepareCustomNotificationAction();
    }

    public void prepareCustomNotificationForSelectedUsers() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = isSingleSelectionAvailable() ? selectedUsers.get(0) : null;
        prepareCustomNotificationAction();
    }

    public void prepareCustomPopupAction() {
        customPopupMessage = "";
    }

    public void prepareCustomPopupAction(UserDetails user) {
        selectedUserDetail = user;
        prepareCustomPopupAction();
    }

    public void prepareCustomPopupForSelectedUsers() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = isSingleSelectionAvailable() ? selectedUsers.get(0) : null;
        prepareCustomPopupAction();
    }

    public void prepareDisableAction(UserDetails user) {
        selectedUserDetail = user;
        selectedUsers.clear();
        pendingAdminAction = "disable";
    }

    public void prepareUnlockAction(UserDetails user) {
        selectedUserDetail = user;
        selectedUsers.clear();
        pendingAdminAction = "unlock";
    }

    public void prepareForceLogoutAction(UserDetails user) {
        selectedUserDetail = user;
        selectedUsers.clear();
        pendingAdminAction = "force-logout";
    }

    public void prepareDisableSelectedUsersAction() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = null;
        pendingAdminAction = "disable";
    }

    public void prepareUnlockSelectedUsersAction() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = null;
        pendingAdminAction = "unlock";
    }

    public void prepareForceLogoutSelectedUsersAction() {
        if (!isAnyUserSelected()) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return;
        }
        selectedUserDetail = null;
        pendingAdminAction = "force-logout";
    }

    public void disableSelectedUser() {
        UserDetails user = reloadSelectedUser();
        if (user == null) {
            return;
        }

        userAdministrationService.updateUserAccountControls(user.getUserId(), true, user.isAccountLocked());
        userAdministrationService.markLogout(user.getUserId(), LoginConstants.LOGOUT_SUCCESSFUL.getId(), null);
        forceLogoutForUser(user, "ACCOUNT_DISABLED_BY_ADMIN");
        notifyActiveOrganizationUsers(resolveOrganizationId(user), buildUserChangeNotification(user.getUserName(), "disabled"));
        refreshAfterAdminAction(getMessage("userAdminDisabledSuccessLabel"));
    }

    public void disableSelectedUser(UserDetails user) {
        selectedUserDetail = user;
        disableSelectedUser();
    }

    public void unlockSelectedUser() {
        UserDetails user = reloadSelectedUser();
        if (user == null) {
            return;
        }

        userAdministrationService.updateUserAccountControls(user.getUserId(), false, false);
        NotificationService.sendGrowlMessageToUserAccount(user.getUserId(),
                getMessage("userAdminAccessRestoredNotificationLabel", resolveCurrentUserName()));
        notifyActiveOrganizationUsers(resolveOrganizationId(user), buildUserChangeNotification(user.getUserName(), "unlocked"));
        refreshAfterAdminAction(getMessage("userAdminUnlockedSuccessLabel"));
    }

    public void unlockSelectedUser(UserDetails user) {
        selectedUserDetail = user;
        unlockSelectedUser();
    }

    public void resetSelectedUserPassword() {
        List<UserDetails> actionUsers = resolveActionUsers();
        if (actionUsers.isEmpty()) {
            return;
        }

        if (adminResetPassword == null || adminResetPassword.trim().isEmpty()) {
            addAdminActionError(getMessage("userAdminResetPasswordRequiredLabel"));
            return;
        }

        if (!adminResetPassword.equals(adminResetPasswordConfirm)) {
            addAdminActionError(getMessage("userAdminResetPasswordMismatchLabel"));
            return;
        }

        String hashedPassword = hashPassword(adminResetPassword.trim());
        for (UserDetails user : actionUsers) {
            userAdministrationService.updateUserPassword(user.getUserId(), hashedPassword);
            userAdministrationService.updateUserAccountControls(user.getUserId(), user.isAccountDisabled(), false);
            NotificationService.sendGrowlMessageToUserAccount(user.getUserId(),
                    getMessage("userAdminPasswordResetNoticeLabel", resolveCurrentUserName()));
            notifyActiveOrganizationUsers(resolveOrganizationId(user), buildUserChangeNotification(user.getUserName(), "password-reset"));
        }
        adminResetPassword = "";
        adminResetPasswordConfirm = "";
        PrimeFaces.current().executeScript("PF('resetPasswordDialog').hide()");
        refreshAfterAdminAction(getMessage("userAdminPasswordResetSuccessLabel"));
    }

    public void forceLogoutSelectedUser() {
        UserDetails user = reloadSelectedUser();
        if (user == null) {
            return;
        }

        if (scheduleForcedLogoutForUser(user)) {
            userAdministrationService.markLogout(user.getUserId(), LoginConstants.LOGOUT_SUCCESSFUL.getId(), null);
            notifyActiveOrganizationUsers(resolveOrganizationId(user), buildUserChangeNotification(user.getUserName(), "force-logout-requested"));
            refreshAfterAdminAction(getMessage("userAdminForceLogoutRequestedSuccessLabel"));
            return;
        }

        forceLogoutForUser(user, "FORCED_LOGOUT_BY_ADMIN");
        notifyActiveOrganizationUsers(resolveOrganizationId(user), buildUserChangeNotification(user.getUserName(), "force-logged-out"));
        refreshAfterAdminAction(getMessage("userAdminForcedLogoutSuccessLabel"));
    }

    public void forceLogoutSelectedUser(UserDetails user) {
        selectedUserDetail = user;
        forceLogoutSelectedUser();
    }

    public void confirmPendingAdminAction() {
        if ("disable".equals(pendingAdminAction)) {
            for (UserDetails user : resolveActionUsers()) {
                selectedUserDetail = user;
                disableSelectedUser();
            }
        } else if ("unlock".equals(pendingAdminAction)) {
            for (UserDetails user : resolveActionUsers()) {
                selectedUserDetail = user;
                unlockSelectedUser();
            }
        } else if ("force-logout".equals(pendingAdminAction)) {
            for (UserDetails user : resolveActionUsers()) {
                selectedUserDetail = user;
                forceLogoutSelectedUser();
            }
        } else if ("delete".equals(pendingAdminAction)) {
            for (UserDetails user : resolveActionUsers()) {
                selectedUserDetail = user;
                deleteCountry();
            }
        }
        pendingAdminAction = null;
    }

    public void sendCustomNotificationToSelectedUser() {
        List<UserDetails> actionUsers = resolveActionUsers();
        if (actionUsers.isEmpty()) {
            return;
        }

        String trimmedMessage = customNotificationMessage == null ? "" : customNotificationMessage.trim();
        if (trimmedMessage.isEmpty()) {
            addAdminActionError(getMessage("userAdminNotificationMessageRequiredLabel"));
            return;
        }

        for (UserDetails user : actionUsers) {
            NotificationService.sendGrowlMessageToUserAccount(user.getUserId(), trimmedMessage);
        }
        customNotificationMessage = "";
        PrimeFaces.current().executeScript("PF('customNotificationDialog').hide()");
        refreshAfterAdminAction(getMessage("userAdminCustomNotificationSuccessLabel"));
    }

    public void sendCustomPopupToSelectedUser() {
        List<UserDetails> actionUsers = resolveActionUsers();
        if (actionUsers.isEmpty()) {
            return;
        }

        String trimmedMessage = customPopupMessage == null ? "" : customPopupMessage.trim();
        if (trimmedMessage.isEmpty()) {
            addAdminActionError(getMessage("userAdminPopupMessageRequiredLabel"));
            return;
        }

        for (UserDetails user : actionUsers) {
            NotificationService.sendGrowlMessageToUserAccount(user.getUserId(), "[Popup] " + trimmedMessage);
        }
        customPopupMessage = "";
        PrimeFaces.current().executeScript("PF('customPopupDialog').hide()");
        refreshAfterAdminAction(getMessage("userAdminCustomPopupSuccessLabel"));
    }

    public String getAccountAccessLabel(UserDetails user) {
        if (user == null) {
            return getMessage("unknownLabel");
        }
        if (user.isAccountDisabled()) {
            return getMessage("userAdminAccessDisabledLabel");
        }
        if (user.isAccountLocked()) {
            return getMessage("userAdminAccessLockedLabel");
        }
        return getMessage("userAdminAccessEnabledLabel");
    }

    public String getAccountAccessSeverity(UserDetails user) {
        if (user == null) {
            return "info";
        }
        if (user.isAccountDisabled()) {
            return "danger";
        }
        if (user.isAccountLocked()) {
            return "warning";
        }
        return "success";
    }

    private void forceLogoutForUser(UserDetails user, String terminationReason) {
        if (user == null) {
            return;
        }

        String lastSessionId = user.getLastSessionId();
        HttpSession targetSession = lastSessionId == null || lastSessionId.trim().isEmpty()
                ? null
                : SessionListeners.getAssociatedSession(lastSessionId);

        if (targetSession != null) {
            SessionAuditSupport.auditSessionTermination(targetSession, LoginConstants.LOGOUT_SUCCESSFUL,
                    terminationReason, true);
            return;
        }

        userAdministrationService.markLogout(user.getUserId(), LoginConstants.LOGOUT_SUCCESSFUL.getId(), null);
    }

    private boolean scheduleForcedLogoutForUser(UserDetails user) {
        if (user == null) {
            return false;
        }

        String lastSessionId = user.getLastSessionId();
        HttpSession targetSession = lastSessionId == null || lastSessionId.trim().isEmpty()
                ? null
                : SessionListeners.getAssociatedSession(lastSessionId);

        if (targetSession == null) {
            return false;
        }

        targetSession.setAttribute(SessionAttributes.ADMIN_FORCE_LOGOUT_NOTIFICATION.getName(),
                getMessage("userAdminForcedLogoutDialogMessageLabel"));
        return true;
    }

    private void refreshAfterAdminAction(String successMessage) {
        fetchUserDetailsList();
        selectedUsers.clear();
        addFacesMessage(FacesMessage.SEVERITY_INFO, "successLabel", successMessage);
        PrimeFaces.current().ajax().update("userform:messages", "userform:usersMainPanelId");
    }

    private void addAdminActionError(String message) {
        addFacesMessage(FacesMessage.SEVERITY_ERROR, "errorLabel", message);
        PrimeFaces.current().ajax().update("userform:messages");
    }

    private UserDetails reloadSelectedUser() {
        if (selectedUserDetail == null) {
            addAdminActionError(getMessage("userAdminSelectUserFirstLabel"));
            return null;
        }

        UserDetails persistentUser = userAdministrationService.getUserDetailById(selectedUserDetail.getUserId());
        if (persistentUser == null) {
            addAdminActionError(getMessage("userAdminSelectedUserNotFoundLabel"));
            return null;
        }

        selectedUserDetail = persistentUser;
        return persistentUser;
    }

    private List<UserDetails> resolveActionUsers() {
        List<UserDetails> sourceUsers = isAnyUserSelected()
                ? selectedUsers
                : (selectedUserDetail == null ? Collections.emptyList() : Collections.singletonList(selectedUserDetail));

        List<UserDetails> persistentUsers = new ArrayList<>();
        for (UserDetails sourceUser : sourceUsers) {
            if (sourceUser == null) {
                continue;
            }

            UserDetails persistentUser = userAdministrationService.getUserDetailById(sourceUser.getUserId());
            if (persistentUser != null) {
                persistentUsers.add(persistentUser);
            }
        }

        if (persistentUsers.isEmpty()) {
            addAdminActionError(getMessage("userAdminSelectedUserNotFoundLabel"));
        }

        return persistentUsers;
    }

    public void selectAllUsers() {
        selectedUsers = usersList == null ? new ArrayList<>() : new ArrayList<>(usersList);
    }

    public void clearSelectedUsers() {
        selectedUsers.clear();
    }

    private Integer resolveOrganizationId(UserDetails user) {
        return user != null && user.getOrganization() != null ? user.getOrganization().getId() : null;
    }

    private String generateTemporaryPassword(int length) {
        StringBuilder passwordBuilder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            passwordBuilder.append(TEMP_PASSWORD_ALPHABET.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_ALPHABET.length())));
        }
        return passwordBuilder.toString();
    }

    private String resolveUserChangeActionLabel(String action) {
        if (action == null || action.trim().isEmpty()) {
            return getMessage("unknownLabel");
        }

        switch (action) {
            case "disable":
                return getMessage("userAdminActionDisabledLabel");
            case "added":
                return getMessage("userAdminActionAddedLabel");
            case "edited":
                return getMessage("userAdminActionEditedLabel");
            case "delete":
            case "deleted":
                return getMessage("userAdminActionDeletedLabel");
            case "disabled":
                return getMessage("userAdminActionDisabledLabel");
            case "unlock":
                return getMessage("userAdminActionUnlockedLabel");
            case "unlocked":
                return getMessage("userAdminActionUnlockedLabel");
            case "password-reset":
                return getMessage("userAdminActionPasswordResetLabel");
            case "force-logout":
                return getMessage("userAdminActionForceLoggedOutLabel");
            case "force-logout-requested":
                return getMessage("userAdminActionForceLogoutRequestedLabel");
            case "force-logged-out":
                return getMessage("userAdminActionForceLoggedOutLabel");
            default:
                return action;
        }
    }

    private void applyProfileImage(UserDetails userDetails) {
        if (removeProfileImage) {
            userDetails.setProfileImage(null);
            userDetails.setProfileImageContentType(null);
            return;
        }

        if (croppedProfileImage != null && croppedProfileImage.getBytes() != null && croppedProfileImage.getBytes().length > 0) {
            AvatarImage avatarImage = resizeImageToAvatar(croppedProfileImage.getBytes(), "image/png");
            userDetails.setProfileImage(avatarImage.getBytes());
            userDetails.setProfileImageContentType(avatarImage.getContentType());
            return;
        }

        if (uploadedProfileImageBytes != null && uploadedProfileImageBytes.length > 0) {
            AvatarImage avatarImage = resizeImageToAvatar(uploadedProfileImageBytes, uploadedProfileImageContentType);
            userDetails.setProfileImage(avatarImage.getBytes());
            userDetails.setProfileImageContentType(avatarImage.getContentType());
            return;
        }

        if (!isAddOperation && selectedUserDetail != null) {
            userDetails.setProfileImage(selectedUserDetail.getProfileImage());
            userDetails.setProfileImageContentType(selectedUserDetail.getProfileImageContentType());
        }
    }

    private void validateProfileImage(UploadedFile uploadedFile) {
        String contentType = uploadedFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(getMessage("userAdminProfileImageInvalidFileLabel"));
        }

        if (uploadedFile.getSize() > (2L * 1024L * 1024L)) {
            throw new IllegalArgumentException(getMessage("userAdminProfileImageSizeLimitLabel"));
        }
    }

    private String resolvePasswordForSave() {
        if (isAddOperation) {
            if (getPassword() == null || getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException(getMessage("userAdminPasswordRequiredLabel"));
            }
            return hashPassword(getPassword());
        }

        if (getPassword() == null || getPassword().trim().isEmpty()) {
            if (selectedUserDetail == null || selectedUserDetail.getPassword() == null
                    || selectedUserDetail.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException(getMessage("userAdminExistingPasswordResolveFailedLabel"));
            }
            return selectedUserDetail.getPassword();
        }

        return hashPassword(getPassword());
    }

    private void preserveExistingAuditFields(UserDetails targetUserDetails, UserDetails sourceUserDetails) {
        if (sourceUserDetails == null) {
            return;
        }

        targetUserDetails.setStatus(sourceUserDetails.getStatus());
        targetUserDetails.setAccountDisabled(sourceUserDetails.isAccountDisabled());
        targetUserDetails.setAccountLocked(sourceUserDetails.isAccountLocked());
        targetUserDetails.setUserType(sourceUserDetails.getUserType());
        targetUserDetails.setLastPasswordChange(sourceUserDetails.getLastPasswordChange());
        targetUserDetails.setLastSuccessfulLogin(sourceUserDetails.getLastSuccessfulLogin());
        targetUserDetails.setLastSeenAt(sourceUserDetails.getLastSeenAt());
        targetUserDetails.setLastLogoutAt(sourceUserDetails.getLastLogoutAt());
        targetUserDetails.setLastSessionId(sourceUserDetails.getLastSessionId());
        targetUserDetails.setCreatedAt(sourceUserDetails.getCreatedAt());
        targetUserDetails.setUpdatedAt(sourceUserDetails.getUpdatedAt());
    }

    public String getSelectedProfileImageDataUrl() {
        if (removeProfileImage) {
            return null;
        }

        if (croppedProfileImage != null && croppedProfileImage.getBytes() != null && croppedProfileImage.getBytes().length > 0) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(croppedProfileImage.getBytes());
        }

        if (uploadedProfileImageBytes != null && uploadedProfileImageBytes.length > 0) {
            String previewContentType = uploadedProfileImageContentType == null ? "image/png" : uploadedProfileImageContentType;
            return "data:" + previewContentType + ";base64," + Base64.getEncoder().encodeToString(uploadedProfileImageBytes);
        }

        UserDetails imageSource = selectedUserDetail;
        if (imageSource == null || imageSource.getProfileImage() == null || imageSource.getProfileImage().length == 0
                || imageSource.getProfileImageContentType() == null) {
            return null;
        }

        return "data:" + imageSource.getProfileImageContentType() + ";base64,"
                + Base64.getEncoder().encodeToString(imageSource.getProfileImage());
    }

    public String encodeImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return "";
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public UploadedFile getProfileImageFile() {
        return profileImageFile;
    }

    public void setProfileImageFile(UploadedFile profileImageFile) {
        this.profileImageFile = profileImageFile;
    }

    public String getAdminResetPassword() {
        return adminResetPassword;
    }

    public void setAdminResetPassword(String adminResetPassword) {
        this.adminResetPassword = adminResetPassword;
    }

    public String getAdminResetPasswordConfirm() {
        return adminResetPasswordConfirm;
    }

    public void setAdminResetPasswordConfirm(String adminResetPasswordConfirm) {
        this.adminResetPasswordConfirm = adminResetPasswordConfirm;
    }

    public String getCustomNotificationMessage() {
        return customNotificationMessage;
    }

    public void setCustomNotificationMessage(String customNotificationMessage) {
        this.customNotificationMessage = customNotificationMessage;
    }

    public String getCustomPopupMessage() {
        return customPopupMessage;
    }

    public void setCustomPopupMessage(String customPopupMessage) {
        this.customPopupMessage = customPopupMessage;
    }

    public CroppedImage getCroppedProfileImage() {
        return croppedProfileImage;
    }

    public void setCroppedProfileImage(CroppedImage croppedProfileImage) {
        this.croppedProfileImage = croppedProfileImage;
    }

    public StreamedContent getUploadedProfileImage() {
        if (removeProfileImage) {
            return buildEmptyCropperImage();
        }

        if (uploadedProfileImageBytes == null || uploadedProfileImageBytes.length == 0) {
            return buildEmptyCropperImage();
        }

        return DefaultStreamedContent.builder()
                .contentType(uploadedProfileImageContentType == null ? "image/png" : uploadedProfileImageContentType)
                .stream(() -> new ByteArrayInputStream(uploadedProfileImageBytes))
                .build();
    }

    public boolean isUploadedProfileImageAvailable() {
        return !removeProfileImage
                && uploadedProfileImageBytes != null
                && uploadedProfileImageBytes.length > 0;
    }

    private StreamedContent buildEmptyCropperImage() {
        return DefaultStreamedContent.builder()
                .contentType("image/png")
                .stream(() -> new ByteArrayInputStream(EMPTY_CROPPER_IMAGE))
                .build();
    }

    public boolean isRemoveProfileImage() {
        return removeProfileImage;
    }

    public void setRemoveProfileImage(boolean removeProfileImage) {
        this.removeProfileImage = removeProfileImage;
    }

    private AvatarImage resizeImageToAvatar(byte[] sourceBytes, String sourceContentType) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(sourceBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IllegalArgumentException(getMessage("userAdminSelectedProfileImageReadFailedLabel"));
            }

            int cropSize = Math.min(originalImage.getWidth(), originalImage.getHeight());
            int x = (originalImage.getWidth() - cropSize) / 2;
            int y = (originalImage.getHeight() - cropSize) / 2;
            BufferedImage squareImage = originalImage.getSubimage(x, y, cropSize, cropSize);

            BufferedImage resizedImage = new BufferedImage(AVATAR_IMAGE_SIZE, AVATAR_IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resizedImage.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.drawImage(squareImage, 0, 0, AVATAR_IMAGE_SIZE, AVATAR_IMAGE_SIZE, null);
            } finally {
                graphics.dispose();
            }

            ImageIO.write(resizedImage, "png", outputStream);
            return new AvatarImage(outputStream.toByteArray(), "image/png");
        } catch (IOException ex) {
            throw new IllegalStateException(getMessage("userAdminProfileImageResizeFailedLabel"), ex);
        }
    }

    private void addFacesMessage(javax.faces.application.FacesMessage.Severity severity, String summaryKey, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, getMessage(summaryKey), detail));
    }

    private String getMessage(String key, Object... arguments) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return key;
        }

        ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msg");
        String pattern = resourceBundle != null && resourceBundle.containsKey(key) ? resourceBundle.getString(key) : key;
        return arguments == null || arguments.length == 0 ? pattern : MessageFormat.format(pattern, arguments);
    }

    public int getAvatarImageSize() {
        return AVATAR_IMAGE_SIZE;
    }

    public String getProfileImageResizeHelpText() {
        return getMessage("userAdminAvatarResizeHelpLabel", AVATAR_IMAGE_SIZE);
    }

    public String getPendingAdminActionConfirmationMessage() {
        if ((selectedUserDetail == null && !isAnyUserSelected()) || pendingAdminAction == null || pendingAdminAction.trim().isEmpty()) {
            return getMessage("userAdminSelectUserFirstLabel");
        }

        String actionLabel = resolveUserChangeActionLabel(pendingAdminAction);
        if (isAnyUserSelected()) {
            return getMessage("userAdminConfirmBulkActionMessageLabel", actionLabel, selectedUsers.size());
        }
        return getMessage("userAdminConfirmActionMessageLabel", actionLabel, selectedUserDetail.getUserName());
    }

    public String getSelectedActionTargetLabel() {
        if (isAnyUserSelected()) {
            if (isSingleSelectionAvailable()) {
                return selectedUsers.get(0).getUserName();
            }
            return getMessage("userAdminSelectedUsersLabel", selectedUsers.size());
        }

        return selectedUserDetail == null ? "-" : selectedUserDetail.getUserName();
    }

    public String getSelectedUsersDisplayNames() {
        List<UserDetails> users = isAnyUserSelected()
                ? selectedUsers
                : (selectedUserDetail == null ? Collections.emptyList() : Collections.singletonList(selectedUserDetail));

        return users.stream()
                .filter(Objects::nonNull)
                .map(UserDetails::getUserName)
                .filter(Objects::nonNull)
                .filter(name -> !name.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }

    public int getActionTargetCount() {
        if (isAnyUserSelected()) {
            return selectedUsers.size();
        }
        return selectedUserDetail == null ? 0 : 1;
    }

    public String getPendingAdminActionLabel() {
        return resolveUserChangeActionLabel(pendingAdminAction);
    }

    private static final class AvatarImage {
        private final byte[] bytes;
        private final String contentType;

        private AvatarImage(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }

        private byte[] getBytes() {
            return bytes;
        }

        private String getContentType() {
            return contentType;
        }
    }
}
