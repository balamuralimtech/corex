package com.web.coretix.license;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.license.ILicenseService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.coretix.appgeneral.GenericManagedBean;
import com.web.coretix.constants.CoreAppModule;
import com.web.coretix.constants.LicenseManagementModule;
import com.web.coretix.constants.RolePrivilegeConstants;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Named("licenseBean")
@Scope("session")
public class LicenseBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 13543439334535436L;
    private final Logger logger = Logger.getLogger(getClass());

    private List<Licenses> licenseList = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();
    private ResourceBundle resourceBundle;

    private String organizationName;
    private java.util.Date startDate;
    private java.util.Date endDate;
    private String remarks;

    private boolean isAddOperation;
    private boolean datatableRendered;
    private int recordsCount;

    private Licenses selectedLicense = new Licenses();

    @Inject
    private ILicenseService licenseService;

    @Inject
    private IOrganizationService organizationService;

    public void initializePageAttributes() {
        resourceBundle = ResourceBundle.getBundle("messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        isAddOperation = true;
        datatableRendered = false;
        recordsCount = 0;

        if (CollectionUtils.isNotEmpty(licenseList)) {
            licenseList.clear();
        }
        if (CollectionUtils.isNotEmpty(organizationList)) {
            organizationList.clear();
        }

        fetchRolePrivilegeList();
        PrimeFaces.current().ajax().update("form:licenseMainPanelId");
    }

    private void fetchRolePrivilegeList() {
        List<RolePrivilegeConstants> privilegeConstantsList = getModulePrivilegeList(CoreAppModule.LICENCE.getId(), LicenseManagementModule.LICENSE.getId());
        if (CollectionUtils.isNotEmpty(privilegeConstantsList)) {
            for (RolePrivilegeConstants privilegeConstants : privilegeConstantsList) {
                logger.debug("License privilege : " + privilegeConstants);
            }
        }
    }

    private void resetFields() {
        organizationName = "";
        startDate = null;
        endDate = null;
        remarks = "";
    }

    public void addButtonAction() {
        isAddOperation = true;
        selectedLicense = new Licenses();
        resetFields();
    }

    public void searchButtonAction() {
        fetchLicenseList();
    }

    public void confirmEditButtonAction() {
        editLicense();
    }

    private void editLicense() {
        isAddOperation = false;
        if (selectedLicense != null) {
            organizationName = selectedLicense.getOrganization() == null ? "" : selectedLicense.getOrganization().getOrganizationName();
            startDate = selectedLicense.getStartDate();
            endDate = selectedLicense.getEndDate();
            remarks = selectedLicense.getRemarks();
        }
    }

    public List<String> completeOrganization(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> organizations = getOrganizations().stream()
                .map(Organizations::getOrganizationName)
                .collect(Collectors.toList());
        return organizations.stream()
                .filter(t -> t.toLowerCase().startsWith(queryLowerCase))
                .collect(Collectors.toList());
    }

    public void saveLicense() {
        if (StringUtils.isBlank(organizationName)) {
            addErrorMessage("Organization is required");
            return;
        }
        if (startDate == null) {
            addErrorMessage("Start date is required");
            return;
        }
        if (endDate == null) {
            addErrorMessage("End date is required");
            return;
        }
        if (endDate.before(startDate)) {
            addErrorMessage("End date should be greater than or equal to start date");
            return;
        }

        Organizations organization = getOrganizationEntityByName(organizationName);
        if (organization == null) {
            addErrorMessage("Selected organization does not exist");
            return;
        }

        Licenses license = new Licenses();
        license.setOrganization(organization);
        license.setStartDate(new Date(startDate.getTime()));
        license.setEndDate(new Date(endDate.getTime()));
        license.setRemarks(remarks);

        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants status;
        if (isAddOperation) {
            userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
            status = licenseService.addLicense(userActivityTO, license);
            handleSaveStatus(status, true);
        } else {
            userActivityTO.setActivityType(UserActivityConstants.UPDATE.getValue());
            license.setId(selectedLicense.getId());
            status = licenseService.updateLicense(userActivityTO, license);
            handleSaveStatus(status, false);
        }

        if (status == GeneralConstants.SUCCESSFUL) {
            fetchLicenseList();
            PrimeFaces.current().executeScript("PF('manageLicenseDialog').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:licenseDataTableId");
        }
    }

    private void handleSaveStatus(GeneralConstants status, boolean addOperation) {
        switch (status) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(addOperation
                                ? resourceBundle.getString("licenseAddedSuccessfullyLabel")
                                : resourceBundle.getString("licenseUpdatedSuccessfullyLabel")));
                break;
            case ENTRY_ALREADY_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                                resourceBundle.getString("licenseAlreadyExistsLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                                resourceBundle.getString("licenseDoesNotExistLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"),
                                addOperation
                                        ? resourceBundle.getString("licenseAddFailedLabel")
                                        : resourceBundle.getString("licenseUpdateFailedLabel")));
                break;
        }
    }

    public void confirmDeleteLicense() {
        if (selectedLicense == null) {
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.DELETE.getValue());
        GeneralConstants deleteStatus = licenseService.deleteLicense(userActivityTO, selectedLicense);
        switch (deleteStatus) {
            case SUCCESSFUL:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(resourceBundle.getString("licenseDeletedSuccessfullyLabel")));
                break;
            case ENTRY_NOT_EXISTS:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, resourceBundle.getString("warningLabel"),
                                resourceBundle.getString("licenseDoesNotExistLabel")));
                break;
            default:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"),
                                resourceBundle.getString("licenseRemovalFailedLabel")));
                break;
        }

        fetchLicenseList();
        PrimeFaces.current().ajax().update("form:messages", "form:licenseDataTableId");
    }

    private void fetchLicenseList() {
        datatableRendered = false;
        if (CollectionUtils.isNotEmpty(licenseList)) {
            licenseList.clear();
        }
        licenseList.addAll(licenseService.getLicenseList());
        if (CollectionUtils.isNotEmpty(licenseList)) {
            datatableRendered = true;
            recordsCount = licenseList.size();
        }
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("errorLabel"), detail));
        PrimeFaces.current().ajax().update("form:messages", "form:addEditLicensePanelId");
    }

    private List<Organizations> getOrganizations() {
        if (CollectionUtils.isEmpty(organizationList)) {
            organizationList.addAll(organizationService.getOrganizationsList());
        }
        return organizationList;
    }

    private Organizations getOrganizationEntityByName(String inputOrganizationName) {
        for (Organizations organization : getOrganizations()) {
            if (organization.getOrganizationName().equalsIgnoreCase(inputOrganizationName)) {
                return organization;
            }
        }
        return null;
    }

    public UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        UserActivityTO userActivityTO = new UserActivityTO();

        if (httpSession != null) {
            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }

    public String getFormattedDate(java.util.Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public String getLicenseStatus(Licenses license) {
        return license != null && license.isActive() ? "Active" : "Expired";
    }

    public List<Licenses> getLicenseList() {
        return licenseList;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public java.util.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public Licenses getSelectedLicense() {
        return selectedLicense;
    }

    public void setSelectedLicense(Licenses selectedLicense) {
        this.selectedLicense = selectedLicense;
    }
}
