package com.web.carex.clinicmanagement;

import com.module.carex.clinicmanagement.IConsultationService;
import com.module.carex.clinicmanagement.IDoctorService;
import com.module.carex.clinicmanagement.IMedicineService;
import com.module.carex.clinicmanagement.IPatientService;
import com.module.carex.settings.IClinicSettingsService;
import com.module.carex.settings.IInvoiceSettingsService;
import com.module.carex.settings.IMedicalCertificateSettingsService;
import com.module.carex.settings.IPrescriptionSettingsService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Consultation;
import com.persist.carex.clinicmanagement.ConsultationMedicine;
import com.persist.carex.clinicmanagement.Doctor;
import com.persist.carex.clinicmanagement.Medicine;
import com.persist.carex.clinicmanagement.Patient;
import com.persist.carex.settings.ClinicSettings;
import com.persist.carex.settings.InvoiceSettings;
import com.persist.carex.settings.MedicalCertificateSettings;
import com.persist.carex.settings.PrescriptionSettings;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Named("consultationBean")
@Scope("session")
public class ConsultationBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    private IConsultationService consultationService;

    @Inject
    private IDoctorService doctorService;

    @Inject
    private IPatientService patientService;

    @Inject
    private IMedicineService medicineService;

    @Inject
    private IOrganizationService organizationService;

    @Inject
    private IClinicSettingsService clinicSettingsService;

    @Inject
    private IPrescriptionSettingsService prescriptionSettingsService;

    @Inject
    private IInvoiceSettingsService invoiceSettingsService;

    @Inject
    private IMedicalCertificateSettingsService medicalCertificateSettingsService;

    private List<Organizations> organizationList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private List<Patient> patientList = new ArrayList<>();
    private List<Medicine> medicineList = new ArrayList<>();
    private List<Consultation> consultationList = new ArrayList<>();

    private Consultation consultation = new Consultation();
    private Consultation selectedConsultation;
    private Integer selectedOrganizationId;
    private Integer selectedDoctorId;
    private Integer selectedPatientId;
    private Date consultationDateTime;
    private Date invoiceIssueDate;
    private Date invoiceDueDate;
    private List<ConsultationMedicineFormLine> medicineLines = new ArrayList<>();
    private ConsultationMedicineFormLine draftMedicineLine = new ConsultationMedicineFormLine();
    private ConsultationMedicineFormLine selectedMedicineLine;
    private boolean addOperation = true;
    private boolean initialized;
    private boolean searchMode = true;
    private boolean readOnlyMode;
    private int activePreviewTabIndex;
    private Organizations selectedOrganization;
    private Doctor selectedDoctor;
    private Patient selectedPatient;

    private ClinicSettings clinicSettings = new ClinicSettings();
    private PrescriptionSettings prescriptionSettings = new PrescriptionSettings();
    private InvoiceSettings invoiceSettings = new InvoiceSettings();
    private MedicalCertificateSettings medicalCertificateSettings = new MedicalCertificateSettings();

    private String prescriptionPreviewBodyHtml;
    private String prescriptionPreviewFooterHtml;
    private String invoicePreviewBodyHtml;
    private String invoicePreviewFooterHtml;
    private String medicalCertificatePreviewBodyHtml;
    private String medicalCertificatePreviewFooterHtml;
    private StreamedContent consultationPrescriptionPdf;
    private StreamedContent consultationInvoicePdf;
    private StreamedContent consultationMedicalCertificatePdf;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.isPostback() && initialized) {
            return;
        }
        organizationList = new ArrayList<>(organizationService.getOrganizationsList());
        if (selectedOrganizationId == null) {
            selectedOrganizationId = resolveCurrentOrganizationId();
        }
        loadScopedData();
        addButtonAction();
        searchMode = true;
        initialized = true;
    }

    public void onOrganizationChange() {
        loadScopedReferenceData();
        consultationList = new ArrayList<>();
        addButtonAction();
        searchMode = true;
        PrimeFaces.current().ajax().update("form:contentModePanel", "form:messages");
    }

    public void onDoctorChange() {
        selectedDoctor = findDoctorInList(selectedDoctorId);
        Doctor doctor = selectedDoctor;
        if (doctor != null && doctor.getConsultationFee() != null && doctor.getConsultationFee().compareTo(BigDecimal.ZERO) > 0) {
            consultation.setConsultationFee(scaleAmount(doctor.getConsultationFee()));
        } else {
            consultation.setConsultationFee(scaleAmount(clinicSettings.getConsultationFee()));
        }
        recalculateTotals();
    }

    public void onPatientChange() {
        selectedPatient = findPatientInList(selectedPatientId);
    }

    public void onDraftMedicineChange() {
        Medicine medicine = resolveMedicine(draftMedicineLine.getMedicineId());
        if (medicine == null) {
            draftMedicineLine = new ConsultationMedicineFormLine();
            return;
        }
        draftMedicineLine.setMedicineName(medicine.getMedicineName());
        draftMedicineLine.setDescriptionText(buildMedicineDescription(medicine));
        draftMedicineLine.setUnitPrice(scaleAmount(medicine.getSellingPrice()));
        if (draftMedicineLine.getQuantity() == null || draftMedicineLine.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            draftMedicineLine.setQuantity(BigDecimal.ONE);
        }
        draftMedicineLine.recalculateLineTotal();
        PrimeFaces.current().ajax().update("form:medicineLinePanel", "form:previewTabsPanel");
    }

    public void onInvoiceToggle() {
        if (!consultation.isIssueInvoice()) {
            consultation.setIssueMedicalCertificate(false);
        }
        recalculateTotals();
    }

    public void onMedicalCertificateToggle() {
        if (consultation.isIssueMedicalCertificate()) {
            consultation.setMedicalCertificateFee(scaleAmount(clinicSettings.getMedicalCertificateFee()));
            if (!consultation.isIssueInvoice()) {
                consultation.setIssueInvoice(true);
            }
        } else {
            consultation.setMedicalCertificateFee(BigDecimal.ZERO);
        }
        recalculateTotals();
    }

    public void addButtonAction() {
        addOperation = true;
        readOnlyMode = false;
        activePreviewTabIndex = 0;
        selectedConsultation = null;
        consultation = new Consultation();
        consultation.setStatus("Completed");
        consultation.setConsultationFee(scaleAmount(clinicSettings.getConsultationFee()));
        consultation.setMedicalCertificateFee(BigDecimal.ZERO);
        consultation.setIssueInvoice(clinicSettings.isRequireInvoice());
        consultation.setIssueMedicalCertificate(clinicSettings.isRequireMedicalCertificate());
        if (consultation.isIssueMedicalCertificate()) {
            consultation.setMedicalCertificateFee(scaleAmount(clinicSettings.getMedicalCertificateFee()));
        }
        consultation.setInvoicePaidBy("Cash");
        consultation.setConsultationNumber(generateConsultationNumber());
        consultationDateTime = new Date();
        invoiceIssueDate = consultationDateTime;
        invoiceDueDate = consultationDateTime;
        medicineLines = new ArrayList<>();
        draftMedicineLine = new ConsultationMedicineFormLine();
        selectedDoctorId = null;
        selectedPatientId = null;
        selectedDoctor = null;
        selectedPatient = null;
        recalculateTotals();
    }

    public void startNewConsultation() {
        addButtonAction();
        searchMode = false;
    }

    public void backToSearch() {
        searchMode = true;
        readOnlyMode = false;
    }

    public void addMedicineLine() {
        if (!validateDraftMedicineLine()) {
            PrimeFaces.current().ajax().update("form:messages");
            return;
        }
        ConsultationMedicineFormLine line = new ConsultationMedicineFormLine();
        line.setMedicineId(draftMedicineLine.getMedicineId());
        line.setMedicineName(draftMedicineLine.getMedicineName());
        line.setDescriptionText(safeText(draftMedicineLine.getDescriptionText(), ""));
        line.setDose(safeText(draftMedicineLine.getDose(), ""));
        line.setFrequency(safeText(draftMedicineLine.getFrequency(), ""));
        line.setDurationText(safeText(draftMedicineLine.getDurationText(), ""));
        line.setRemarks(safeText(draftMedicineLine.getRemarks(), ""));
        line.setQuantity(scaleAmount(draftMedicineLine.getQuantity()));
        line.setUnitPrice(scaleAmount(draftMedicineLine.getUnitPrice()));
        line.recalculateLineTotal();
        medicineLines.add(line);
        draftMedicineLine = new ConsultationMedicineFormLine();
        recalculateTotals();
        PrimeFaces.current().ajax().update("form:medicineLinePanel", "form:previewTabsPanel");
    }

    public void removeMedicineLine() {
        if (selectedMedicineLine == null) {
            return;
        }
        medicineLines.remove(selectedMedicineLine);
        selectedMedicineLine = null;
        recalculateTotals();
        PrimeFaces.current().ajax().update("form:medicineLinePanel", "form:previewTabsPanel");
    }

    public void confirmEditButtonAction() {
        if (selectedConsultation == null) {
            return;
        }
        readOnlyMode = false;
        searchMode = false;
        Consultation consultationFromDb = consultationService.getConsultationById(selectedConsultation.getId());
        if (consultationFromDb == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Consultation does not exist anymore.");
            return;
        }
        addOperation = false;
        consultation = consultationFromDb;
        selectedDoctorId = consultation.getDoctor() == null ? null : consultation.getDoctor().getId();
        selectedPatientId = consultation.getPatient() == null ? null : consultation.getPatient().getId();
        selectedDoctor = consultation.getDoctor();
        selectedPatient = consultation.getPatient();
        consultationDateTime = consultation.getConsultationDate() == null ? new Date() : new Date(consultation.getConsultationDate().getTime());
        invoiceIssueDate = consultation.getInvoiceIssueDate() == null ? consultationDateTime : new Date(consultation.getInvoiceIssueDate().getTime());
        invoiceDueDate = consultation.getInvoiceDueDate() == null ? consultationDateTime : new Date(consultation.getInvoiceDueDate().getTime());
        medicineLines = new ArrayList<>();
        if (consultation.getConsultationMedicines() != null) {
            for (ConsultationMedicine line : consultation.getConsultationMedicines()) {
                ConsultationMedicineFormLine formLine = new ConsultationMedicineFormLine();
                formLine.setMedicineId(line.getMedicine() == null ? null : line.getMedicine().getId());
                formLine.setMedicineName(line.getMedicine() == null ? line.getDescriptionText() : line.getMedicine().getMedicineName());
                formLine.setDescriptionText(line.getDescriptionText());
                formLine.setDose(line.getDose());
                formLine.setFrequency(line.getFrequency());
                formLine.setDurationText(line.getDurationText());
                formLine.setRemarks(line.getRemarks());
                formLine.setQuantity(scaleAmount(line.getQuantity()));
                formLine.setUnitPrice(scaleAmount(line.getUnitPrice()));
                formLine.recalculateLineTotal();
                medicineLines.add(formLine);
            }
        }
        draftMedicineLine = new ConsultationMedicineFormLine();
        recalculateTotals();
    }

    public void viewSelectedConsultation() {
        if (selectedConsultation == null) {
            return;
        }
        confirmEditButtonAction();
        readOnlyMode = true;
    }

    public void openConsultationView(Integer consultationId) {
        if (!loadSelectedConsultation(consultationId)) {
            return;
        }
        viewSelectedConsultation();
    }

    public void openConsultationEditor(Integer consultationId) {
        if (!loadSelectedConsultation(consultationId)) {
            return;
        }
        confirmEditButtonAction();
    }

    public void openPrescriptionPreview(Integer consultationId) {
        activePreviewTabIndex = 0;
        openConsultationView(consultationId);
    }

    public void openInvoicePreview(Integer consultationId) {
        activePreviewTabIndex = 1;
        openConsultationView(consultationId);
    }

    public void openMedicalCertificatePreview(Integer consultationId) {
        activePreviewTabIndex = 2;
        openConsultationView(consultationId);
    }

    public void selectConsultationForDelete(Integer consultationId) {
        loadSelectedConsultation(consultationId);
    }

    public void viewPrescriptionFromTable() {
        activePreviewTabIndex = 0;
        viewSelectedConsultation();
    }

    public void viewInvoiceFromTable() {
        activePreviewTabIndex = 1;
        viewSelectedConsultation();
    }

    public void viewMedicalCertificateFromTable() {
        activePreviewTabIndex = 2;
        viewSelectedConsultation();
    }

    public void saveConsultation() {
        if (!validateConsultation()) {
            PrimeFaces.current().ajax().update("form:messages", "form:consultationEditorPanel");
            return;
        }
        Organizations organization = selectedOrganization != null ? selectedOrganization : organizationService.getOrganizationById(selectedOrganizationId);
        Doctor doctor = selectedDoctor != null ? selectedDoctor : doctorService.getDoctorById(selectedDoctorId);
        Patient patient = selectedPatient != null ? selectedPatient : patientService.getPatientById(selectedPatientId);
        consultation.setOrganization(organization);
        consultation.setDoctor(doctor);
        consultation.setPatient(patient);
        consultation.setConsultationDate(new Timestamp(consultationDateTime.getTime()));
        consultation.setInvoiceIssueDate(consultation.isIssueInvoice() && invoiceIssueDate != null ? new Timestamp(invoiceIssueDate.getTime()) : null);
        consultation.setInvoiceDueDate(consultation.isIssueInvoice() && invoiceDueDate != null ? new Timestamp(invoiceDueDate.getTime()) : null);
        consultation.getConsultationMedicines().clear();
        int lineNumber = 1;
        for (ConsultationMedicineFormLine formLine : medicineLines) {
            Medicine medicine = resolveMedicine(formLine.getMedicineId());
            if (medicine == null) {
                continue;
            }
            ConsultationMedicine line = new ConsultationMedicine();
            line.setMedicine(medicine);
            line.setLineNumber(lineNumber++);
            line.setDescriptionText(safeText(formLine.getDescriptionText(), buildMedicineDescription(medicine)));
            line.setDose(safeText(formLine.getDose(), ""));
            line.setFrequency(safeText(formLine.getFrequency(), ""));
            line.setDurationText(safeText(formLine.getDurationText(), ""));
            line.setRemarks(safeText(formLine.getRemarks(), ""));
            line.setQuantity(scaleAmount(formLine.getQuantity()));
            line.setUnitPrice(scaleAmount(formLine.getUnitPrice()));
            line.setLineTotal(scaleAmount(formLine.getLineTotal()));
            consultation.addConsultationMedicine(line);
        }
        recalculateTotals();
        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;
        if (addOperation) {
            userActivityTO.setActivityType("Add");
            result = consultationService.addConsultation(userActivityTO, consultation);
        } else {
            userActivityTO.setActivityType("Update");
            result = consultationService.updateConsultation(userActivityTO, consultation);
        }
        handleSaveResult(result);
    }

    public void confirmDeleteConsultation() {
        if (selectedConsultation == null) {
            return;
        }
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = consultationService.deleteConsultation(userActivityTO, selectedConsultation);
        if (result == GeneralConstants.SUCCESSFUL) {
            addMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Consultation deleted successfully.");
            fetchConsultationList();
            addButtonAction();
        } else if (result == GeneralConstants.ENTRY_NOT_EXISTS) {
            addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Consultation does not exist anymore.");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", "Unable to delete consultation.");
        }
        PrimeFaces.current().ajax().update("form:consultationMainPanel", "form:consultationEditorPanel", "form:previewTabsPanel", "form:messages");
    }

    public void fetchConsultationList() {
        consultationList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(consultationService.getConsultationsByOrganizationId(selectedOrganizationId));
    }

    public void preparePrescriptionDownload() {
        consultationPrescriptionPdf = prepareDocumentDownload("consultation-prescription.pdf", "Prescription", prescriptionPreviewBodyHtml, prescriptionPreviewFooterHtml, "prescription-preview");
    }

    public void prepareInvoiceDownload() {
        consultationInvoicePdf = prepareDocumentDownload("consultation-invoice.pdf", "Invoice", invoicePreviewBodyHtml, invoicePreviewFooterHtml, "invoice-preview");
    }

    public void prepareMedicalCertificateDownload() {
        if (!consultation.isIssueMedicalCertificate()) {
            consultationMedicalCertificatePdf = null;
            addMessage(FacesMessage.SEVERITY_WARN, "Medical certificate disabled", "Enable medical certificate to download it.");
            return;
        }
        consultationMedicalCertificatePdf = prepareDocumentDownload("consultation-medical-certificate.pdf", "Medical Certificate",
                medicalCertificatePreviewBodyHtml, medicalCertificatePreviewFooterHtml, "medical-certificate-preview");
    }

    public List<String> getAvailablePaidByOptions() {
        return Arrays.asList("Cash", "Card", "UPI", "Insurance", "Online Transfer");
    }

    public String getPreviewThemeColor() {
        return normalizeHexColor(prescriptionSettings.getThemeColor(), "#0F766E");
    }

    public String getPreviewTextColor() {
        return normalizeHexColor(prescriptionSettings.getTextColor(), "#111827");
    }

    public String getPreviewClinicName() { return safeText(clinicSettings.getClinicName(), "XXX XXXX XXX"); }
    public String getPreviewClinicTagline() { return safeText(clinicSettings.getClinicTagline(), "XXX XXXX XXX XXX"); }

    public String getPreviewDoctorName() {
        Doctor doctor = getSelectedDoctor();
        if (doctor != null && !isBlank(doctor.getDoctorName())) {
            return doctor.getDoctorName();
        }
        return safeText(clinicSettings.getDoctorName(), "Dr. XXX XXXX XXX");
    }

    public String getPreviewDoctorQualification() {
        Doctor doctor = getSelectedDoctor();
        if (doctor != null && !isBlank(doctor.getQualification())) {
            return doctor.getQualification();
        }
        return safeText(clinicSettings.getDoctorQualification(), "MD Pediatrics");
    }

    public String getPreviewDoctorSpecialization() {
        Doctor doctor = getSelectedDoctor();
        if (doctor != null && !isBlank(doctor.getSpecialization())) {
            return doctor.getSpecialization();
        }
        return safeText(clinicSettings.getDoctorSpecialization(), "Senior Consultant");
    }

    public String getPreviewClinicEmail() { return safeText(clinicSettings.getClinicEmail(), "xxx@xxxx.com"); }
    public String getPreviewRegistrationNumber() { return safeText(clinicSettings.getRegistrationNumber(), "XXX88"); }
    public String getPreviewClinicAddress() { return safeText(clinicSettings.getClinicAddress(), "Clinic address"); }
    public String getPreviewAppointmentContact() { return safeText(clinicSettings.getAppointmentContact(), "XXXX1 3XX38"); }
    public String getPreviewScheduleLineOne() { return safeText(clinicSettings.getScheduleLineOne(), "Days : Mon - Sat | Timings : 9.00 AM - 12.00 PM & 6.00 PM - 9.00 PM"); }
    public String getPreviewScheduleLineTwo() { return safeText(clinicSettings.getScheduleLineTwo(), "Sunday : 10.00 AM - 1.00 PM | Tuesday : 6.00 PM - 9.00 PM"); }
    public String getPreviewCurrencySymbol() { return safeText(clinicSettings.getBaseCurrencySymbol(), ""); }
    public String getPreviewFontFamily() { return safeText(prescriptionSettings.getFontFamily(), "Helvetica"); }

    public String getPreviewPatientName() {
        Patient patient = getSelectedPatient();
        return patient == null ? "XXX XXXX XXXXX" : safeText(patient.getPatientName(), "XXX XXXX XXXXX");
    }

    public String getPreviewPatientGender() {
        Patient patient = getSelectedPatient();
        return patient == null ? "Xxxxxx" : safeText(patient.getGender(), "Xxxxxx");
    }

    public String getPreviewPatientAgeText() {
        Patient patient = getSelectedPatient();
        if (patient == null || patient.getDateOfBirth() == null) {
            return "X year(s)";
        }
        LocalDate dob = patient.getDateOfBirth().toLocalDate();
        Period period = Period.between(dob, LocalDate.now());
        return period.getYears() + " year(s) " + Math.max(period.getMonths(), 0) + " month(s)";
    }

    public String getPreviewPatientCode() {
        Patient patient = getSelectedPatient();
        return patient == null ? "XXX000XX" : safeText(patient.getPatientCode(), "XXX000XX");
    }

    public String getPreviewConsultationDateTime() {
        Date currentDate = consultationDateTime == null ? new Date() : consultationDateTime;
        return DATE_TIME_FORMATTER.format(currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public String getPreviewInvoiceDate() {
        Date currentDate = invoiceIssueDate == null ? new Date() : invoiceIssueDate;
        return DATE_FORMATTER.format(currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public String getPreviewInvoiceDueDate() {
        Date currentDate = invoiceDueDate == null ? new Date() : invoiceDueDate;
        return DATE_FORMATTER.format(currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public String getPreviewPrescriptionWatermarkText() { return safeText(prescriptionSettings.getWatermarkText(), "PRESCRIPTION"); }
    public String getPreviewInvoiceWatermarkText() { return safeText(invoiceSettings.getWatermarkText(), "INVOICE"); }
    public String getPreviewMedicalCertificateWatermarkText() { return safeText(medicalCertificateSettings.getWatermarkText(), "CERTIFICATE"); }

    public boolean isPreviewOrganizationLogoAvailable() {
        return selectedOrganization != null && selectedOrganization.getImage() != null && selectedOrganization.getImage().length > 0;
    }

    public String getPreviewOrganizationLogoDataUri() {
        if (selectedOrganization == null || selectedOrganization.getImage() == null || selectedOrganization.getImage().length == 0) {
            return "";
        }
        return "data:" + resolveImageMimeType(selectedOrganization.getImage()) + ";base64," + Base64.getEncoder().encodeToString(selectedOrganization.getImage());
    }

    public int getPreviewLogoSizePx() {
        String logoSize = safeText(prescriptionSettings.getLogoSize(), "Medium");
        if ("Small".equalsIgnoreCase(logoSize)) {
            return 64;
        }
        if ("Large".equalsIgnoreCase(logoSize)) {
            return 116;
        }
        return 90;
    }

    public BigDecimal getComputedMedicineTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ConsultationMedicineFormLine line : medicineLines) {
            total = total.add(scaleAmount(line.getLineTotal()));
        }
        return scaleAmount(total);
    }

    public BigDecimal getComputedInvoiceTotal() {
        BigDecimal total = scaleAmount(consultation.getConsultationFee()).add(getComputedMedicineTotal());
        if (consultation.isIssueMedicalCertificate()) {
            total = total.add(scaleAmount(consultation.getMedicalCertificateFee()));
        }
        return scaleAmount(total);
    }

    public boolean isInvoiceAvailable() { return consultation.isIssueInvoice(); }
    public boolean isMedicalCertificateAvailable() { return consultation.isIssueMedicalCertificate(); }
    public boolean isSearchMode() { return searchMode; }
    public boolean isReadOnlyMode() { return readOnlyMode; }
    public int getActivePreviewTabIndex() { return activePreviewTabIndex; }
    public void setActivePreviewTabIndex(int activePreviewTabIndex) { this.activePreviewTabIndex = activePreviewTabIndex; }

    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }
    public List<Organizations> getOrganizationList() { return organizationList; }
    public List<Doctor> getDoctorList() { return doctorList; }
    public List<Patient> getPatientList() { return patientList; }
    public List<Medicine> getMedicineList() { return medicineList; }
    public List<Consultation> getConsultationList() { return consultationList; }
    public Consultation getSelectedConsultation() { return selectedConsultation; }
    public void setSelectedConsultation(Consultation selectedConsultation) { this.selectedConsultation = selectedConsultation; }
    public Integer getSelectedOrganizationId() { return selectedOrganizationId; }
    public void setSelectedOrganizationId(Integer selectedOrganizationId) { this.selectedOrganizationId = selectedOrganizationId; }
    public Integer getSelectedDoctorId() { return selectedDoctorId; }
    public void setSelectedDoctorId(Integer selectedDoctorId) { this.selectedDoctorId = selectedDoctorId; }
    public Integer getSelectedPatientId() { return selectedPatientId; }
    public void setSelectedPatientId(Integer selectedPatientId) { this.selectedPatientId = selectedPatientId; }
    public Date getConsultationDateTime() { return consultationDateTime; }
    public void setConsultationDateTime(Date consultationDateTime) { this.consultationDateTime = consultationDateTime; }
    public Date getInvoiceIssueDate() { return invoiceIssueDate; }
    public void setInvoiceIssueDate(Date invoiceIssueDate) { this.invoiceIssueDate = invoiceIssueDate; }
    public Date getInvoiceDueDate() { return invoiceDueDate; }
    public void setInvoiceDueDate(Date invoiceDueDate) { this.invoiceDueDate = invoiceDueDate; }
    public List<ConsultationMedicineFormLine> getMedicineLines() { return medicineLines; }
    public ConsultationMedicineFormLine getDraftMedicineLine() { return draftMedicineLine; }
    public void setDraftMedicineLine(ConsultationMedicineFormLine draftMedicineLine) { this.draftMedicineLine = draftMedicineLine; }
    public ConsultationMedicineFormLine getSelectedMedicineLine() { return selectedMedicineLine; }
    public void setSelectedMedicineLine(ConsultationMedicineFormLine selectedMedicineLine) { this.selectedMedicineLine = selectedMedicineLine; }
    public boolean isAddOperation() { return addOperation; }
    public PrescriptionSettings getPrescriptionSettings() { return prescriptionSettings; }
    public InvoiceSettings getInvoiceSettings() { return invoiceSettings; }
    public MedicalCertificateSettings getMedicalCertificateSettings() { return medicalCertificateSettings; }
    public ClinicSettings getClinicSettings() { return clinicSettings; }
    public String getPrescriptionPreviewBodyHtml() { return prescriptionPreviewBodyHtml; }
    public void setPrescriptionPreviewBodyHtml(String prescriptionPreviewBodyHtml) { this.prescriptionPreviewBodyHtml = prescriptionPreviewBodyHtml; }
    public String getPrescriptionPreviewFooterHtml() { return prescriptionPreviewFooterHtml; }
    public void setPrescriptionPreviewFooterHtml(String prescriptionPreviewFooterHtml) { this.prescriptionPreviewFooterHtml = prescriptionPreviewFooterHtml; }
    public String getInvoicePreviewBodyHtml() { return invoicePreviewBodyHtml; }
    public void setInvoicePreviewBodyHtml(String invoicePreviewBodyHtml) { this.invoicePreviewBodyHtml = invoicePreviewBodyHtml; }
    public String getInvoicePreviewFooterHtml() { return invoicePreviewFooterHtml; }
    public void setInvoicePreviewFooterHtml(String invoicePreviewFooterHtml) { this.invoicePreviewFooterHtml = invoicePreviewFooterHtml; }
    public String getMedicalCertificatePreviewBodyHtml() { return medicalCertificatePreviewBodyHtml; }
    public void setMedicalCertificatePreviewBodyHtml(String medicalCertificatePreviewBodyHtml) { this.medicalCertificatePreviewBodyHtml = medicalCertificatePreviewBodyHtml; }
    public String getMedicalCertificatePreviewFooterHtml() { return medicalCertificatePreviewFooterHtml; }
    public void setMedicalCertificatePreviewFooterHtml(String medicalCertificatePreviewFooterHtml) { this.medicalCertificatePreviewFooterHtml = medicalCertificatePreviewFooterHtml; }
    public StreamedContent getConsultationPrescriptionPdf() { return consultationPrescriptionPdf; }
    public StreamedContent getConsultationInvoicePdf() { return consultationInvoicePdf; }
    public StreamedContent getConsultationMedicalCertificatePdf() { return consultationMedicalCertificatePdf; }

    private void loadScopedData() {
        loadScopedReferenceData();
        fetchConsultationList();
    }

    private boolean loadSelectedConsultation(Integer consultationId) {
        if (consultationId == null) {
            selectedConsultation = null;
            return false;
        }
        Consultation consultationRow = consultationService.getConsultationById(consultationId);
        if (consultationRow == null) {
            selectedConsultation = null;
            addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Consultation does not exist anymore.");
            return false;
        }
        selectedConsultation = consultationRow;
        return true;
    }

    private void loadScopedReferenceData() {
        selectedOrganization = selectedOrganizationId == null ? null : organizationService.getOrganizationById(selectedOrganizationId);
        doctorList = selectedOrganizationId == null ? new ArrayList<>() : new ArrayList<>(doctorService.getDoctorsByOrganizationId(selectedOrganizationId));
        patientList = selectedOrganizationId == null ? new ArrayList<>() : new ArrayList<>(patientService.getPatientsByOrganizationId(selectedOrganizationId));
        medicineList = selectedOrganizationId == null ? new ArrayList<>() : new ArrayList<>(medicineService.getMedicinesByOrganizationId(selectedOrganizationId));
        selectedDoctor = findDoctorInList(selectedDoctorId);
        selectedPatient = findPatientInList(selectedPatientId);
        loadSettings();
    }

    private void loadSettings() {
        clinicSettings = selectedOrganizationId == null ? createDefaultClinicSettings() : fallbackClinicSettings(clinicSettingsService.getClinicSettingsByOrganizationId(selectedOrganizationId));
        prescriptionSettings = selectedOrganizationId == null ? createDefaultPrescriptionSettings() : fallbackPrescriptionSettings(prescriptionSettingsService.getPrescriptionSettingsByOrganizationId(selectedOrganizationId));
        invoiceSettings = selectedOrganizationId == null ? createDefaultInvoiceSettings() : fallbackInvoiceSettings(invoiceSettingsService.getInvoiceSettingsByOrganizationId(selectedOrganizationId));
        medicalCertificateSettings = selectedOrganizationId == null ? createDefaultMedicalCertificateSettings() : fallbackMedicalCertificateSettings(medicalCertificateSettingsService.getMedicalCertificateSettingsByOrganizationId(selectedOrganizationId));
    }

    private boolean validateConsultation() {
        if (selectedOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return false;
        }
        if (selectedDoctorId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Doctor required", "Select a doctor.");
            return false;
        }
        if (selectedPatientId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Patient required", "Select a patient.");
            return false;
        }
        if (consultationDateTime == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Consultation date required", "Select consultation date and time.");
            return false;
        }
        if (isBlank(consultation.getConsultationNumber())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Consultation number required", "Enter consultation number.");
            return false;
        }
        if (isBlank(consultation.getSymptoms()) && medicineLines.isEmpty() && isBlank(consultation.getDiagnosis())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Consultation details required", "Enter symptoms, diagnosis, or at least one medicine line.");
            return false;
        }
        if (consultation.isIssueInvoice() && isBlank(consultation.getInvoicePaidBy())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Payment mode required", "Enter how the invoice was paid.");
            return false;
        }
        if (consultation.isIssueMedicalCertificate() && isBlank(consultation.getMedicalCertificateDisease())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Medical certificate disease required", "Enter the disease for the medical certificate.");
            return false;
        }
        return true;
    }

    private boolean validateDraftMedicineLine() {
        if (draftMedicineLine.getMedicineId() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Medicine required", "Select a medicine to add.");
            return false;
        }
        if (draftMedicineLine.getQuantity() == null || draftMedicineLine.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Quantity required", "Enter a quantity greater than zero.");
            return false;
        }
        if (draftMedicineLine.getUnitPrice() == null || draftMedicineLine.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid price", "Enter a valid unit price.");
            return false;
        }
        return true;
    }

    private void recalculateTotals() {
        consultation.setMedicineTotal(getComputedMedicineTotal());
        if (!consultation.isIssueMedicalCertificate()) {
            consultation.setMedicalCertificateFee(BigDecimal.ZERO);
        } else {
            consultation.setMedicalCertificateFee(scaleAmount(
                    consultation.getMedicalCertificateFee() == null || consultation.getMedicalCertificateFee().compareTo(BigDecimal.ZERO) == 0
                            ? clinicSettings.getMedicalCertificateFee()
                            : consultation.getMedicalCertificateFee()));
        }
        consultation.setConsultationFee(scaleAmount(consultation.getConsultationFee()));
        consultation.setInvoiceTotal(consultation.isIssueInvoice() ? getComputedInvoiceTotal() : BigDecimal.ZERO);
    }

    private void handleSaveResult(GeneralConstants result) {
        if (result == GeneralConstants.SUCCESSFUL) {
            addMessage(FacesMessage.SEVERITY_INFO, "Saved", addOperation ? "Consultation saved successfully." : "Consultation updated successfully.");
            fetchConsultationList();
            addButtonAction();
        } else if (result == GeneralConstants.ENTRY_ALREADY_EXISTS) {
            addMessage(FacesMessage.SEVERITY_WARN, "Duplicate", "Consultation number already exists.");
        } else if (result == GeneralConstants.ENTRY_NOT_EXISTS) {
            addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Consultation does not exist anymore.");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Save failed", "Unable to save consultation.");
        }
        PrimeFaces.current().ajax().update("form:consultationMainPanel", "form:consultationEditorPanel", "form:previewTabsPanel", "form:messages");
    }

    private StreamedContent prepareDocumentDownload(String fileName, String title, String bodyHtml, String footerHtml, String previewClass) {
        try {
            if (isBlank(bodyHtml)) {
                throw new IllegalStateException("Preview content was not captured for download.");
            }
            byte[] pdfBytes = buildBrowserRenderedPdf(title, bodyHtml, footerHtml, previewClass);
            return DefaultStreamedContent.builder()
                    .name(fileName)
                    .contentType("application/pdf")
                    .stream(() -> new ByteArrayInputStream(pdfBytes))
                    .build();
        } catch (Exception exception) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Download failed", safeText(exception.getMessage(), "Unable to generate PDF."));
            return null;
        }
    }

    private byte[] buildBrowserRenderedPdf(String title, String bodyHtml, String footerHtml, String previewClass) throws Exception {
        Path tempDirectory = Files.createTempDirectory("carex-consultation-preview-");
        try {
            Path htmlPath = tempDirectory.resolve("preview.html");
            Path pdfPath = tempDirectory.resolve("preview.pdf");
            Files.writeString(htmlPath, buildBrowserRenderHtml(title, bodyHtml, footerHtml, previewClass), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    resolveBrowserExecutable(),
                    "--headless=new",
                    "--disable-gpu",
                    "--run-all-compositor-stages-before-draw",
                    "--print-to-pdf-no-header",
                    "--no-pdf-header-footer",
                    "--print-to-pdf=" + pdfPath.toAbsolutePath(),
                    htmlPath.toUri().toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Browser PDF generation timed out.");
            }
            if (process.exitValue() != 0 || !Files.exists(pdfPath)) {
                throw new IllegalStateException("Browser PDF generation failed. " + output);
            }
            return Files.readAllBytes(pdfPath);
        } finally {
            deleteRecursively(tempDirectory);
        }
    }

    private String buildBrowserRenderHtml(String title, String bodyHtml, String footerHtml, String previewClass) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><title>" + escapeHtml(title) + "</title><style>"
                + "html,body{font-family:" + safeCssFontFamily() + ";margin:0;padding:0;background:#fff;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;overflow:hidden !important;}"
                + "body{background:#fff;overflow:hidden !important;}.preview-shell{position:relative;box-sizing:border-box;width:186mm;height:272mm;max-height:272mm;margin:0 auto;padding:0;overflow:hidden !important;border:none !important;outline:none !important;box-shadow:none !important;}"
                + ".preview-shell *{-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;color-adjust:exact !important;box-sizing:border-box;}"
                + ".print-body{padding-bottom:42mm;overflow:hidden !important;max-height:230mm;}"
                + ".print-body ." + previewClass + ",.print-body .card{border:none !important;outline:none !important;box-shadow:none !important;background:#fff !important;}"
                + ".print-footer{position:absolute;left:24px;right:24px;bottom:18px;}"
                + "@page{size:A4 portrait;margin:12mm;}@media print{html,body{background:#fff !important;overflow:hidden !important;}body{padding:0;margin:0;overflow:hidden !important;}}"
                + "</style></head><body><div class=\"preview-shell\"><div class=\"print-body\">"
                + safeHtmlFragment(bodyHtml)
                + "</div>" + (isBlank(footerHtml) ? "" : "<div class=\"print-footer\">" + safeHtmlFragment(footerHtml) + "</div>")
                + "</div></body></html>";
    }

    private String safeCssFontFamily() {
        String fontFamily = safeText(getPreviewFontFamily(), "Helvetica");
        if ("Times New Roman".equalsIgnoreCase(fontFamily)) {
            return "'Times New Roman', Times, serif";
        }
        if ("Courier".equalsIgnoreCase(fontFamily)) {
            return "'Courier New', Courier, monospace";
        }
        if ("Verdana".equalsIgnoreCase(fontFamily)) {
            return "Verdana, Arial, sans-serif";
        }
        if ("Georgia".equalsIgnoreCase(fontFamily)) {
            return "Georgia, 'Times New Roman', serif";
        }
        return "Helvetica, Arial, sans-serif";
    }

    private String resolveBrowserExecutable() {
        String[] candidates = {
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        };
        for (String candidate : candidates) {
            if (Files.exists(Path.of(candidate))) {
                return candidate;
            }
        }
        throw new IllegalStateException("No supported browser executable found for PDF generation.");
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

    private Integer resolveCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        Object organizationId = facesContext.getExternalContext().getSessionMap().get("organizationId");
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private Doctor getSelectedDoctor() {
        return selectedDoctor;
    }

    private Patient getSelectedPatient() {
        return selectedPatient;
    }

    private Medicine resolveMedicine(Integer medicineId) {
        if (medicineId == null) {
            return null;
        }
        for (Medicine medicine : medicineList) {
            if (medicine != null && medicineId.equals(medicine.getId())) {
                return medicine;
            }
        }
        return medicineService.getMedicineById(medicineId);
    }

    private Doctor findDoctorInList(Integer doctorId) {
        if (doctorId == null) {
            return null;
        }
        for (Doctor doctor : doctorList) {
            if (doctor != null && doctorId.equals(doctor.getId())) {
                return doctor;
            }
        }
        return null;
    }

    private Patient findPatientInList(Integer patientId) {
        if (patientId == null) {
            return null;
        }
        for (Patient patient : patientList) {
            if (patient != null && patientId.equals(patient.getId())) {
                return patient;
            }
        }
        return null;
    }

    private String buildMedicineDescription(Medicine medicine) {
        if (medicine == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(safeText(medicine.getMedicineName(), ""));
        if (!isBlank(medicine.getStrength())) {
            builder.append(" ").append(medicine.getStrength().trim());
        }
        if (!isBlank(medicine.getUnit())) {
            builder.append(" (").append(medicine.getUnit().trim()).append(")");
        }
        return builder.toString().trim();
    }

    private String generateConsultationNumber() {
        return "CONS-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    }

    private ClinicSettings fallbackClinicSettings(ClinicSettings settings) {
        return settings == null ? createDefaultClinicSettings() : settings;
    }

    private PrescriptionSettings fallbackPrescriptionSettings(PrescriptionSettings settings) {
        return settings == null ? createDefaultPrescriptionSettings() : settings;
    }

    private InvoiceSettings fallbackInvoiceSettings(InvoiceSettings settings) {
        return settings == null ? createDefaultInvoiceSettings() : settings;
    }

    private MedicalCertificateSettings fallbackMedicalCertificateSettings(MedicalCertificateSettings settings) {
        return settings == null ? createDefaultMedicalCertificateSettings() : settings;
    }

    private ClinicSettings createDefaultClinicSettings() {
        ClinicSettings settings = new ClinicSettings();
        settings.setClinicName("XXX XXXX XXX");
        settings.setClinicTagline("XXX XXXX XXX XXX");
        settings.setDoctorName("Dr. XXX XXXX XXX");
        settings.setDoctorQualification("MD Pediatrics");
        settings.setDoctorSpecialization("Senior Consultant");
        settings.setClinicEmail("xxx@xxxx.com");
        settings.setRegistrationNumber("XXX88");
        settings.setClinicAddress("Clinic address");
        settings.setAppointmentContact("XXXX1 3XX38");
        settings.setScheduleLineOne("Days : Mon - Sat | Timings : 9.00 AM - 12.00 PM & 6.00 PM - 9.00 PM");
        settings.setScheduleLineTwo("Sunday : 10.00 AM - 1.00 PM | Tuesday : 6.00 PM - 9.00 PM");
        settings.setConsultationFee(BigDecimal.ZERO);
        settings.setMedicalCertificateFee(BigDecimal.ZERO);
        settings.setRequireInvoice(true);
        settings.setBaseCurrencySymbol("");
        return settings;
    }

    private PrescriptionSettings createDefaultPrescriptionSettings() {
        PrescriptionSettings settings = new PrescriptionSettings();
        settings.setThemeColor("#0F766E");
        settings.setTextColor("#111827");
        settings.setHeaderTitle("Medical Prescription");
        settings.setClinicalNotesLabel("Clinical Notes");
        settings.setSymptomsLabel("Symptoms");
        settings.setFamilyHistoryLabel("Family History");
        settings.setVitalsLabel("Vitals");
        settings.setFindingsLabel("Findings");
        settings.setDiagnosisLabel("Diagnosis");
        settings.setPrescriptionLabel("Prescription");
        settings.setMedicationColumnLabel("Medications");
        settings.setDoseColumnLabel("Dose");
        settings.setFrequencyColumnLabel("Frequency");
        settings.setDurationColumnLabel("Duration");
        settings.setRemarksColumnLabel("Remarks");
        settings.setFollowUpLabel("Follow Up");
        settings.setFooterNote("Review after 3 days or earlier if symptoms worsen.");
        settings.setFontFamily("Helvetica");
        settings.setFontSize(12);
        settings.setLogoSize("Medium");
        settings.setShowClinicLogo(true);
        settings.setShowDoctorSignature(true);
        settings.setShowPatientAge(true);
        settings.setShowPatientGender(true);
        settings.setShowSymptomsSection(true);
        settings.setShowFamilyHistorySection(true);
        settings.setShowVitalsSection(true);
        settings.setShowFindingsSection(true);
        settings.setShowDiagnosisSection(true);
        return settings;
    }

    private InvoiceSettings createDefaultInvoiceSettings() {
        InvoiceSettings settings = new InvoiceSettings();
        settings.setThemeColor("#0F766E");
        settings.setTextColor("#111827");
        settings.setInvoiceTitle("Tax Invoice");
        settings.setInvoiceNumberLabel("Invoice No");
        settings.setBillFromLabel("Bill From");
        settings.setBillToLabel("Bill To");
        settings.setIssueDateLabel("Issue Date");
        settings.setDueDateLabel("Due Date");
        settings.setPaidByLabel("Paid By");
        settings.setDescriptionColumnLabel("Description");
        settings.setQuantityColumnLabel("Qty");
        settings.setPriceColumnLabel("Price");
        settings.setTotalColumnLabel("Total");
        settings.setGrandTotalLabel("Grand Total");
        settings.setFooterNote("Thank you for visiting the clinic.");
        settings.setFontFamily("Helvetica");
        settings.setFontSize(12);
        settings.setLogoSize("Medium");
        settings.setShowClinicLogo(true);
        return settings;
    }

    private MedicalCertificateSettings createDefaultMedicalCertificateSettings() {
        MedicalCertificateSettings settings = new MedicalCertificateSettings();
        settings.setThemeColor("#0F766E");
        settings.setTextColor("#111827");
        settings.setCertificateTitle("Medical Certificate");
        settings.setIntroLine("I hereby certify that the patient named below was examined and treated under my care.");
        settings.setBodyParagraphOne("Based on the examination and treatment, the patient was advised to rest and continue the prescribed course as medically indicated.");
        settings.setDiseaseLabel("Nature Of Disease");
        settings.setTreatmentDurationLabel("Duration Of Treatment");
        settings.setPatientSignatureLabel("Signature Of Patient");
        settings.setPlaceLabel("Place");
        settings.setDateLabel("Date");
        settings.setFontFamily("Helvetica");
        settings.setFontSize(12);
        settings.setLogoSize("Medium");
        settings.setShowClinicLogo(true);
        return settings;
    }

    private BigDecimal scaleAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeHexColor(String value, String fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        String normalized = value.trim();
        if (!normalized.startsWith("#")) {
            normalized = "#" + normalized;
        }
        return normalized.matches("^#[0-9A-Fa-f]{6}$") ? normalized.toUpperCase() : fallback;
    }

    private String resolveImageMimeType(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return "image/png";
        }
        if ((imageBytes[0] & 0xFF) == 0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
            return "image/png";
        }
        if ((imageBytes[0] & 0xFF) == 0xFF && (imageBytes[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        return "image/png";
    }

    private String escapeHtml(String value) {
        return safeText(value, "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String safeHtmlFragment(String html) {
        return html == null ? "" : html.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private void deleteRecursively(Path path) {
        try {
            if (Files.notExists(path)) {
                return;
            }
            Files.walk(path).sorted((left, right) -> right.compareTo(left)).forEach(currentPath -> {
                try {
                    Files.deleteIfExists(currentPath);
                } catch (Exception ignored) {
                    // Best-effort temp cleanup.
                }
            });
        } catch (Exception ignored) {
            // Best-effort temp cleanup.
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public static class ConsultationMedicineFormLine implements Serializable {

        private Integer medicineId;
        private String medicineName;
        private String descriptionText;
        private String dose;
        private String frequency;
        private String durationText;
        private String remarks;
        private BigDecimal quantity = BigDecimal.ONE;
        private BigDecimal unitPrice = BigDecimal.ZERO;
        private BigDecimal lineTotal = BigDecimal.ZERO;

        public void recalculateLineTotal() {
            BigDecimal safeQuantity = quantity == null ? BigDecimal.ZERO : quantity;
            BigDecimal safeUnitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
            lineTotal = safeQuantity.multiply(safeUnitPrice).setScale(2, RoundingMode.HALF_UP);
        }

        public Integer getMedicineId() { return medicineId; }
        public void setMedicineId(Integer medicineId) { this.medicineId = medicineId; }
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public String getDescriptionText() { return descriptionText; }
        public void setDescriptionText(String descriptionText) { this.descriptionText = descriptionText; }
        public String getDose() { return dose; }
        public void setDose(String dose) { this.dose = dose; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getDurationText() { return durationText; }
        public void setDurationText(String durationText) { this.durationText = durationText; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

}
