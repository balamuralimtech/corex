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
@Table(name = "InvoiceSettings")
public class InvoiceSettings implements Serializable {

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

    @Column(name = "invoice_title", nullable = false, length = 150)
    private String invoiceTitle = "Tax Invoice";

    @Column(name = "invoice_number_label", length = 80)
    private String invoiceNumberLabel = "Invoice No";

    @Column(name = "bill_from_label", length = 80)
    private String billFromLabel = "Bill From";

    @Column(name = "bill_to_label", length = 80)
    private String billToLabel = "Bill To";

    @Column(name = "issue_date_label", length = 80)
    private String issueDateLabel = "Issue Date";

    @Column(name = "due_date_label", length = 80)
    private String dueDateLabel = "Due Date";

    @Column(name = "paid_by_label", length = 80)
    private String paidByLabel = "Paid By";

    @Column(name = "description_column_label", length = 80)
    private String descriptionColumnLabel = "Description";

    @Column(name = "quantity_column_label", length = 40)
    private String quantityColumnLabel = "Qty";

    @Column(name = "price_column_label", length = 40)
    private String priceColumnLabel = "Price";

    @Column(name = "total_column_label", length = 40)
    private String totalColumnLabel = "Total";

    @Column(name = "grand_total_label", length = 80)
    private String grandTotalLabel = "Grand Total";

    @Column(name = "footer_note", length = 255)
    private String footerNote;

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
    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }
    public String getInvoiceNumberLabel() { return invoiceNumberLabel; }
    public void setInvoiceNumberLabel(String invoiceNumberLabel) { this.invoiceNumberLabel = invoiceNumberLabel; }
    public String getBillFromLabel() { return billFromLabel; }
    public void setBillFromLabel(String billFromLabel) { this.billFromLabel = billFromLabel; }
    public String getBillToLabel() { return billToLabel; }
    public void setBillToLabel(String billToLabel) { this.billToLabel = billToLabel; }
    public String getIssueDateLabel() { return issueDateLabel; }
    public void setIssueDateLabel(String issueDateLabel) { this.issueDateLabel = issueDateLabel; }
    public String getDueDateLabel() { return dueDateLabel; }
    public void setDueDateLabel(String dueDateLabel) { this.dueDateLabel = dueDateLabel; }
    public String getPaidByLabel() { return paidByLabel; }
    public void setPaidByLabel(String paidByLabel) { this.paidByLabel = paidByLabel; }
    public String getDescriptionColumnLabel() { return descriptionColumnLabel; }
    public void setDescriptionColumnLabel(String descriptionColumnLabel) { this.descriptionColumnLabel = descriptionColumnLabel; }
    public String getQuantityColumnLabel() { return quantityColumnLabel; }
    public void setQuantityColumnLabel(String quantityColumnLabel) { this.quantityColumnLabel = quantityColumnLabel; }
    public String getPriceColumnLabel() { return priceColumnLabel; }
    public void setPriceColumnLabel(String priceColumnLabel) { this.priceColumnLabel = priceColumnLabel; }
    public String getTotalColumnLabel() { return totalColumnLabel; }
    public void setTotalColumnLabel(String totalColumnLabel) { this.totalColumnLabel = totalColumnLabel; }
    public String getGrandTotalLabel() { return grandTotalLabel; }
    public void setGrandTotalLabel(String grandTotalLabel) { this.grandTotalLabel = grandTotalLabel; }
    public String getFooterNote() { return footerNote; }
    public void setFooterNote(String footerNote) { this.footerNote = footerNote; }
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
