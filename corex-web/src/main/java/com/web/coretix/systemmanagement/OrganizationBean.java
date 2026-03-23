/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.web.coretix.systemmanagement;

import com.module.coretix.systemmanagement.ICityService;
import com.module.coretix.systemmanagement.IStateService;
import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Cities;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.module.coretix.systemmanagement.ICountryService;
import com.module.coretix.systemmanagement.IOrganizationService;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.systemmanagement.States;
import com.web.coretix.appgeneral.GenericManagedBean;
import com.web.coretix.constants.*;
import org.primefaces.PrimeFaces;

import java.io.ByteArrayInputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.CroppedImage;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author admin
 */
@Named("organizationBean")
@Scope("session")
public class OrganizationBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 1354353434334535435L;
    private static final Logger logger = LoggerFactory.getLogger(OrganizationBean.class);
    private List<Organizations> organizationList = new ArrayList<>();
    private List<Countries> countriesList = new ArrayList<>();
    private ResourceBundle resourceBundle;

    private String organizationName;
    private Countries selectedCountry;
    private String organizationCountry;
    private String organizationState;
    private String organizationCity;
    private String organizationPhoneNumber;
    private String organizationWebsite;

    private boolean isAddOperation;
    private boolean datatableRendered;

    private int recordsCount;

    // Field validation flags
    private boolean nameError = false;
    private boolean countryError = false;
    private boolean stateError = false;
    private boolean cityError = false;
    private boolean phoneError = false;

    private CroppedImage croppedImage;

    private UploadedFile organizationLogoImageFile;

    private Organizations selectedOrganization = new Organizations();

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private ICountryService countryService;

    @Inject
    private IStateService stateService;

    @Inject
    private ICityService cityService;

    /**
     * @return the organizationList
     */
    public List<Organizations> getOrganizationList() {
        return organizationList;
    }

    /**
     * @return the countriesList
     */
    public List<Countries> getCountriesList() {
        return countriesList;
    }

    public void initializePageAttributes() {
        logger.debug("entered into initializePageAttributes !!!");

        resourceBundle = ResourceBundle.getBundle("messages",FacesContext.getCurrentInstance().getViewRoot().getLocale());

        logger.debug("username : "+fetchCurrentUsername());
        logger.debug("user id : "+fetchCurrentUserId());
        logger.debug("role id :"+fetchCurrentUserRoleId());

        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        if (CollectionUtils.isNotEmpty(countriesList)) {
            logger.debug("inside  countriesList clear");
            countriesList.clear();
        }
        if (CollectionUtils.isNotEmpty(organizationList)) {
            logger.debug("inside  organizationList clear");
            organizationList.clear();
        }
        fetchRolePrivilegeList();

        PrimeFaces.current().ajax().update("form:orgMainPanelId");
        logger.debug("end of initializePageAttributes !!!");
    }

    private void fetchRolePrivilegeList()
    {
        logger.debug("entered into fetchRolePrivilegeList !!!");
        List<RolePrivilegeConstants> privilegeConstantsList = getModulePrivilegeList(CoreAppModule.SYSTEM_MANAGEMENT.getId(), SystemManagementModule.ORGANIZATION.getId());

        if (CollectionUtils.isNotEmpty(privilegeConstantsList)) {

            for (RolePrivilegeConstants privilegeConstants : privilegeConstantsList) {
                switch (privilegeConstants){
                    case VIEW:
                        logger.debug("PrivilegeConstants.VIEW : " + privilegeConstants);
                        break;
                    case ADD:
                        logger.debug("PrivilegeConstants.ADD : " + privilegeConstants);
                        break;
                    case EDIT:
                        logger.debug("PrivilegeConstants.EDIT : " + privilegeConstants);
                        break;
                    case DELETE:
                        logger.debug("PrivilegeConstants.DELETE : " + privilegeConstants);
                        break;
                    case EXPORT:
                        logger.debug("PrivilegeConstants.EXPORT : " + privilegeConstants);
                        break;
                    default:
                        logger.debug("PrivilegeConstants.DEFAULT : " + privilegeConstants);
                }
            }

        }
        else {
            logger.debug("privilegeConstantsList is null or empty !!!");
        }
        logger.debug("end of fetchRolePrivilegeList !!!");
    }

    private void resetFields() {
        logger.debug("entered into resetFields action !!!");
        organizationCountry = "";
        organizationName = "";
        organizationState = "";
        organizationCity = "";
        organizationPhoneNumber = "";
        organizationWebsite = "";
        organizationLogoImageFile = null;
        croppedImage = null;

        // Reset error flags
        resetErrorFlags();
    }

    private void resetErrorFlags() {
        nameError = false;
        countryError = false;
        stateError = false;
        cityError = false;
        phoneError = false;
    }

    public void addButtonAction() {
        logger.debug("entered into add button action !!!");
        isAddOperation = true;
        resetFields();
    }

    public void searchButtonAction() {
        logger.debug("entered into searchButtonAction !!!");
        fetchOrganizationList();
        logger.debug("end of searchButtonAction !!!");
    }

    public void confirmEditButtonAction() {
        editOrganization();
    }

    private void editOrganization() {
        logger.debug("entered into edit button action !!!");
        isAddOperation = false;

        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("selectedOrganization.getId() : " + selectedOrganization.getId());

        organizationName = selectedOrganization.getOrganizationName();
        organizationCountry = selectedOrganization.getCountry().getName();
        organizationState = selectedOrganization.getState();
        organizationCity = selectedOrganization.getCity();
        organizationPhoneNumber = selectedOrganization.getPhoneNumber();
        organizationWebsite = selectedOrganization.getWebsite();
       // organizationLogoImageFile = selectedOrganization.getImage();

//        if (selectedOrganization.getImage() != null) {
//            organizationLogoImageBase64 = encodeImageToBase64(selectedOrganization.getImage());
//            logger.debug("Image successfully loaded for editing");
//        } else {
//            organizationLogoImageBase64 = null;
//            logger.debug("No image available for the selected organization");
//        }


        logger.debug("organizationName : " + organizationName);
        logger.debug("organizationCountry : " + organizationCountry);
        logger.debug("organizationState : " + organizationState);
        logger.debug("organizationCity : " + organizationCity);
        logger.debug("organizationPhoneNumber : " + organizationPhoneNumber);
        logger.debug("organizationWebsite : " + organizationWebsite);
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

        Countries tempCountry = getCountriesByCountryName(organizationCountry);
        List<States> states = stateService.getStatesListByCountryId(tempCountry.getId());
        for (States state : states) {
            stateList.add(state.getName());
        }

        return stateList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public void onStateSelect()
    {
        logger.debug("entered into onStateSelect !!!"+organizationState);


    }

    public List<String> completeCity(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> cityList = new ArrayList<>();
        Countries tempCountry = getCountriesByCountryName(organizationCountry);
        States tempStates = getStateEntityByStateName(organizationState);
        List<Cities> cities = cityService.getCitiesListByCountryIdAndStateId(tempCountry.getId(), tempStates.getId());
        for (Cities city : cities) {
            cityList.add(city.getName());
        }

        return cityList.stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public void saveOrganization() {

        logger.debug("Inside save organization method ");
        logger.debug("organizationName : " + organizationName);
        logger.debug("organizationCountry : " + organizationCountry);
        logger.debug("organizationState : " + organizationState);
        logger.debug("organizationCity : " + organizationCity);
        logger.debug("organizationPhoneNumber : " + organizationPhoneNumber);
        logger.debug("organizationWebsite : " + organizationWebsite);
        logger.debug("isAddOperation : " + isAddOperation);
        logger.debug("organizationLogoImageFile: " + organizationLogoImageFile);

        // Reset error flags before validation
        resetErrorFlags();
        boolean hasErrors = false;
        List<String> errorFieldIds = new ArrayList<>();

        // Validation
        if (organizationName == null || organizationName.trim().isEmpty()) {
            logger.debug("organizationName is null or empty");
            nameError = true;
            hasErrors = true;
            errorFieldIds.add("form:name");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Organization name is required"));
        }

        if (organizationCountry == null || organizationCountry.trim().isEmpty()) {
            logger.debug("organizationCountry is null or empty");
            countryError = true;
            hasErrors = true;
            errorFieldIds.add("form:countrylist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Country is required"));
        }

        if (organizationState == null || organizationState.trim().isEmpty()) {
            logger.debug("organizationState is null or empty");
            stateError = true;
            hasErrors = true;
            errorFieldIds.add("form:statelist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "State is required"));
        }

        if (organizationCity == null || organizationCity.trim().isEmpty()) {
            logger.debug("organizationCity is null or empty");
            cityError = true;
            hasErrors = true;
            errorFieldIds.add("form:citylist");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "City is required"));
        }

        if (organizationPhoneNumber == null || organizationPhoneNumber.trim().isEmpty()) {
            logger.debug("organizationPhoneNumber is null or empty");
            phoneError = true;
            hasErrors = true;
            errorFieldIds.add("form:phonenumber");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    resourceBundle.getString("errorLabel"),
                    "Phone number is required"));
        }

        // If there are validation errors, trigger visual effects
        if (hasErrors) {
            String fieldIdsJson = String.join(",", errorFieldIds);
            PrimeFaces.current().executeScript("highlightErrorFields(['" + String.join("','", errorFieldIds) + "']);");
            return;
        }

        logger.debug("crossed validation !!!!!!!!!!!");

        Organizations org = new Organizations();

        org.setOrganizationName(organizationName);
        Countries addCountries = getCountriesByCountryName(organizationCountry);
        logger.debug("country name " + addCountries.getName());
        if (addCountries != null) {
            org.setCountry(addCountries);
        }
        org.setCity(organizationCity);
        org.setState(organizationState);
        org.setPhoneNumber(organizationPhoneNumber);
        org.setWebsite(organizationWebsite);

        // Set the uploaded image (if available)
        if (organizationLogoImageFile != null && organizationLogoImageFile.getContent() != null) {
            org.setImage(organizationLogoImageFile.getContent());
        }


        UserActivityTO userActivityTO = populateUserActivityTO();

        if (isAddOperation) {

            logger.debug("if (isAddOperation) {");
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            userActivityTO.setActivityDescription(org.getOrganizationName()+ " - New Organization Added");
            userActivityTO.setCreatedAt(new Date());
            GeneralConstants addStatus = organizationService.addOrganization(userActivityTO, org);
            switch (addStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("organizationAddedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("organizationAlreadyExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("addingOrganizationFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        } else {
            logger.debug("else  edit operation !!");
            logger.debug("selectedOrganization.getId() : " + selectedOrganization.getId());
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            userActivityTO.setActivityDescription("Existing Organization "+org.getOrganizationName()+" Updated");
            org.setId(selectedOrganization.getId());
            GeneralConstants updateStatus = organizationService.updateOrganization(userActivityTO, org);
            switch (updateStatus) {
                case SUCCESSFUL:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("organizationUpdatedSuccessfullyLabel")));
                    break;
                case ENTRY_ALREADY_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("organizationAlreadyExistsLabel")));
                    break;
                case ENTRY_NOT_EXISTS:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,resourceBundle.getString("warningLabel"), resourceBundle.getString("organizationNotExistsLabel")));
                    break;
                case FAILED:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("organizationUpdateFailedLabel")));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                    break;
            }
        }
        fetchOrganizationList();
        PrimeFaces.current().executeScript("PF('manageOrgDialog').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:orgDataTableId");
    }

    public UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

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

    public void confirmDeleteOrganization() {
        deleteOrganization();
    }

    private void deleteOrganization() {
        logger.debug("inside delete organization ");
        logger.debug("selectedOrganization.getId() : " + selectedOrganization.getId());

        organizationName = selectedOrganization.getOrganizationName();
        organizationCountry = selectedOrganization.getCountry().getName();
        organizationState = selectedOrganization.getState();
        organizationCity = selectedOrganization.getCity();
        organizationPhoneNumber = selectedOrganization.getPhoneNumber();
        organizationWebsite = selectedOrganization.getWebsite();

        logger.debug("organizationName : " + organizationName);
        logger.debug("organizationCountry : " + organizationCountry);
        logger.debug("organizationState : " + organizationState);
        logger.debug("organizationCity : " + organizationCity);
        logger.debug("organizationPhoneNumber : " + organizationPhoneNumber);
        logger.debug("organizationWebsite : " + organizationWebsite);
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        userActivityTO.setActivityDescription(" Organization "+organizationName+" Deleted");

        GeneralConstants deleteStatus = organizationService.deleteOrganization(userActivityTO, getSelectedOrganization());
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("organizationDeletedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"), resourceBundle.getString("organizationDoestNotExistLabel")));
                break;
            case FAILED:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("organizationRemovalFailedLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), resourceBundle.getString("unexpectedErrorLabel")));
                break;
        }
        
        fetchOrganizationList();
        PrimeFaces.current().ajax().update("form:messages", "form:orgDataTableId");
    }

    private void fetchOrganizationList() {
        datatableRendered = false;
        logger.debug("inside fetchOrganizationList ");
        if (CollectionUtils.isNotEmpty(organizationList)) {
            logger.debug("inside fetchOrganizationList clear");
            organizationList.clear();
        }
        organizationList.addAll(organizationService.getOrganizationsList());

        if (CollectionUtils.isNotEmpty(organizationList)) {
            logger.debug("organizationList.size() : " + organizationList.size());
            datatableRendered = true;
            recordsCount = organizationList.size();
        }
    }

    public void onOrgSelect()
    {
        logger.debug("inside onOrgSelect !!!!");
        logger.debug("organization : "+organizationName);
        logger.debug("country : "+organizationCountry);
        logger.debug("state : "+organizationState);


    }

    private Countries getCountriesByCountryName(String countryName) {
        return countryService.getCountryEntityByCountryName(countryName);
    }

    private States getStateEntityByStateName(String stateName)
    {
        return stateService.getStateEntityByStateName(stateName);
    }


