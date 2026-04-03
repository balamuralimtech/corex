package com.persist.carex.settings;

import com.persist.coretix.modal.systemmanagement.Organizations;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "ClinicSettings")
public class ClinicSettings implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organizations organization;

    @Column(name = "clinic_name", nullable = false, length = 150)
    private String clinicName;

    @Column(name = "clinic_tagline", length = 150)
    private String clinicTagline;

    @Column(name = "doctor_name", length = 120)
    private String doctorName;

    @Column(name = "doctor_qualification", length = 150)
    private String doctorQualification;

    @Column(name = "doctor_specialization", length = 255)
    private String doctorSpecialization;

    @Column(name = "clinic_email", length = 120)
    private String clinicEmail;

    @Column(name = "registration_number", length = 60)
    private String registrationNumber;

    @Column(name = "clinic_address", length = 255)
    private String clinicAddress;

    @Column(name = "appointment_contact", length = 60)
    private String appointmentContact;

    @Column(name = "schedule_line_one", length = 255)
    private String scheduleLineOne;

    @Column(name = "schedule_line_two", length = 255)
    private String scheduleLineTwo;

    @Column(name = "weekdays_open", nullable = false)
    private boolean weekdaysOpen = true;

    @Column(name = "saturday_open", nullable = false)
    private boolean saturdayOpen = true;

    @Column(name = "sunday_open", nullable = false)
    private boolean sundayOpen;

    @Temporal(TemporalType.TIME)
    @Column(name = "opening_time", nullable = false)
    private Date openingTime;

    @Temporal(TemporalType.TIME)
    @Column(name = "closing_time", nullable = false)
    private Date closingTime;

    @Temporal(TemporalType.TIME)
    @Column(name = "break_start_time")
    private Date breakStartTime;

    @Temporal(TemporalType.TIME)
    @Column(name = "break_end_time")
    private Date breakEndTime;

    @Column(name = "consultation_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(name = "followup_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal followupFee = BigDecimal.ZERO;

    @Column(name = "registration_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal registrationFee = BigDecimal.ZERO;

    @Column(name = "medical_certificate_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal medicalCertificateFee = BigDecimal.ZERO;

    @Column(name = "base_currency_code", length = 10)
    private String baseCurrencyCode;

    @Column(name = "base_currency_symbol", length = 10)
    private String baseCurrencySymbol;

    @Column(name = "base_currency_name", length = 100)
    private String baseCurrencyName;

    @Column(name = "require_invoice", nullable = false)
    private boolean requireInvoice = true;

    @Column(name = "require_medical_certificate", nullable = false)
    private boolean requireMedicalCertificate;

    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes = 15;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Organizations getOrganization() { return organization; }
    public void setOrganization(Organizations organization) { this.organization = organization; }
    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
    public String getClinicTagline() { return clinicTagline; }
    public void setClinicTagline(String clinicTagline) { this.clinicTagline = clinicTagline; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDoctorQualification() { return doctorQualification; }
    public void setDoctorQualification(String doctorQualification) { this.doctorQualification = doctorQualification; }
    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }
    public String getClinicEmail() { return clinicEmail; }
    public void setClinicEmail(String clinicEmail) { this.clinicEmail = clinicEmail; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }
    public String getAppointmentContact() { return appointmentContact; }
    public void setAppointmentContact(String appointmentContact) { this.appointmentContact = appointmentContact; }
    public String getScheduleLineOne() { return scheduleLineOne; }
    public void setScheduleLineOne(String scheduleLineOne) { this.scheduleLineOne = scheduleLineOne; }
    public String getScheduleLineTwo() { return scheduleLineTwo; }
    public void setScheduleLineTwo(String scheduleLineTwo) { this.scheduleLineTwo = scheduleLineTwo; }
    public boolean isWeekdaysOpen() { return weekdaysOpen; }
    public void setWeekdaysOpen(boolean weekdaysOpen) { this.weekdaysOpen = weekdaysOpen; }
    public boolean isSaturdayOpen() { return saturdayOpen; }
    public void setSaturdayOpen(boolean saturdayOpen) { this.saturdayOpen = saturdayOpen; }
    public boolean isSundayOpen() { return sundayOpen; }
    public void setSundayOpen(boolean sundayOpen) { this.sundayOpen = sundayOpen; }
    public Date getOpeningTime() { return openingTime; }
    public void setOpeningTime(Date openingTime) { this.openingTime = openingTime; }
    public Date getClosingTime() { return closingTime; }
    public void setClosingTime(Date closingTime) { this.closingTime = closingTime; }
    public Date getBreakStartTime() { return breakStartTime; }
    public void setBreakStartTime(Date breakStartTime) { this.breakStartTime = breakStartTime; }
    public Date getBreakEndTime() { return breakEndTime; }
    public void setBreakEndTime(Date breakEndTime) { this.breakEndTime = breakEndTime; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public BigDecimal getFollowupFee() { return followupFee; }
    public void setFollowupFee(BigDecimal followupFee) { this.followupFee = followupFee; }
    public BigDecimal getRegistrationFee() { return registrationFee; }
    public void setRegistrationFee(BigDecimal registrationFee) { this.registrationFee = registrationFee; }
    public BigDecimal getMedicalCertificateFee() { return medicalCertificateFee; }
    public void setMedicalCertificateFee(BigDecimal medicalCertificateFee) { this.medicalCertificateFee = medicalCertificateFee; }
    public String getBaseCurrencyCode() { return baseCurrencyCode; }
    public void setBaseCurrencyCode(String baseCurrencyCode) { this.baseCurrencyCode = baseCurrencyCode; }
    public String getBaseCurrencySymbol() { return baseCurrencySymbol; }
    public void setBaseCurrencySymbol(String baseCurrencySymbol) { this.baseCurrencySymbol = baseCurrencySymbol; }
    public String getBaseCurrencyName() { return baseCurrencyName; }
    public void setBaseCurrencyName(String baseCurrencyName) { this.baseCurrencyName = baseCurrencyName; }
    public boolean isRequireInvoice() { return requireInvoice; }
    public void setRequireInvoice(boolean requireInvoice) { this.requireInvoice = requireInvoice; }
    public boolean isRequireMedicalCertificate() { return requireMedicalCertificate; }
    public void setRequireMedicalCertificate(boolean requireMedicalCertificate) { this.requireMedicalCertificate = requireMedicalCertificate; }
    public Integer getSlotDurationMinutes() { return slotDurationMinutes; }
    public void setSlotDurationMinutes(Integer slotDurationMinutes) { this.slotDurationMinutes = slotDurationMinutes; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
