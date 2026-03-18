/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import javax.crypto.SecretKey;

import com.web.coretix.constants.LoginConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import com.web.coretix.appgeneral.GenericManagedBean;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.web.coretix.constants.AccessRightConstants;

/**
 *
 * @author admin
 */
@Named("userAdministrationBean")
@Scope("session")
public class UserAdministrationBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13543434334535435L;
    private final Logger logger = Logger.getLogger(getClass());
    private List<UserDetails> usersList = new ArrayList<>();
    
    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;
    
    private UserDetails selectedUserDetail;


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

    private int usersLoggedInCount;
    private int usersLoggedOutCount;
    private int usersNeverLoggedinCount;
    
    @Inject
    private IUserAdministrationService userAdministrationService;
    
    @Inject
    private IRoleAdministrationService roleAdministrationService;
    
    @Inject
    private IOrganizationService organizationService;
    
    @Inject
    private IBranchService branchService;
    
    @Inject
    private ICountryService countryService;

    @Inject
    private IStateService stateService;

    @Inject
    private ICityService cityService;


    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        UsersStatusCountTO usersStatusCountTO = userAdministrationService.populateUsersStatusCount();
        usersLoggedInCount = usersStatusCountTO.getUsersLoggedInCount();
        usersLoggedOutCount = usersStatusCountTO.getUsersLoggedOutCount();
        usersNeverLoggedinCount = usersStatusCountTO.getUsersNeverLoggedInCount();
        logger.debug("usersLoggedInCount : "+usersLoggedInCount);
        logger.debug("usersLoggedOutCount : "+usersLoggedOutCount);
        logger.debug("usersNeverLoggedinCount : "+usersNeverLoggedinCount);
        
        setUserName("");
        setPassword("");
        setEmailId("");
        setContact("");
        
        setRole("");
        setOrganization("");
        setBranch("");
        setCountry("");
        setState("");
        setCity("");
        setAddress("");
        setAccessRight("");

        if (CollectionUtils.isNotEmpty(usersList)) {
            logger.debug("inside  usersList clear");
            usersList.clear();
        }

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
        setOrganization("");
        setBranch("");
        setCountry("");
        setState("");
        setCity("");
        setAddress("");
        setAccessRight("");
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchUserDetailsList();
        logger.debug("end of searchButtonAction !!!");
    }


    public void confirmEditButtonAction() {
        editCountry();
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
        setPassword(getSelectedUserDetail().getPassword());
        setEmailId(getSelectedUserDetail().getEmailId());
        setContact(getSelectedUserDetail().getCountry().getName());
        
        setRole(getSelectedUserDetail().getRole().getRoleName());
        setOrganization(getSelectedUserDetail().getOrganization().getOrganizationName());
        setBranch(getSelectedUserDetail().getBranch().getBranchName());
        setCountry(getSelectedUserDetail().getCountry().getName());
        setState(getSelectedUserDetail().getState().getName());
        setCity(getSelectedUserDetail().getCity().getName());
        setAddress(getSelectedUserDetail().getAddress());
        setAccessRight(AccessRightConstants.getById(getSelectedUserDetail().getAccessRight()).getValue());

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
        List<Cities> cities = cityService.getCitiesListByCountryIdAndStateId(tempCountry.getId(), tempStates.getId());
        for (Cities city : cities) {
            cityList.add(city.getName());
        }

        return cityList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizationList = new ArrayList<>();
        List<Organizations> countries = organizationService.getOrganizationsList();
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
        logger.debug("tempOrg.getId() : "+tempOrg.getId());
        List<Branches> branches = branchService.getBranchesListByOrgId(tempOrg.getId());
        for (Branches branch : branches) {
            logger.debug("branch : "+branch);
            branchList.add(branch.getBranchName());
        }

        return branchList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
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
    

    public void saveCountry() {

        logger.debug("Inside save organization method ");
        logger.debug("isAddOperation : " + isAddOperation);
        SecretKey secretKey = null;
        String encryptedPassword = null;
        try {
            secretKey = generateSecretKey();
            logger.debug("password : " + getPassword());
            encryptedPassword = encrypt(getPassword(), secretKey);
            logger.debug("password : " + encrypt(getPassword(), secretKey));
        } catch (Exception e) {
            logger.debug("Error while creating secret key !");
        }

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
        userDetails.setPassword(getPassword());
        userDetails.setEmailId(getEmailId());
        userDetails.setContact(getContact());
        
        Roles addRole = getRoleEntityByRoleName(getRole());
        if(addRole != null)
        {
            userDetails.setRole(addRole);
        }
        
        Organizations addOrganization = getOrganizationsByOrganizationName(getOrganization());
        logger.debug("country name " + addOrganization.getOrganizationName());
        if (addOrganization != null) {
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

        if (isAddOperation) {
            userDetails.setStatus(LoginConstants.NEVER_LOGIN_BEFORE.getId());
            logger.debug("if (isAddOperation) {");
            userAdministrationService.addUserDetail(userDetails);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User Added"));
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedUserDetail.getUserId() : " + getSelectedUserDetail().getUserId());
            //country.setId(selectedCountry.getId());
            userDetails.setUserId(getSelectedUserDetail().getUserId());
            userAdministrationService.updateUserDetail(userDetails);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User Updated"));
        }

        fetchUserDetailsList();
        logger.debug("crossed fetchUserDetailsList !!!");
        PrimeFaces.current().executeScript("PF('ManageUserDialog').hide()");
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

    private void deleteCountry() {
        logger.debug("inside delete user ");
        logger.debug("selectedUserDetail.getId() : " + getSelectedUserDetail().getUserId());

        userAdministrationService.deleteUserDetail(selectedUserDetail);
        fetchUserDetailsList();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User Removed"));
        PrimeFaces.current().ajax().update("userform:messages", "userform:usersDataTableId");
    }

    private void fetchUserDetailsList() {
        datatableRendered = false;
        logger.debug("inside fetchUserDetailsList ");
        if (CollectionUtils.isNotEmpty(usersList)) {
            logger.debug("inside fetchUserDetailsList clear");
            usersList.clear();
        }
        usersList.addAll(userAdministrationService.getUserDetailsList());

        if (CollectionUtils.isNotEmpty(usersList)) {
            logger.debug("countriesList.size() : " + usersList.size());
            datatableRendered = true;
            recordsCount = usersList.size();
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
}