/////////////////////////// LOGO UPLOAD METHODS  //////////////////////////////

    public CroppedImage getCroppedImage() {
        return croppedImage;
    }

    public void setCroppedImage(CroppedImage croppedImage) {
        this.croppedImage = croppedImage;
    }



    public void handleFileUpload(FileUploadEvent event) {
        this.organizationLogoImageFile = null;
        this.croppedImage = null;
        UploadedFile file = event.getFile();
        if (file != null && file.getContent() != null && file.getContent().length > 0 && file.getFileName() != null) {
            this.organizationLogoImageFile = file;
            FacesMessage msg = new FacesMessage("Successful", this.organizationLogoImageFile.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void crop() {
        if (this.croppedImage == null || this.croppedImage.getBytes() == null || this.croppedImage.getBytes().length == 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Cropping failed."));
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                    "Cropped successfully."));
        }
    }

    public StreamedContent getImage() {
        if (organizationLogoImageFile == null
                || organizationLogoImageFile.getContent() == null
                || organizationLogoImageFile.getContent().length == 0) {
            return null;
        }

        return DefaultStreamedContent.builder()
                .contentType(organizationLogoImageFile.getContentType())
                .stream(() -> {
                    try {
                        return new ByteArrayInputStream(organizationLogoImageFile.getContent());
                    }
                    catch (Exception e) {
                        logger.error("Error reading organization logo image", e);
                        return null;
                    }
                })
                .build();
    }

    public StreamedContent getCropped() {
        return DefaultStreamedContent.builder()
                .contentType(organizationLogoImageFile == null ? null : organizationLogoImageFile.getContentType())
                .stream(() -> {
                    if (croppedImage == null
                            || croppedImage.getBytes() == null
                            || croppedImage.getBytes().length == 0) {
                        return null;
                    }

                    try {
                        return new ByteArrayInputStream(this.croppedImage.getBytes());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .build();
    }


    /**
     * @return the organizationName
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * @param organizationName the organizationName to set
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * @return the organizationState
     */
    public String getOrganizationState() {
        return organizationState;
    }

    /**
     * @param organizationState the organizationState to set
     */
    public void setOrganizationState(String organizationState) {
        this.organizationState = organizationState;
    }

    /**
     * @return the organizationCity
     */
    public String getOrganizationCity() {
        return organizationCity;
    }

    /**
     * @param organizationCity the organizationCity to set
     */
    public void setOrganizationCity(String organizationCity) {
        this.organizationCity = organizationCity;
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
     * @return the organizationPhoneNumber
     */
    public String getOrganizationPhoneNumber() {
        return organizationPhoneNumber;
    }

    /**
     * @param organizationPhoneNumber the organizationPhoneNumber to set
     */
    public void setOrganizationPhoneNumber(String organizationPhoneNumber) {
        this.organizationPhoneNumber = organizationPhoneNumber;
    }

    /**
     * @return the organizationWebsite
     */
    public String getOrganizationWebsite() {
        return organizationWebsite;
    }

    /**
     * @param organizationWebsite the organizationWebsite to set
     */
    public void setOrganizationWebsite(String organizationWebsite) {
        this.organizationWebsite = organizationWebsite;
    }

    /**
     * @return the organizationCountry
     */
    public String getOrganizationCountry() {
        return organizationCountry;
    }

    /**
     * @param organizationCountry the organizationCountry to set
     */
    public void setOrganizationCountry(String organizationCountry) {
        this.organizationCountry = organizationCountry;
    }

    /**
     * @return the selectedOrganization
     */
    public Organizations getSelectedOrganization() {
        return selectedOrganization;
    }

    /**
     * @param selectedOrganization the selectedOrganization to set
     */
    public void setSelectedOrganization(Organizations selectedOrganization) {
        this.selectedOrganization = selectedOrganization;
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

    // Getters for error flags
    public boolean isNameError() {
        return nameError;
    }

    public boolean isCountryError() {
        return countryError;
    }

    public boolean isStateError() {
        return stateError;
    }

    public boolean isCityError() {
        return cityError;
    }

    public boolean isPhoneError() {
        return phoneError;
    }

}

