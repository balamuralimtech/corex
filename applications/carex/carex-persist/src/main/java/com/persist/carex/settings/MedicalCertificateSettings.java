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
@Table(name = "MedicalCertificateSettings")
public class MedicalCertificateSettings implements Serializable {

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

    @Column(name = "certificate_title", nullable = false, length = 150)
    private String certificateTitle = "Medical Certificate";

    @Column(name = "intro_line", length = 255)
    private String introLine;

    @Column(name = "body_paragraph_one", length = 1000)
    private String bodyParagraphOne;

    @Column(name = "body_paragraph_two", length = 1000)
    private String bodyParagraphTwo;

    @Column(name = "disease_label", length = 80)
    private String diseaseLabel = "Nature Of Disease";

    @Column(name = "treatment_duration_label", length = 80)
    private String treatmentDurationLabel = "Duration Of Treatment";

    @Column(name = "patient_signature_label", length = 80)
    private String patientSignatureLabel = "Signature Of Patient";

    @Column(name = "place_label", length = 40)
    private String placeLabel = "Place";

    @Column(name = "date_label", length = 40)
    private String dateLabel = "Date";

    @Column(name = "font_family", nullable = false, length = 50)
    private String fontFamily = "Helvetica";

    @Column(name = "font_size", nullable = false)
    private Integer fontSize = 12;

    @Column(name = "logo_size", nullable = false, length = 20)
    private String logoSize = "Medium";

    @Column(name = "show_clinic_logo", nullable = false)
    private boolean showClinicLogo = true;

    @Column(name = "show_watermark", nullable = false)
    private boolean showWatermark;

    @Column(name = "watermark_text", length = 100)
    private String watermarkText;

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
    public String getCertificateTitle() { return certificateTitle; }
    public void setCertificateTitle(String certificateTitle) { this.certificateTitle = certificateTitle; }
    public String getIntroLine() { return introLine; }
    public void setIntroLine(String introLine) { this.introLine = introLine; }
    public String getBodyParagraphOne() { return bodyParagraphOne; }
    public void setBodyParagraphOne(String bodyParagraphOne) { this.bodyParagraphOne = bodyParagraphOne; }
    public String getBodyParagraphTwo() { return bodyParagraphTwo; }
    public void setBodyParagraphTwo(String bodyParagraphTwo) { this.bodyParagraphTwo = bodyParagraphTwo; }
    public String getDiseaseLabel() { return diseaseLabel; }
    public void setDiseaseLabel(String diseaseLabel) { this.diseaseLabel = diseaseLabel; }
    public String getTreatmentDurationLabel() { return treatmentDurationLabel; }
    public void setTreatmentDurationLabel(String treatmentDurationLabel) { this.treatmentDurationLabel = treatmentDurationLabel; }
    public String getPatientSignatureLabel() { return patientSignatureLabel; }
    public void setPatientSignatureLabel(String patientSignatureLabel) { this.patientSignatureLabel = patientSignatureLabel; }
    public String getPlaceLabel() { return placeLabel; }
    public void setPlaceLabel(String placeLabel) { this.placeLabel = placeLabel; }
    public String getDateLabel() { return dateLabel; }
    public void setDateLabel(String dateLabel) { this.dateLabel = dateLabel; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }
    public String getLogoSize() { return logoSize; }
    public void setLogoSize(String logoSize) { this.logoSize = logoSize; }
    public boolean isShowClinicLogo() { return showClinicLogo; }
    public void setShowClinicLogo(boolean showClinicLogo) { this.showClinicLogo = showClinicLogo; }
    public boolean isShowWatermark() { return showWatermark; }
    public void setShowWatermark(boolean showWatermark) { this.showWatermark = showWatermark; }
    public String getWatermarkText() { return watermarkText; }
    public void setWatermarkText(String watermarkText) { this.watermarkText = watermarkText; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
