package com.web.carex.clinicmanagement;

import com.module.carex.clinicmanagement.IDoctorService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.carex.clinicmanagement.Doctor;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.web.carex.appgeneral.CarexManagedBean;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named("doctorBean")
@Scope("session")
public class DoctorBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private IDoctorService doctorService;

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IUserAdministrationService userAdministrationService;

    private List<Doctor> doctorList = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();
    private Doctor doctor = new Doctor();
    private Doctor selectedDoctor;
    private Integer selectedOrganizationId;
    private Integer formOrganizationId;
    private Integer selectedUserId;
    private boolean addOperation = true;
    private boolean initialized;
    private boolean doctorResultsLoaded;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        selectedOrganizationId = resolveDefaultOrganizationId(organizationList, selectedOrganizationId);
        if (formOrganizationId == null) {
            formOrganizationId = selectedOrganizationId;
        }
        doctorList = new ArrayList<>();
        doctorResultsLoaded = false;
        initialized = true;
    }

    public void onOrganizationChange() {
        doctorList = new ArrayList<>();
        doctorResultsLoaded = false;
        PrimeFaces.current().ajax().update("form:doctorMainPanel", "form:messages");
    }

    public void onFormOrganizationChange() {
        selectedUserId = null;
        PrimeFaces.current().ajax().update("form:doctorDialogPanel");
    }

    public void addButtonAction() {
        addOperation = true;
        doctor = new Doctor();
        doctor.setActive(true);
        doctor.setConsultationFee(BigDecimal.ZERO);
        doctor.setConsultationDurationMinutes(15);
        formOrganizationId = selectedOrganizationId;
        selectedUserId = null;
        selectedDoctor = null;
    }

    public void confirmEditButtonAction() {
        if (selectedDoctor == null) {
            return;
        }
        addOperation = false;
        doctor = new Doctor();
        doctor.setId(selectedDoctor.getId());
        doctor.setOrganization(selectedDoctor.getOrganization());
        doctor.setDoctorCode(selectedDoctor.getDoctorCode());
        doctor.setDoctorName(selectedDoctor.getDoctorName());
        doctor.setQualification(selectedDoctor.getQualification());
        doctor.setSpecialization(selectedDoctor.getSpecialization());
        doctor.setLicenseNumber(selectedDoctor.getLicenseNumber());
        doctor.setPhoneNumber(selectedDoctor.getPhoneNumber());
        doctor.setEmailAddress(selectedDoctor.getEmailAddress());
        doctor.setGender(selectedDoctor.getGender());
        doctor.setConsultationFee(selectedDoctor.getConsultationFee());
        doctor.setExperienceYears(selectedDoctor.getExperienceYears());
        doctor.setConsultationDurationMinutes(selectedDoctor.getConsultationDurationMinutes());
        doctor.setActive(selectedDoctor.isActive());
        doctor.setNotes(selectedDoctor.getNotes());
        formOrganizationId = selectedDoctor.getOrganization() == null ? selectedOrganizationId : selectedDoctor.getOrganization().getId();
        selectedUserId = selectedDoctor.getUserDetail() == null ? null : selectedDoctor.getUserDetail().getUserId();
    }

    public void saveDoctor() {
        if (!validateForm()) {
            PrimeFaces.current().ajax().update("form:dialogMessages");
            return;
        }
        Organizations organization = organizationService.getOrganizationById(formOrganizationId);
        if (organization == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization missing", "Select a valid organization.");
            return;
        }
        doctor.setOrganization(organization);
        doctor.setUserDetail(resolveSelectedUser());
        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;
        if (addOperation) {
            userActivityTO.setActivityType("Add");
            result = doctorService.addDoctor(userActivityTO, doctor);
        } else {
            userActivityTO.setActivityType("Update");
            result = doctorService.updateDoctor(userActivityTO, doctor);
        }
        handleSaveResult(result);
    }

    public void confirmDeleteDoctor() {
        if (selectedDoctor == null) {
            return;
        }
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = doctorService.deleteDoctor(userActivityTO, selectedDoctor);
        switch (result) {
            case SUCCESSFUL:
                addMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Doctor removed successfully.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Doctor does not exist anymore.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", "Unable to remove doctor.");
                break;
        }
        fetchDoctorList();
        PrimeFaces.current().ajax().update("form:doctorMainPanel", "form:messages");
    }

    public void fetchDoctorList() {
        doctorList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(doctorService.getDoctorsByOrganizationId(selectedOrganizationId));
        doctorResultsLoaded = true;
    }

    public List<String> getAvailableGenders() {
        return Arrays.asList("Male", "Female", "Other");
    }

    public List<UserDetails> getAvailableUsers() {
        List<UserDetails> allUsers = userAdministrationService.getUserDetailsList();
        List<UserDetails> eligibleUsers = new ArrayList<>();
        for (UserDetails user : allUsers) {
            if (user == null || user.getOrganization() == null || selectedOrganizationId == null) {
                continue;
            }
            if (!formOrganizationMatches(user.getOrganization().getId())) {
                continue;
            }
            boolean sameLinkedUser = selectedUserId != null && user.getUserId() == selectedUserId;
            boolean doctorRole = user.getRole() != null
                    && user.getRole().getRoleName() != null
                    && user.getRole().getRoleName().trim().toLowerCase().contains("doctor");
            boolean alreadyLinked = isUserAlreadyLinked(user.getUserId());
            if (sameLinkedUser || !alreadyLinked) {
                if (doctorRole || selectedUserId == null || sameLinkedUser) {
                    eligibleUsers.add(user);
                }
            }
        }
        if (eligibleUsers.isEmpty()) {
            for (UserDetails user : allUsers) {
                if (user != null && user.getOrganization() != null && formOrganizationMatches(user.getOrganization().getId())) {
                    boolean sameLinkedUser = selectedUserId != null && user.getUserId() == selectedUserId;
                    if (sameLinkedUser || !isUserAlreadyLinked(user.getUserId())) {
                        eligibleUsers.add(user);
                    }
                }
            }
        }
        return eligibleUsers;
    }

    public List<SelectItem> getAvailableUserSelectItems() {
        List<SelectItem> selectItems = new ArrayList<>();
        for (UserDetails user : getAvailableUsers()) {
            String roleName = user.getRole() == null ? "" : user.getRole().getRoleName();
            String label = user.getUserName() + " (" + safeText(user.getEmailId()) + ")" + (isBlank(roleName) ? "" : " - " + roleName);
            selectItems.add(new SelectItem(user.getUserId(), label));
        }
        return selectItems;
    }

    public List<Doctor> getDoctorList() { return doctorList; }
    public List<Organizations> getOrganizationList() { return organizationList; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Doctor getSelectedDoctor() { return selectedDoctor; }
    public void setSelectedDoctor(Doctor selectedDoctor) { this.selectedDoctor = selectedDoctor; }
    public Integer getSelectedOrganizationId() { return selectedOrganizationId; }
    public void setSelectedOrganizationId(Integer selectedOrganizationId) { this.selectedOrganizationId = resolveAccessibleOrganizationId(selectedOrganizationId); }
    public Integer getFormOrganizationId() { return formOrganizationId; }
    public void setFormOrganizationId(Integer formOrganizationId) { this.formOrganizationId = resolveAccessibleOrganizationId(formOrganizationId); }
    public Integer getSelectedUserId() { return selectedUserId; }
    public void setSelectedUserId(Integer selectedUserId) { this.selectedUserId = selectedUserId; }
    public boolean isAddOperation() { return addOperation; }
    public boolean isDoctorResultsLoaded() { return doctorResultsLoaded; }

    private boolean validateForm() {
        if (formOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return false;
        }
        if (isBlank(doctor.getDoctorCode())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Doctor code required", "Enter doctor code.");
            return false;
        }
        if (isBlank(doctor.getDoctorName())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Doctor name required", "Enter doctor name.");
            return false;
        }
        if (isBlank(doctor.getPhoneNumber())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Phone required", "Enter phone number.");
            return false;
        }
        if (!isBlank(doctor.getEmailAddress()) && !doctor.getEmailAddress().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid email", "Enter a valid email address.");
            return false;
        }
        UserDetails selectedUser = resolveSelectedUser();
        if (selectedUser != null && (selectedUser.getOrganization() == null
                || !formOrganizationId.equals(selectedUser.getOrganization().getId()))) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid linked user", "Selected user must belong to the same organization.");
            return false;
        }
        if (doctor.getConsultationFee() != null && doctor.getConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid fee", "Consultation fee cannot be negative.");
            return false;
        }
        if (doctor.getExperienceYears() != null && doctor.getExperienceYears() < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid experience", "Experience years cannot be negative.");
            return false;
        }
        if (doctor.getConsultationDurationMinutes() != null && doctor.getConsultationDurationMinutes() <= 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid duration", "Consultation duration must be greater than zero.");
            return false;
        }
        return true;
    }

    private void handleSaveResult(GeneralConstants result) {
        switch (result) {
            case SUCCESSFUL:
                addMessage(FacesMessage.SEVERITY_INFO, "Saved", addOperation ? "Doctor added successfully." : "Doctor updated successfully.");
                fetchDoctorList();
                PrimeFaces.current().executeScript("PF('manageDoctorDialog').hide()");
                break;
            case ENTRY_ALREADY_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Duplicate", "Doctor code already exists.");
                break;
            case ENTRY_IN_USE:
                addMessage(FacesMessage.SEVERITY_WARN, "User already linked", "Selected user is already mapped to another doctor.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Doctor does not exist anymore.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Save failed", "Unable to save doctor.");
                break;
        }
        PrimeFaces.current().ajax().update("form:doctorMainPanel", "form:messages", "form:dialogMessages");
    }

    private UserActivityTO populateUserActivityTO() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        UserActivityTO userActivityTO = new UserActivityTO();
        userActivityTO.setUserId(sessionMap.get("userAccountId") instanceof Integer ? (Integer) sessionMap.get("userAccountId") : 0);
        userActivityTO.setUserName((String) sessionMap.get("username"));
        userActivityTO.setIpAddress((String) sessionMap.get("Machine IP"));
        userActivityTO.setDeviceInfo((String) sessionMap.get("Machine Name"));
        userActivityTO.setLocationInfo((String) sessionMap.get("browserClientInfo"));
        userActivityTO.setCreatedAt(new Date());
        return userActivityTO;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean formOrganizationMatches(Integer organizationId) {
        return formOrganizationId != null && formOrganizationId.equals(organizationId);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private UserDetails resolveSelectedUser() {
        if (selectedUserId == null) {
            return null;
        }
        return userAdministrationService.getUserDetailById(selectedUserId);
    }

    private boolean isUserAlreadyLinked(Integer userId) {
        if (userId == null) {
            return false;
        }
        for (Doctor existingDoctor : doctorList) {
            if (existingDoctor.getUserDetail() != null && existingDoctor.getUserDetail().getUserId() == userId) {
                return true;
            }
        }
        return false;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
