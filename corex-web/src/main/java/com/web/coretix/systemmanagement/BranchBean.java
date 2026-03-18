/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.web.coretix.systemmanagement;

import com.module.coretix.systemmanagement.*;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author admin
 */
@Named("branchBean")
@Scope("session")
public class BranchBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private final Logger logger = Logger.getLogger(getClass());
    private List<Branches> branchesList = new ArrayList<>();

    private ResourceBundle resourceBundle;
    private String branchOrganizationName;
    private Countries selectedCountry;
    private String branchCountry;

    private String branchName;
    private String branchState;
    private String branchCity;
    private String branchPhoneNumber;
    private String branchEmail;

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    private Branches selectedBranch = new Branches();

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


    public void initializePageAttributes()
    {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());

        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;
        if (CollectionUtils.isNotEmpty(branchesList)) {
            logger.debug("inside  branchesList clear");
            branchesList.clear();
        }

        PrimeFaces.current().ajax().update("form:branchMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void resetFields()
    {
        logger.debug("entered into resetFields action !!!");
        branchCountry = "";
        branchOrganizationName = "";
        setBranchName("");
        branchState = "";
        branchCity = "";
        branchPhoneNumber = "";
        branchEmail = "";
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void confirmEditButtonAction()
    {
        editBranch();
    }

    private void editBranch()
    {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : "+isAddOperation);
        logger.debug("selectedBranch.getId() : "+selectedBranch.getId());

        branchName = getSelectedBranch().getBranchName();
        branchOrganizationName = getSelectedBranch().getOrganization().getOrganizationName();
        branchCountry = getSelectedBranch().getCountry().getName();

        branchState = getSelectedBranch().getState();
        branchCity = getSelectedBranch().getCity();
        branchPhoneNumber = getSelectedBranch().getPhoneNumber();
        branchEmail = getSelectedBranch().getEmail();

        logger.debug("organizationName : "+branchOrganizationName);
        logger.debug("organizationCountry : "+branchCountry);
        logger.debug("organizationState : "+branchState);
        logger.debug("organizationCity : "+branchCity);
        logger.debug("organizationPhoneNumber : "+branchPhoneNumber);
        logger.debug("organizationWebsite : "+branchEmail);
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
        logger.debug("organization : "+branchOrganizationName);
        logger.debug("country : "+branchCountry);
        logger.debug("state : "+branchState);
    }


    public void saveBranch() {

        logger.debug("Inside save organization method ");
        logger.debug("branchName : "+branchName);
        logger.debug("organizationName : "+branchOrganizationName);
        logger.debug("organizationCountry : "+branchCountry);
        logger.debug("organizationState : "+branchState);
        logger.debug("organizationCity : "+branchCity);
        logger.debug("organizationPhoneNumber : "+branchPhoneNumber);
        logger.debug("organizationWebsite : "+branchEmail);
        logger.debug("isAddOperation : "+isAddOperation);

        Branches branches = new Branches();

        branches.setBranchName(branchName);

        Countries addCountry = getCountriesByCountryName(branchCountry);
        logger.debug("country name " + addCountry.getName());
        if (addCountry != null) {
            branches.setCountry(addCountry);
        }

        States addState = getStateEntityByStateName(branchState);
        if(addState != null) {
            branches.setState(addState.getName());
        }

        Cities addCity = getCityEntityByCityName(branchCity);
        if(addCity != null) {
            branches.setCity(addCity.getName());
        }

        Organizations addOrganization = getOrganizationsByOrganizationName(branchOrganizationName);
        logger.debug("country name " + addOrganization.getOrganizationName());
        if (addOrganization != null) {
            branches.setOrganization(addOrganization);
        }

        branches.setPhoneNumber(branchPhoneNumber);
        branches.setEmail(branchEmail);

        UserActivityTO userActivityTO = populateUserActivityTO();


        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");

            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(branches.getBranchName()+ " - New Branch Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = branchService.addBranch(userActivityTO,branches);
            logger.debug("addStatus : "+addStatus);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("branchAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("branchAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("branchAddFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"),  resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedBranch.getId() :"+ selectedBranch.getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Branch "+branches.getBranchName()+" Updated");
            branches.setId(selectedBranch.getId());
            GeneralConstants updateStatus = branchService.updateBranch(userActivityTO, branches);
            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( resourceBundle.getString("branchUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,  resourceBundle.getString("warningLabel"), resourceBundle.getString("branchAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,  resourceBundle.getString("warningLabel"), resourceBundle.getString("branchDoesNotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("branchUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }

        fetchBranchesList();
        PrimeFaces.current().executeScript("PF('manageBranchDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:branchMainPanelId");
    }

    public UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

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

    public List<String> completeState(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> stateList = new ArrayList<>();

        Countries tempCountry = getCountriesByCountryName(branchCountry);
        List<States> states = stateService.getStatesListByCountryId(tempCountry.getId());
        for (States state : states) {
            stateList.add(state.getName());
        }

        return stateList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public void onStateSelect()
    {
        logger.debug("entered into onStateSelect !!!"+branchState);


    }

    public List<String> completeCity(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> cityList = new ArrayList<>();
        Countries tempCountry = getCountriesByCountryName(branchCountry);
        States tempStates = getStateEntityByStateName(branchState);
        List<Cities> cities = cityService.getCitiesListByCountryIdAndStateId(tempCountry.getId(), tempStates.getId());
        for (Cities city : cities) {
            cityList.add(city.getName());
        }

        return cityList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }



    public void confirmDeleteBranch() {
        deleteBranch();
    }

    private void deleteBranch()
    {
        logger.debug("inside delete organization ");
        logger.debug("selectedOrganization.getId() : "+getSelectedBranch().getId());
        branchName = getSelectedBranch().getBranchName();
        branchOrganizationName = getSelectedBranch().getOrganization().getOrganizationName();
        branchCountry = getSelectedBranch().getCountry().getName();
        branchState = getSelectedBranch().getState();
        branchCity = getSelectedBranch().getCity();
        branchPhoneNumber = getSelectedBranch().getPhoneNumber();
        branchEmail = getSelectedBranch().getEmail();

        logger.debug("branchName : "+branchName);
        logger.debug("organizationName : "+branchOrganizationName);
        logger.debug("organizationCountry : "+branchCountry);
        logger.debug("organizationState : "+branchState);
        logger.debug("organizationCity : "+branchCity);
        logger.debug("organizationPhoneNumber : "+branchPhoneNumber);
        logger.debug("organizationWebsite : "+branchEmail);
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Branch "+branchName+" Deleted");

        GeneralConstants deleteStatus = branchService.deleteBranch(userActivityTO, getSelectedBranch());
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( resourceBundle.getString("branchRemovedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),  resourceBundle.getString("branchDoesNotExistsLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("branchRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }


        fetchBranchesList();
        PrimeFaces.current().ajax().update("form:messages", "form:branchDataTableId");
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchBranchesList();
        logger.debug("end of searchButtonAction !!!");
    }

    private void fetchBranchesList()
    {
        logger.debug("inside branchesList ");
        if(CollectionUtils.isNotEmpty(branchesList))
        {
            logger.debug("inside branchesList clear");
            branchesList.clear();
        }
        branchesList.addAll(branchService.getBranchesList());

        if (CollectionUtils.isNotEmpty(branchesList)) {
            logger.debug("branchList.size() : " + branchesList.size());
            datatableRendered = true;
            recordsCount = branchesList.size();
        }
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

    /**
     * @return the branchOrganizationName
     */
    public String getBranchOrganizationName() {
        return branchOrganizationName;
    }

    /**
     * @param branchOrganizationName the branchOrganizationName to set
     */
    public void setBranchOrganizationName(String branchOrganizationName) {
        this.branchOrganizationName = branchOrganizationName;
    }

    /**
     * @return the branchState
     */
    public String getBranchState() {
        return branchState;
    }

    /**
     * @param branchState the branchState to set
     */
    public void setBranchState(String branchState) {
        this.branchState = branchState;
    }

    /**
     * @return the branchCity
     */
    public String getBranchCity() {
        return branchCity;
    }

    /**
     * @param branchCity the branchCity to set
     */
    public void setBranchCity(String branchCity) {
        this.branchCity = branchCity;
    }

    /**
     * @return the selectedCountry
     */
    public Countries getSelectedCountry() {
        return selectedCountry;
    }

    /**
     * @param selectedCountry the selectedCountry to set
     */
    public void setSelectedCountry(Countries selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    /**
     * @return the branchPhoneNumber
     */
    public String getBranchPhoneNumber() {
        return branchPhoneNumber;
    }

    /**
     * @param branchPhoneNumber the branchPhoneNumber to set
     */
    public void setBranchPhoneNumber(String branchPhoneNumber) {
        this.branchPhoneNumber = branchPhoneNumber;
    }

    /**
     * @return the branchEmail
     */
    public String getBranchEmail() {
        return branchEmail;
    }

    /**
     * @param branchEmail the branchEmail to set
     */
    public void setBranchEmail(String branchEmail) {
        this.branchEmail = branchEmail;
    }

    /**
     * @return the branchCountry
     */
    public String getBranchCountry() {
        return branchCountry;
    }

    /**
     * @param branchCountry the branchCountry to set
     */
    public void setBranchCountry(String branchCountry) {
        this.branchCountry = branchCountry;
    }

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param branchName the branchName to set
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * @return the branchesList
     */
    public List<Branches> getBranchesList() {
        return branchesList;
    }

    /**
     * @param branchesList the branchesList to set
     */
    public void setBranchesList(List<Branches> branchesList) {
        this.branchesList = branchesList;
    }

    /**
     * @return the selectedBranch
     */
    public Branches getSelectedBranch() {
        return selectedBranch;
    }

    /**
     * @param selectedBranch the selectedBranch to set
     */
    public void setSelectedBranch(Branches selectedBranch) {
        this.selectedBranch = selectedBranch;
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

}
