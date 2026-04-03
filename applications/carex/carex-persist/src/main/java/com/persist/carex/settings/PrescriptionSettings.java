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
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "PrescriptionSettings")
public class PrescriptionSettings implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organizations organization;

    @Column(name = "page_size", nullable = false, length = 20)
    private String pageSize = "A4";

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName = "Classic";

    @Column(name = "theme_color", nullable = false, length = 20)
    private String themeColor = "#0F766E";

    @Column(name = "text_color", nullable = false, length = 20)
    private String textColor = "#111827";

    @Column(name = "header_title", nullable = false, length = 150)
    private String headerTitle = "Medical Prescription";

    @Column(name = "clinical_notes_label", length = 80)
    private String clinicalNotesLabel = "Clinical Notes";

    @Column(name = "symptoms_label", length = 80)
    private String symptomsLabel = "Symptoms";

    @Column(name = "family_history_label", length = 80)
    private String familyHistoryLabel = "Family History";

    @Column(name = "vitals_label", length = 80)
    private String vitalsLabel = "Vitals";

    @Column(name = "findings_label", length = 80)
    private String findingsLabel = "Findings";

    @Column(name = "diagnosis_label", length = 80)
    private String diagnosisLabel = "Diagnosis";

    @Column(name = "prescription_label", length = 80)
    private String prescriptionLabel = "Prescription";

    @Column(name = "medication_column_label", length = 60)
    private String medicationColumnLabel = "Medications";

    @Column(name = "dose_column_label", length = 60)
    private String doseColumnLabel = "Dose";

    @Column(name = "frequency_column_label", length = 60)
    private String frequencyColumnLabel = "Frequency";

    @Column(name = "duration_column_label", length = 60)
    private String durationColumnLabel = "Duration";

    @Column(name = "remarks_column_label", length = 60)
    private String remarksColumnLabel = "Remarks";

    @Column(name = "follow_up_label", length = 80)
    private String followUpLabel = "Follow Up";

    @Column(name = "footer_note", length = 255)
    private String footerNote;

    @Column(name = "font_family", nullable = false, length = 50)
    private String fontFamily = "Helvetica";

    @Column(name = "font_size", nullable = false)
    private Integer fontSize = 12;

    @Column(name = "logo_size", nullable = false, length = 20)
    private String logoSize = "Medium";

    @Column(name = "margin_top_mm", nullable = false)
    private Integer marginTopMm = 12;

    @Column(name = "margin_right_mm", nullable = false)
    private Integer marginRightMm = 12;

    @Column(name = "margin_bottom_mm", nullable = false)
    private Integer marginBottomMm = 12;

    @Column(name = "margin_left_mm", nullable = false)
    private Integer marginLeftMm = 12;

    @Column(name = "show_clinic_logo", nullable = false)
    private boolean showClinicLogo = true;

    @Column(name = "show_doctor_signature", nullable = false)
    private boolean showDoctorSignature = true;

    @Column(name = "show_watermark", nullable = false)
    private boolean showWatermark;

    @Column(name = "watermark_text", length = 100)
    private String watermarkText;

    @Column(name = "show_patient_age", nullable = false)
    private boolean showPatientAge = true;

    @Column(name = "show_patient_gender", nullable = false)
    private boolean showPatientGender = true;

    @Column(name = "show_symptoms_section", nullable = false)
    private boolean showSymptomsSection = true;

    @Column(name = "show_family_history_section", nullable = false)
    private boolean showFamilyHistorySection = true;

    @Column(name = "show_vitals_section", nullable = false)
    private boolean showVitalsSection = true;

    @Column(name = "show_findings_section", nullable = false)
    private boolean showFindingsSection = true;

    @Column(name = "show_diagnosis_section", nullable = false)
    private boolean showDiagnosisSection = true;

    @Column(name = "show_qr_code", nullable = false)
    private boolean showQrCode;

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
    public String getPageSize() { return pageSize; }
    public void setPageSize(String pageSize) { this.pageSize = pageSize; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }
    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }
    public String getHeaderTitle() { return headerTitle; }
    public void setHeaderTitle(String headerTitle) { this.headerTitle = headerTitle; }
    public String getClinicalNotesLabel() { return clinicalNotesLabel; }
    public void setClinicalNotesLabel(String clinicalNotesLabel) { this.clinicalNotesLabel = clinicalNotesLabel; }
    public String getSymptomsLabel() { return symptomsLabel; }
    public void setSymptomsLabel(String symptomsLabel) { this.symptomsLabel = symptomsLabel; }
    public String getFamilyHistoryLabel() { return familyHistoryLabel; }
    public void setFamilyHistoryLabel(String familyHistoryLabel) { this.familyHistoryLabel = familyHistoryLabel; }
    public String getVitalsLabel() { return vitalsLabel; }
    public void setVitalsLabel(String vitalsLabel) { this.vitalsLabel = vitalsLabel; }
    public String getFindingsLabel() { return findingsLabel; }
    public void setFindingsLabel(String findingsLabel) { this.findingsLabel = findingsLabel; }
    public String getDiagnosisLabel() { return diagnosisLabel; }
    public void setDiagnosisLabel(String diagnosisLabel) { this.diagnosisLabel = diagnosisLabel; }
    public String getPrescriptionLabel() { return prescriptionLabel; }
    public void setPrescriptionLabel(String prescriptionLabel) { this.prescriptionLabel = prescriptionLabel; }
    public String getMedicationColumnLabel() { return medicationColumnLabel; }
    public void setMedicationColumnLabel(String medicationColumnLabel) { this.medicationColumnLabel = medicationColumnLabel; }
    public String getDoseColumnLabel() { return doseColumnLabel; }
    public void setDoseColumnLabel(String doseColumnLabel) { this.doseColumnLabel = doseColumnLabel; }
    public String getFrequencyColumnLabel() { return frequencyColumnLabel; }
    public void setFrequencyColumnLabel(String frequencyColumnLabel) { this.frequencyColumnLabel = frequencyColumnLabel; }
    public String getDurationColumnLabel() { return durationColumnLabel; }
    public void setDurationColumnLabel(String durationColumnLabel) { this.durationColumnLabel = durationColumnLabel; }
    public String getRemarksColumnLabel() { return remarksColumnLabel; }
    public void setRemarksColumnLabel(String remarksColumnLabel) { this.remarksColumnLabel = remarksColumnLabel; }
    public String getFollowUpLabel() { return followUpLabel; }
    public void setFollowUpLabel(String followUpLabel) { this.followUpLabel = followUpLabel; }
    public String getFooterNote() { return footerNote; }
    public void setFooterNote(String footerNote) { this.footerNote = footerNote; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }
    public String getLogoSize() { return logoSize; }
    public void setLogoSize(String logoSize) { this.logoSize = logoSize; }
    public Integer getMarginTopMm() { return marginTopMm; }
    public void setMarginTopMm(Integer marginTopMm) { this.marginTopMm = marginTopMm; }
    public Integer getMarginRightMm() { return marginRightMm; }
    public void setMarginRightMm(Integer marginRightMm) { this.marginRightMm = marginRightMm; }
    public Integer getMarginBottomMm() { return marginBottomMm; }
    public void setMarginBottomMm(Integer marginBottomMm) { this.marginBottomMm = marginBottomMm; }
    public Integer getMarginLeftMm() { return marginLeftMm; }
    public void setMarginLeftMm(Integer marginLeftMm) { this.marginLeftMm = marginLeftMm; }
    public boolean isShowClinicLogo() { return showClinicLogo; }
    public void setShowClinicLogo(boolean showClinicLogo) { this.showClinicLogo = showClinicLogo; }
    public boolean isShowDoctorSignature() { return showDoctorSignature; }
    public void setShowDoctorSignature(boolean showDoctorSignature) { this.showDoctorSignature = showDoctorSignature; }
    public boolean isShowWatermark() { return showWatermark; }
    public void setShowWatermark(boolean showWatermark) { this.showWatermark = showWatermark; }
    public String getWatermarkText() { return watermarkText; }
    public void setWatermarkText(String watermarkText) { this.watermarkText = watermarkText; }
    public boolean isShowPatientAge() { return showPatientAge; }
    public void setShowPatientAge(boolean showPatientAge) { this.showPatientAge = showPatientAge; }
    public boolean isShowPatientGender() { return showPatientGender; }
    public void setShowPatientGender(boolean showPatientGender) { this.showPatientGender = showPatientGender; }
    public boolean isShowSymptomsSection() { return showSymptomsSection; }
    public void setShowSymptomsSection(boolean showSymptomsSection) { this.showSymptomsSection = showSymptomsSection; }
    public boolean isShowFamilyHistorySection() { return showFamilyHistorySection; }
    public void setShowFamilyHistorySection(boolean showFamilyHistorySection) { this.showFamilyHistorySection = showFamilyHistorySection; }
    public boolean isShowVitalsSection() { return showVitalsSection; }
    public void setShowVitalsSection(boolean showVitalsSection) { this.showVitalsSection = showVitalsSection; }
    public boolean isShowFindingsSection() { return showFindingsSection; }
    public void setShowFindingsSection(boolean showFindingsSection) { this.showFindingsSection = showFindingsSection; }
    public boolean isShowDiagnosisSection() { return showDiagnosisSection; }
    public void setShowDiagnosisSection(boolean showDiagnosisSection) { this.showDiagnosisSection = showDiagnosisSection; }
    public boolean isShowQrCode() { return showQrCode; }
    public void setShowQrCode(boolean showQrCode) { this.showQrCode = showQrCode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
