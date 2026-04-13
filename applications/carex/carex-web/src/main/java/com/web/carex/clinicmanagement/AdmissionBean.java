package com.web.carex.clinicmanagement;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IDoctorService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.Doctor;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.settings.ClinicSettings;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.web.carex.appgeneral.CarexManagedBean;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Named("admissionBean")
@Scope("session")
public class AdmissionBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private transient IConsultationService consultationService;

    @Inject
    private transient IDoctorService doctorService;

    @Inject
    private transient IPatientService patientService;

    @Inject
    private transient IOrganizationService organizationService;

    @Inject
    private transient IClinicSettingsService clinicSettingsService;

    private List<Organizations> organizationList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private List<Patient> patientList = new ArrayList<>();
    private List<Consultation> consultationEntries = new ArrayList<>();

    private Consultation selectedConsultation;
    private Integer selectedOrganizationId;
    private Integer selectedDoctorId;
    private Integer selectedExistingPatientId;
    private Patient selectedExistingPatient;
    private boolean existingPatientMode = true;
    private boolean editMode;
    private boolean initialized;

    private String newPatientName;
    private String newPatientPhoneNumber;
    private String newPatientGender;
    private Integer patientAgeYears;
    private String temperatureCelsius;
    private String weightKg;
    private String bloodPressure;
    private String admissionNotes;
    private String selectedStatusFilter = "ALL";

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }

        organizationList = new ArrayList<>(getAccessibleOrganizations(organizationService));
        selectedOrganizationId = resolveDefaultOrganizationId(organizationList, selectedOrganizationId);
        loadScopedReferenceData();
        resetForm();
        refreshQueue();
        initialized = true;
    }

    public void onOrganizationChange() {
        loadScopedReferenceData();
        resetForm();
        refreshQueue();
        PrimeFaces.current().ajax().update("form:pageShell", "form:messages");
    }

    public void onPatientModeChange() {
        if (existingPatientMode) {
            if (selectedExistingPatient != null) {
                populatePatientFormFromEntity(selectedExistingPatient);
            }
        } else {
            selectedExistingPatientId = null;
            selectedExistingPatient = null;
        }
        PrimeFaces.current().ajax().update("form:admissionPanel");
    }

    public void onExistingPatientChange() {
        selectedExistingPatient = selectedExistingPatientId == null ? null : patientService.getPatientById(selectedExistingPatientId);
        populatePatientFormFromEntity(selectedExistingPatient);
        PrimeFaces.current().ajax().update("form:admissionPanel");
    }

    public void registerAdmission() {
        if (!isReceptionTokenWorkflowEnabled()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Admission disabled",
                    "This clinic is configured for direct doctor consultation without receptionist token registration.");
            PrimeFaces.current().ajax().update("form:messages", "form:pageShell", "form:heroPanel");
            return;
        }

        if (!validateForm()) {
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }

        Organizations organization = organizationService.getOrganizationById(selectedOrganizationId);
        Doctor doctor = findDoctor(selectedDoctorId);
        if (organization == null || doctor == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Missing details", "Select a valid organization and doctor.");
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }

        Patient patient = resolvePatientForAdmission(organization);
        if (patient == null) {
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }
        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;
        Consultation consultation;
        if (editMode && selectedConsultation != null) {
            consultation = consultationService.getConsultationById(selectedConsultation.getId());
            if (consultation == null || !canModifyAdmission(consultation)) {
                addMessage(FacesMessage.SEVERITY_WARN, "Admission unavailable", "This admission can no longer be edited.");
                refreshQueue();
                resetForm();
                PrimeFaces.current().ajax().update("form:pageShell", "form:messages", "form:heroPanel");
                return;
            }
            consultation.setOrganization(organization);
            consultation.setDoctor(doctor);
            consultation.setPatient(patient);
            consultation.setPatientAgeYears(patientAgeYears);
            consultation.setTemperatureCelsius(safeText(temperatureCelsius));
            consultation.setWeightKg(safeText(weightKg));
            consultation.setBloodPressure(safeText(bloodPressure));
            consultation.setSymptoms(safeText(admissionNotes));
            consultation.setVitals(buildVitalsSummary());
            userActivityTO.setActivityType("Update");
            result = consultationService.updateConsultation(userActivityTO, consultation);
        } else {
            consultation = buildQueuedConsultation(organization, doctor, patient);
            userActivityTO.setActivityType("Add");
            result = consultationService.addConsultation(userActivityTO, consultation);
        }
        if (result != GeneralConstants.SUCCESSFUL) {
            addMessage(FacesMessage.SEVERITY_ERROR, editMode ? "Update failed" : "Registration failed",
                    editMode ? "Unable to update the consultation admission." : "Unable to register the consultation admission.");
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }

        String detail;
        if (editMode) {
            detail = "Token " + consultation.getTokenNumber() + " was updated successfully before doctor assignment.";
        } else {
            boolean notificationSent = sendDoctorAndAdminNotifications(doctor, consultation, patient);
            detail = "Token " + consultation.getTokenNumber() + " has been assigned for " + patient.getPatientName() + ".";
            if (!notificationSent) {
                detail += " The selected doctor is not linked to an application user, so no direct message was delivered.";
            }
        }
        addMessage(FacesMessage.SEVERITY_INFO, editMode ? "Admission updated" : "Admission registered", detail);
        resetForm();
        refreshQueue();
        PrimeFaces.current().ajax().update("form:pageShell", "form:messages", "form:heroPanel");
    }

    public void editAdmission(Integer consultationId) {
        Consultation consultation = consultationService.getConsultationById(consultationId);
        if (consultation == null || !canModifyAdmission(consultation)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Admission unavailable", "Only waiting admissions can be edited.");
            PrimeFaces.current().ajax().update("form:messages", "form:consultationListingPanel");
            return;
        }

        selectedConsultation = consultation;
        editMode = true;
        existingPatientMode = true;
        selectedDoctorId = consultation.getDoctor() == null ? null : consultation.getDoctor().getId();
        selectedExistingPatientId = consultation.getPatient() == null ? null : consultation.getPatient().getId();
        selectedExistingPatient = selectedExistingPatientId == null ? null : patientService.getPatientById(selectedExistingPatientId);
        populatePatientFormFromEntity(selectedExistingPatient);
        patientAgeYears = consultation.getPatientAgeYears();
        temperatureCelsius = consultation.getTemperatureCelsius();
        weightKg = consultation.getWeightKg();
        bloodPressure = consultation.getBloodPressure();
        admissionNotes = consultation.getSymptoms();
        PrimeFaces.current().ajax().update("form:heroPanel", "form:admissionPanel", "form:messages");
    }

    public void deleteAdmission(Integer consultationId) {
        Consultation consultation = consultationService.getConsultationById(consultationId);
        if (consultation == null || !canModifyAdmission(consultation)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Admission unavailable", "Only waiting admissions can be deleted.");
            PrimeFaces.current().ajax().update("form:messages", "form:consultationListingPanel");
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = consultationService.deleteConsultation(userActivityTO, consultation);
        if (result == GeneralConstants.SUCCESSFUL) {
            addMessage(FacesMessage.SEVERITY_INFO, "Admission deleted", "Waiting consultation token " + consultation.getTokenNumber() + " was removed.");
            if (selectedConsultation != null && consultation.getId().equals(selectedConsultation.getId())) {
                resetForm();
            }
            refreshQueue();
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", "Unable to delete the selected admission.");
        }
        PrimeFaces.current().ajax().update("form:pageShell", "form:messages", "form:heroPanel");
    }

    public void cancelEdit() {
        resetForm();
        PrimeFaces.current().ajax().update("form:heroPanel", "form:admissionPanel", "form:messages");
    }

    public void refreshQueue() {
        consultationEntries = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId));
    }

    public List<Consultation> getFilteredConsultationEntries() {
        if ("ALL".equalsIgnoreCase(safeText(selectedStatusFilter))) {
            return consultationEntries;
        }

        List<Consultation> filteredEntries = new ArrayList<>();
        for (Consultation consultation : consultationEntries) {
            if (consultation == null) {
                continue;
            }
            if (selectedStatusFilter.equalsIgnoreCase(safeText(consultation.getStatus()))) {
                filteredEntries.add(consultation);
            }
        }
        return filteredEntries;
    }

    public List<String> getStatusFilterOptions() {
        return Arrays.asList("ALL", "Waiting", "In Progress", "Completed");
    }

    public List<String> getAvailableGenders() {
        return Arrays.asList("Male", "Female", "Other");
    }

    public String getSelectedDoctorName() {
        Doctor doctor = findDoctor(selectedDoctorId);
        return doctor == null ? "" : safeText(doctor.getDoctorName());
    }

    public String getSelectedExistingPatientPhoneNumber() {
        return selectedExistingPatient == null ? "" : safeText(selectedExistingPatient.getPhoneNumber());
    }

    public String getSelectedExistingPatientGender() {
        return selectedExistingPatient == null ? "" : safeText(selectedExistingPatient.getGender());
    }

    public String getSelectedExistingPatientBloodGroup() {
        return selectedExistingPatient == null ? "" : safeText(selectedExistingPatient.getBloodGroup());
    }

    public String getSelectedExistingPatientAgeText() {
        if (selectedExistingPatient == null || selectedExistingPatient.getDateOfBirth() == null) {
            return patientAgeYears == null ? "" : patientAgeYears + " yrs";
        }
        int years = Period.between(selectedExistingPatient.getDateOfBirth().toLocalDate(), LocalDate.now()).getYears();
        return years + " yrs";
    }

    public String getSelectedOrganizationName() {
        if (!isApplicationAdmin()) {
            return getCurrentOrganizationName();
        }
        for (Organizations organization : organizationList) {
            if (organization != null && selectedOrganizationId != null && selectedOrganizationId.equals(organization.getId())) {
                return safeText(organization.getOrganizationName());
            }
        }
        return "";
    }

    public boolean isReceptionTokenWorkflowEnabled() {
        if (selectedOrganizationId == null) {
            return true;
        }
        ClinicSettings settings = clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId);
        return settings == null || settings.isReceptionTokenWorkflowEnabled();
    }

    public String formatQueueTime(Consultation consultation) {
        if (consultation == null || consultation.getConsultationDate() == null) {
            return "-";
        }
        return consultation.getConsultationDate().toLocalDateTime().format(TIME_FORMATTER);
    }

    public String getQueueVitalsSummary(Consultation consultation) {
        if (consultation == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (consultation.getPatientAgeYears() != null) {
            parts.add(consultation.getPatientAgeYears() + " yrs");
        }
        if (!isBlank(consultation.getTemperatureCelsius())) {
            parts.add("Temp " + consultation.getTemperatureCelsius().trim());
        }
        if (!isBlank(consultation.getBloodPressure())) {
            parts.add("BP " + consultation.getBloodPressure().trim());
        }
        return String.join(" | ", parts);
    }

    public String queueSeverity(String status) {
        String normalized = safeText(status).toLowerCase();
        if ("waiting".equals(normalized)) {
            return "warning";
        }
        if ("in progress".equals(normalized)) {
            return "info";
        }
        return "success";
    }

    public boolean canModifyAdmission(Consultation consultation) {
        return consultation != null && "Waiting".equalsIgnoreCase(safeText(consultation.getStatus()));
    }

    public String getAdmissionActionLabel() {
        return editMode ? "Update Consultation" : "Register Consultation";
    }

    public List<Organizations> getOrganizationList() { return organizationList; }
    public List<Doctor> getDoctorList() { return doctorList; }
    public List<Patient> getPatientList() { return patientList; }
    public List<Consultation> getConsultationEntries() { return consultationEntries; }
    public List<Consultation> getTodaysQueue() { return consultationEntries; }
    public Integer getSelectedOrganizationId() { return selectedOrganizationId; }
    public void setSelectedOrganizationId(Integer selectedOrganizationId) { this.selectedOrganizationId = resolveAccessibleOrganizationId(selectedOrganizationId); }
    public Integer getSelectedDoctorId() { return selectedDoctorId; }
    public void setSelectedDoctorId(Integer selectedDoctorId) { this.selectedDoctorId = selectedDoctorId; }
    public Integer getSelectedExistingPatientId() { return selectedExistingPatientId; }
    public void setSelectedExistingPatientId(Integer selectedExistingPatientId) { this.selectedExistingPatientId = selectedExistingPatientId; }
    public Patient getSelectedExistingPatient() { return selectedExistingPatient; }
    public boolean isExistingPatientMode() { return existingPatientMode; }
    public void setExistingPatientMode(boolean existingPatientMode) { this.existingPatientMode = existingPatientMode; }
    public boolean isEditMode() { return editMode; }
    public String getNewPatientName() { return newPatientName; }
    public void setNewPatientName(String newPatientName) { this.newPatientName = newPatientName; }
    public String getNewPatientPhoneNumber() { return newPatientPhoneNumber; }
    public void setNewPatientPhoneNumber(String newPatientPhoneNumber) { this.newPatientPhoneNumber = newPatientPhoneNumber; }
    public String getNewPatientGender() { return newPatientGender; }
    public void setNewPatientGender(String newPatientGender) { this.newPatientGender = newPatientGender; }
    public Integer getPatientAgeYears() { return patientAgeYears; }
    public void setPatientAgeYears(Integer patientAgeYears) { this.patientAgeYears = patientAgeYears; }
    public String getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(String temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }
    public String getWeightKg() { return weightKg; }
    public void setWeightKg(String weightKg) { this.weightKg = weightKg; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public String getAdmissionNotes() { return admissionNotes; }
    public void setAdmissionNotes(String admissionNotes) { this.admissionNotes = admissionNotes; }
    public String getSelectedStatusFilter() { return selectedStatusFilter; }
    public void setSelectedStatusFilter(String selectedStatusFilter) { this.selectedStatusFilter = selectedStatusFilter; }

    private void loadScopedReferenceData() {
        doctorList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(doctorService.getDoctorsByOrganizationId(selectedOrganizationId));
        patientList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
    }

    private void resetForm() {
        selectedConsultation = null;
        editMode = false;
        existingPatientMode = true;
        selectedDoctorId = doctorList.isEmpty() ? null : doctorList.get(0).getId();
        selectedExistingPatientId = null;
        selectedExistingPatient = null;
        newPatientName = null;
        newPatientPhoneNumber = null;
        newPatientGender = null;
        patientAgeYears = null;
        temperatureCelsius = null;
        weightKg = null;
        bloodPressure = null;
        admissionNotes = null;
    }

    private boolean validateForm() {
        if (!isReceptionTokenWorkflowEnabled()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Reception queue disabled",
                    "Enable receptionist token workflow in Clinic Settings to use admission registration.");
            return false;
        }
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return false;
        }
        if (selectedDoctorId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Doctor required", "Select a doctor.");
            return false;
        }
        if (patientAgeYears == null || patientAgeYears < 0 || patientAgeYears > 130) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Age required", "Enter a valid patient age.");
            return false;
        }
        if (existingPatientMode && selectedExistingPatientId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient required", "Select an existing patient.");
            return false;
        }
        if (!existingPatientMode) {
            if (isBlank(newPatientName)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Patient name required", "Enter the patient name.");
                return false;
            }
            if (isBlank(newPatientPhoneNumber)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Phone number required", "Enter the patient phone number.");
                return false;
            }
        } else {
            if (isBlank(newPatientName)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Patient name required", "Loaded patient details are incomplete. Enter the patient name.");
                return false;
            }
        }
        return true;
    }

    private Patient resolvePatientForAdmission(Organizations organization) {
        if (existingPatientMode) {
            Patient patient = patientService.getPatientById(selectedExistingPatientId);
            if (patient == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Patient missing", "Selected patient record could not be found.");
                return null;
            }
            patient.setPatientName(newPatientName == null ? patient.getPatientName() : newPatientName.trim());
            patient.setPhoneNumber(newPatientPhoneNumber == null ? patient.getPhoneNumber() : newPatientPhoneNumber.trim());
            patient.setGender(safeText(newPatientGender));
            patient.setDateOfBirth(resolveDateOfBirthFromAge(patientAgeYears));
            UserActivityTO userActivityTO = populateUserActivityTO();
            userActivityTO.setActivityType("Update");
            GeneralConstants result = patientService.updatePatient(userActivityTO, patient);
            if (result != GeneralConstants.SUCCESSFUL) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Patient update failed", "Unable to update the selected patient details.");
                return null;
            }
            patientList = new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
            selectedExistingPatient = patientService.getPatientById(patient.getId());
            return selectedExistingPatient == null ? patient : selectedExistingPatient;
        }
        return createNewPatient(organization);
    }

    private Patient createNewPatient(Organizations organization) {
        Patient patient = new Patient();
        patient.setOrganization(organization);
        patient.setPatientCode(generatePatientCode());
        patient.setPatientName(newPatientName.trim());
        patient.setPhoneNumber(newPatientPhoneNumber.trim());
        patient.setGender(safeText(newPatientGender));
        patient.setDateOfBirth(resolveDateOfBirthFromAge(patientAgeYears));
        patient.setActive(true);
        patient.setNotes("Registered through consultation admission.");

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Add");
        GeneralConstants result = patientService.addPatient(userActivityTO, patient);
        if (result != GeneralConstants.SUCCESSFUL) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient save failed", "Unable to create the patient record for admission.");
            return null;
        }

        patientList = new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
        for (Patient patientRow : patientList) {
            if (patientRow != null && patient.getPatientCode().equalsIgnoreCase(patientRow.getPatientCode())) {
                return patientRow;
            }
        }
        return patient;
    }

    private Consultation buildQueuedConsultation(Organizations organization, Doctor doctor, Patient patient) {
        Consultation consultation = new Consultation();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp dayStart = Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        Timestamp dayEnd = Timestamp.valueOf(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN));
        Integer tokenNumber = consultationService.getNextTokenNumber(organization.getId(), dayStart, dayEnd);

        ClinicSettings clinicSettings = clinicSettingsService.getClinicSettingsByOrganizationId(organization.getId());
        BigDecimal consultationFee = doctor.getConsultationFee();
        if (consultationFee == null || consultationFee.compareTo(BigDecimal.ZERO) <= 0) {
            consultationFee = clinicSettings == null ? BigDecimal.ZERO : zeroSafe(clinicSettings.getConsultationFee());
        }

        consultation.setOrganization(organization);
        consultation.setDoctor(doctor);
        consultation.setPatient(patient);
        consultation.setConsultationDate(now);
        consultation.setConsultationNumber(generateConsultationNumber(tokenNumber));
        consultation.setTokenNumber(tokenNumber);
        consultation.setPatientAgeYears(patientAgeYears);
        consultation.setTemperatureCelsius(safeText(temperatureCelsius));
        consultation.setWeightKg(safeText(weightKg));
        consultation.setBloodPressure(safeText(bloodPressure));
        consultation.setSymptoms(safeText(admissionNotes));
        consultation.setVitals(buildVitalsSummary());
        consultation.setConsultationFee(zeroSafe(consultationFee));
        consultation.setMedicineTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        consultation.setMedicalCertificateFee(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        consultation.setInvoiceTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        consultation.setIssueInvoice(false);
        consultation.setIssueMedicalCertificate(false);
        consultation.setStatus("Waiting");
        return consultation;
    }

    private boolean sendDoctorAndAdminNotifications(Doctor doctor, Consultation consultation, Patient patient) {
        String message = "New consultation token " + consultation.getTokenNumber()
                + " registered for " + patient.getPatientName()
                + (isBlank(consultation.getBloodPressure()) ? "" : " | BP " + consultation.getBloodPressure())
                + ".";
        invokeNotificationBridge("sendGrowlMessageToApplicationAdmins", new Class[]{String.class}, new Object[]{message});

        Integer userAccountId = doctor.getUserDetail() == null ? null : doctor.getUserDetail().getUserId();
        Integer organizationId = doctor.getOrganization() == null ? null : doctor.getOrganization().getId();
        if (userAccountId != null) {
            return invokeNotificationBridge("sendGrowlMessageToUserAccount", new Class[]{Integer.class, String.class}, new Object[]{userAccountId, message});
        }
        if (organizationId != null) {
            return invokeNotificationBridge("sendGrowlMessageToOrganization", new Class[]{Integer.class, String.class}, new Object[]{organizationId, message});
        }
        return false;
    }

    private Doctor findDoctor(Integer doctorId) {
        if (doctorId == null) {
            return null;
        }
        for (Doctor doctor : doctorList) {
            if (doctor != null && doctorId.equals(doctor.getId())) {
                return doctor;
            }
        }
        return doctorService.getDoctorById(doctorId);
    }

    private Date resolveDateOfBirthFromAge(Integer age) {
        if (age == null || age < 0) {
            return null;
        }
        LocalDate dateOfBirth = LocalDate.now().minusYears(age);
        return Date.valueOf(dateOfBirth);
    }

    private String buildVitalsSummary() {
        List<String> parts = new ArrayList<>();
        if (!isBlank(temperatureCelsius)) {
            parts.add("Temp " + temperatureCelsius.trim() + " C");
        }
        if (!isBlank(weightKg)) {
            parts.add("Weight " + weightKg.trim() + " kg");
        }
        if (!isBlank(bloodPressure)) {
            parts.add("BP " + bloodPressure.trim());
        }
        return String.join(" | ", parts);
    }

    private String generatePatientCode() {
        return "PAT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private String generateConsultationNumber(Integer tokenNumber) {
        return "CONS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + tokenNumber;
    }

    private BigDecimal zeroSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
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
        userActivityTO.setCreatedAt(java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        return userActivityTO;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private void populatePatientFormFromEntity(Patient patient) {
        if (patient == null) {
            newPatientName = null;
            newPatientPhoneNumber = null;
            newPatientGender = null;
            return;
        }
        newPatientName = patient.getPatientName();
        newPatientPhoneNumber = patient.getPhoneNumber();
        newPatientGender = patient.getGender();
        if (patient.getDateOfBirth() != null) {
            patientAgeYears = Period.between(patient.getDateOfBirth().toLocalDate(), LocalDate.now()).getYears();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(String value) {
        return isBlank(value) ? "" : value.trim();
    }

    private boolean invokeNotificationBridge(String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Class<?> notificationServiceClass = Class.forName("com.web.coretix.general.NotificationService");
            notificationServiceClass.getMethod(methodName, parameterTypes).invoke(null, parameters);
            return true;
        } catch (Exception ignored) {
            // The notification helper lives in the core web layer. If it is not reachable here,
            // admission registration should still succeed without blocking the queue flow.
            return false;
        }
    }
}
