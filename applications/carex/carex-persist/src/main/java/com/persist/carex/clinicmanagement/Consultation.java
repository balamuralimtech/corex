package com.persist.carex.clinicmanagement;

import com.persist.coretix.modal.systemmanagement.Organizations;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Consultations")
public class Consultation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organizations organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "consultation_number", nullable = false, length = 40, unique = true)
    private String consultationNumber;

    @Column(name = "consultation_date", nullable = false)
    private Timestamp consultationDate;

    @Column(name = "token_number")
    private Integer tokenNumber;

    @Column(name = "patient_age_years")
    private Integer patientAgeYears;

    @Column(name = "temperature_celsius", length = 20)
    private String temperatureCelsius;

    @Column(name = "weight_kg", length = 20)
    private String weightKg;

    @Column(name = "blood_pressure", length = 30)
    private String bloodPressure;

    @Column(name = "symptoms", length = 2000)
    private String symptoms;

    @Column(name = "family_history", length = 1000)
    private String familyHistory;

    @Column(name = "vitals", length = 1000)
    private String vitals;

    @Column(name = "findings", length = 2000)
    private String findings;

    @Column(name = "diagnosis", length = 2000)
    private String diagnosis;

    @Column(name = "follow_up_note", length = 500)
    private String followUpNote;

    @Column(name = "doctor_notes", length = 2000)
    private String doctorNotes;

    @Column(name = "issue_invoice", nullable = false)
    private boolean issueInvoice = true;

    @Column(name = "issue_medical_certificate", nullable = false)
    private boolean issueMedicalCertificate;

    @Column(name = "invoice_paid_by", length = 80)
    private String invoicePaidBy;

    @Column(name = "invoice_issue_date")
    private Timestamp invoiceIssueDate;

    @Column(name = "invoice_due_date")
    private Timestamp invoiceDueDate;

    @Column(name = "medical_certificate_disease", length = 500)
    private String medicalCertificateDisease;

    @Column(name = "medical_certificate_treatment_duration", length = 255)
    private String medicalCertificateTreatmentDuration;

    @Column(name = "medical_certificate_place", length = 120)
    private String medicalCertificatePlace;

    @Column(name = "consultation_fee", precision = 12, scale = 2)
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(name = "medicine_total", precision = 12, scale = 2)
    private BigDecimal medicineTotal = BigDecimal.ZERO;

    @Column(name = "medical_certificate_fee", precision = 12, scale = 2)
    private BigDecimal medicalCertificateFee = BigDecimal.ZERO;

    @Column(name = "invoice_total", precision = 12, scale = 2)
    private BigDecimal invoiceTotal = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "Completed";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber asc")
    private List<ConsultationMedicine> consultationMedicines = new ArrayList<>();

    public void addConsultationMedicine(ConsultationMedicine consultationMedicine) {
        consultationMedicine.setConsultation(this);
        consultationMedicines.add(consultationMedicine);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Organizations getOrganization() { return organization; }
    public void setOrganization(Organizations organization) { this.organization = organization; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getConsultationNumber() { return consultationNumber; }
    public void setConsultationNumber(String consultationNumber) { this.consultationNumber = consultationNumber; }
    public Timestamp getConsultationDate() { return consultationDate; }
    public void setConsultationDate(Timestamp consultationDate) { this.consultationDate = consultationDate; }
    public Integer getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(Integer tokenNumber) { this.tokenNumber = tokenNumber; }
    public Integer getPatientAgeYears() { return patientAgeYears; }
    public void setPatientAgeYears(Integer patientAgeYears) { this.patientAgeYears = patientAgeYears; }
    public String getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(String temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }
    public String getWeightKg() { return weightKg; }
    public void setWeightKg(String weightKg) { this.weightKg = weightKg; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }
    public String getVitals() { return vitals; }
    public void setVitals(String vitals) { this.vitals = vitals; }
    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getFollowUpNote() { return followUpNote; }
    public void setFollowUpNote(String followUpNote) { this.followUpNote = followUpNote; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
    public boolean isIssueInvoice() { return issueInvoice; }
    public void setIssueInvoice(boolean issueInvoice) { this.issueInvoice = issueInvoice; }
    public boolean isIssueMedicalCertificate() { return issueMedicalCertificate; }
    public void setIssueMedicalCertificate(boolean issueMedicalCertificate) { this.issueMedicalCertificate = issueMedicalCertificate; }
    public String getInvoicePaidBy() { return invoicePaidBy; }
    public void setInvoicePaidBy(String invoicePaidBy) { this.invoicePaidBy = invoicePaidBy; }
    public Timestamp getInvoiceIssueDate() { return invoiceIssueDate; }
    public void setInvoiceIssueDate(Timestamp invoiceIssueDate) { this.invoiceIssueDate = invoiceIssueDate; }
    public Timestamp getInvoiceDueDate() { return invoiceDueDate; }
    public void setInvoiceDueDate(Timestamp invoiceDueDate) { this.invoiceDueDate = invoiceDueDate; }
    public String getMedicalCertificateDisease() { return medicalCertificateDisease; }
    public void setMedicalCertificateDisease(String medicalCertificateDisease) { this.medicalCertificateDisease = medicalCertificateDisease; }
    public String getMedicalCertificateTreatmentDuration() { return medicalCertificateTreatmentDuration; }
    public void setMedicalCertificateTreatmentDuration(String medicalCertificateTreatmentDuration) { this.medicalCertificateTreatmentDuration = medicalCertificateTreatmentDuration; }
    public String getMedicalCertificatePlace() { return medicalCertificatePlace; }
    public void setMedicalCertificatePlace(String medicalCertificatePlace) { this.medicalCertificatePlace = medicalCertificatePlace; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public BigDecimal getMedicineTotal() { return medicineTotal; }
    public void setMedicineTotal(BigDecimal medicineTotal) { this.medicineTotal = medicineTotal; }
    public BigDecimal getMedicalCertificateFee() { return medicalCertificateFee; }
    public void setMedicalCertificateFee(BigDecimal medicalCertificateFee) { this.medicalCertificateFee = medicalCertificateFee; }
    public BigDecimal getInvoiceTotal() { return invoiceTotal; }
    public void setInvoiceTotal(BigDecimal invoiceTotal) { this.invoiceTotal = invoiceTotal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public List<ConsultationMedicine> getConsultationMedicines() { return consultationMedicines; }
    public void setConsultationMedicines(List<ConsultationMedicine> consultationMedicines) { this.consultationMedicines = consultationMedicines; }
}
