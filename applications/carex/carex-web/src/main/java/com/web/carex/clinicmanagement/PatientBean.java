package com.web.carex.clinicmanagement;

import com.module.carex.clinicmanagement.IPatientService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Named("patientBean")
@Scope("session")
public class PatientBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private IPatientService patientService;

    @Inject
    private IOrganizationService organizationService;

    private List<Patient> patientList = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();
    private Patient patient = new Patient();
    private Patient selectedPatient;
    private Integer selectedOrganizationId;
    private Integer formOrganizationId;
    private java.util.Date patientDateOfBirth;
    private boolean addOperation = true;
    private boolean initialized;
    private boolean patientResultsLoaded;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        organizationList = new ArrayList<>(organizationService.getOrganizationsList());
        if (selectedOrganizationId == null) {
            selectedOrganizationId = resolveCurrentOrganizationId();
        }
        if (formOrganizationId == null) {
            formOrganizationId = selectedOrganizationId;
        }
        patientList = new ArrayList<>();
        patientResultsLoaded = false;
        initialized = true;
    }

    public void onOrganizationChange() {
        patientList = new ArrayList<>();
        patientResultsLoaded = false;
        PrimeFaces.current().ajax().update("form:patientMainPanel", "form:messages");
    }

    public void onFormOrganizationChange() {
        PrimeFaces.current().ajax().update("form:patientDialogPanel");
    }

    public void addButtonAction() {
        addOperation = true;
        patient = new Patient();
        patient.setActive(true);
        formOrganizationId = selectedOrganizationId;
        patientDateOfBirth = null;
        selectedPatient = null;
    }

    public void confirmEditButtonAction() {
        if (selectedPatient == null) {
            return;
        }
        addOperation = false;
        patient = new Patient();
        patient.setId(selectedPatient.getId());
        patient.setOrganization(selectedPatient.getOrganization());
        patient.setPatientCode(selectedPatient.getPatientCode());
        patient.setPatientName(selectedPatient.getPatientName());
        patient.setGender(selectedPatient.getGender());
        patient.setDateOfBirth(selectedPatient.getDateOfBirth());
        patient.setPhoneNumber(selectedPatient.getPhoneNumber());
        patient.setEmailAddress(selectedPatient.getEmailAddress());
        patient.setBloodGroup(selectedPatient.getBloodGroup());
        patient.setPatientIdProofNo(selectedPatient.getPatientIdProofNo());
        patient.setAddress(selectedPatient.getAddress());
        patient.setEmergencyContactName(selectedPatient.getEmergencyContactName());
        patient.setEmergencyContactNumber(selectedPatient.getEmergencyContactNumber());
        patient.setActive(selectedPatient.isActive());
        patient.setNotes(selectedPatient.getNotes());
        formOrganizationId = selectedPatient.getOrganization() == null ? selectedOrganizationId : selectedPatient.getOrganization().getId();
        patientDateOfBirth = selectedPatient.getDateOfBirth() == null ? null : new java.util.Date(selectedPatient.getDateOfBirth().getTime());
    }

    public void savePatient() {
        if (!validateForm()) {
            PrimeFaces.current().ajax().update("form:dialogMessages");
            return;
        }
        Organizations organization = organizationService.getOrganizationById(formOrganizationId);
        if (organization == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization missing", "Select a valid organization.");
            return;
        }
        patient.setOrganization(organization);
        patient.setDateOfBirth(patientDateOfBirth == null ? null : new java.sql.Date(patientDateOfBirth.getTime()));
        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;
        if (addOperation) {
            userActivityTO.setActivityType("Add");
            result = patientService.addPatient(userActivityTO, patient);
        } else {
            userActivityTO.setActivityType("Update");
            result = patientService.updatePatient(userActivityTO, patient);
        }
        handleSaveResult(result);
    }

    public void confirmDeletePatient() {
        if (selectedPatient == null) {
            return;
        }
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = patientService.deletePatient(userActivityTO, selectedPatient);
        switch (result) {
            case SUCCESSFUL:
                addMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Patient removed successfully.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Patient does not exist anymore.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", "Unable to remove patient.");
                break;
        }
        fetchPatientList();
        PrimeFaces.current().ajax().update("form:patientMainPanel", "form:messages");
    }

    public void fetchPatientList() {
        patientList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
        patientResultsLoaded = true;
    }

    public List<String> getAvailableGenders() { return Arrays.asList("Male", "Female", "Other"); }
    public List<String> getAvailableBloodGroups() { return Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"); }

    public Integer getPatientAge() {
        if (patientDateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - patientDateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
    }

    public List<Patient> getPatientList() { return patientList; }
    public List<Organizations> getOrganizationList() { return organizationList; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }
    public Integer getSelectedOrganizationId() { return selectedOrganizationId; }
    public void setSelectedOrganizationId(Integer selectedOrganizationId) { this.selectedOrganizationId = selectedOrganizationId; }
    public Integer getFormOrganizationId() { return formOrganizationId; }
    public void setFormOrganizationId(Integer formOrganizationId) { this.formOrganizationId = formOrganizationId; }
    public java.util.Date getPatientDateOfBirth() { return patientDateOfBirth; }
    public void setPatientDateOfBirth(java.util.Date patientDateOfBirth) { this.patientDateOfBirth = patientDateOfBirth; }
    public boolean isAddOperation() { return addOperation; }
    public boolean isPatientResultsLoaded() { return patientResultsLoaded; }

    private boolean validateForm() {
        if (formOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return false;
        }
        if (isBlank(patient.getPatientCode())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient code required", "Enter patient code.");
            return false;
        }
        if (isBlank(patient.getPatientName())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient name required", "Enter patient name.");
            return false;
        }
        if (isBlank(patient.getPhoneNumber())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Phone required", "Enter phone number.");
            return false;
        }
        if (patientDateOfBirth != null && patientDateOfBirth.after(new java.util.Date())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid date of birth", "Date of birth cannot be in the future.");
            return false;
        }
        if (!isBlank(patient.getEmailAddress()) && !patient.getEmailAddress().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid email", "Enter a valid email address.");
            return false;
        }
        return true;
    }

    private void handleSaveResult(GeneralConstants result) {
        switch (result) {
            case SUCCESSFUL:
                addMessage(FacesMessage.SEVERITY_INFO, "Saved", addOperation ? "Patient added successfully." : "Patient updated successfully.");
                fetchPatientList();
                PrimeFaces.current().executeScript("PF('managePatientDialog').hide()");
                break;
            case ENTRY_ALREADY_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Duplicate", "Patient code already exists.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Patient does not exist anymore.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Save failed", "Unable to save patient.");
                break;
        }
        PrimeFaces.current().ajax().update("form:patientMainPanel", "form:messages", "form:dialogMessages");
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
        userActivityTO.setCreatedAt(new java.util.Date());
        return userActivityTO;
    }

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        Object organizationId = facesContext.getExternalContext().getSessionMap().get("organizationId");
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
