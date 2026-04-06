package com.web.carex.clinicmanagement;

import com.module.carex.clinicmanagement.IMedicineService;
import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.carex.clinicmanagement.Medicine;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Named("medicineBean")
@Scope("session")
public class MedicineBean extends CarexManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private IMedicineService medicineService;

    @Inject
    private IOrganizationService organizationService;

    private List<Medicine> medicineList = new ArrayList<>();
    private List<Organizations> organizationList = new ArrayList<>();
    private Medicine medicine = new Medicine();
    private Medicine selectedMedicine;
    private Integer selectedOrganizationId;
    private Integer formOrganizationId;
    private boolean addOperation = true;
    private boolean initialized;
    private boolean medicineResultsLoaded;

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
        medicineList = new ArrayList<>();
        medicineResultsLoaded = false;
        initialized = true;
    }

    public void onOrganizationChange() {
        medicineList = new ArrayList<>();
        medicineResultsLoaded = false;
        PrimeFaces.current().ajax().update("form:medicineMainPanel", "form:messages");
    }

    public void onFormOrganizationChange() {
        PrimeFaces.current().ajax().update("form:medicineDialogPanel");
    }

    public void addButtonAction() {
        addOperation = true;
        medicine = new Medicine();
        medicine.setActive(true);
        medicine.setPurchasePrice(BigDecimal.ZERO);
        medicine.setSellingPrice(BigDecimal.ZERO);
        medicine.setStockQuantity(0);
        medicine.setReorderLevel(0);
        formOrganizationId = selectedOrganizationId;
        selectedMedicine = null;
    }

    public void confirmEditButtonAction() {
        if (selectedMedicine == null) {
            return;
        }
        addOperation = false;
        medicine = new Medicine();
        medicine.setId(selectedMedicine.getId());
        medicine.setOrganization(selectedMedicine.getOrganization());
        medicine.setMedicineCode(selectedMedicine.getMedicineCode());
        medicine.setMedicineName(selectedMedicine.getMedicineName());
        medicine.setGenericName(selectedMedicine.getGenericName());
        medicine.setCategory(selectedMedicine.getCategory());
        medicine.setUnit(selectedMedicine.getUnit());
        medicine.setStrength(selectedMedicine.getStrength());
        medicine.setManufacturer(selectedMedicine.getManufacturer());
        medicine.setPurchasePrice(selectedMedicine.getPurchasePrice());
        medicine.setSellingPrice(selectedMedicine.getSellingPrice());
        medicine.setStockQuantity(selectedMedicine.getStockQuantity());
        medicine.setReorderLevel(selectedMedicine.getReorderLevel());
        medicine.setActive(selectedMedicine.isActive());
        medicine.setNotes(selectedMedicine.getNotes());
        formOrganizationId = selectedMedicine.getOrganization() == null ? selectedOrganizationId : selectedMedicine.getOrganization().getId();
    }

    public void saveMedicine() {
        if (!validateForm()) {
            PrimeFaces.current().ajax().update("form:dialogMessages");
            return;
        }
        Organizations organization = organizationService.getOrganizationById(formOrganizationId);
        if (organization == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization missing", "Select a valid organization.");
            return;
        }
        medicine.setOrganization(organization);
        UserActivityTO userActivityTO = populateUserActivityTO();
        GeneralConstants result;
        if (addOperation) {
            userActivityTO.setActivityType("Add");
            result = medicineService.addMedicine(userActivityTO, medicine);
        } else {
            userActivityTO.setActivityType("Update");
            result = medicineService.updateMedicine(userActivityTO, medicine);
        }
        handleSaveResult(result);
    }

    public void confirmDeleteMedicine() {
        if (selectedMedicine == null) {
            return;
        }
        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType("Delete");
        GeneralConstants result = medicineService.deleteMedicine(userActivityTO, selectedMedicine);
        switch (result) {
            case SUCCESSFUL:
                addMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Medicine removed successfully.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Medicine does not exist anymore.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", "Unable to remove medicine.");
                break;
        }
        fetchMedicineList();
        PrimeFaces.current().ajax().update("form:medicineMainPanel", "form:messages");
    }

    public void fetchMedicineList() {
        medicineList = selectedOrganizationId == null
                ? new ArrayList<>()
                : new ArrayList<>(medicineService.getMedicinesByOrganizationId(selectedOrganizationId));
        medicineResultsLoaded = true;
    }

    public List<String> getAvailableUnits() {
        return Arrays.asList("Tablet", "Capsule", "Bottle", "Strip", "Vial", "Tube", "Sachet", "Piece", "Ml", "Mg", "Gram");
    }

    public List<String> getAvailableCategories() {
        return Arrays.asList("Antibiotic", "Analgesic", "Syrup", "Injection", "Supplement", "Topical", "General");
    }

    public List<Medicine> getMedicineList() { return medicineList; }
    public List<Organizations> getOrganizationList() { return organizationList; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Medicine getSelectedMedicine() { return selectedMedicine; }
    public void setSelectedMedicine(Medicine selectedMedicine) { this.selectedMedicine = selectedMedicine; }
    public Integer getSelectedOrganizationId() { return selectedOrganizationId; }
    public void setSelectedOrganizationId(Integer selectedOrganizationId) { this.selectedOrganizationId = resolveAccessibleOrganizationId(selectedOrganizationId); }
    public Integer getFormOrganizationId() { return formOrganizationId; }
    public void setFormOrganizationId(Integer formOrganizationId) { this.formOrganizationId = resolveAccessibleOrganizationId(formOrganizationId); }
    public boolean isAddOperation() { return addOperation; }
    public boolean isMedicineResultsLoaded() { return medicineResultsLoaded; }

    private boolean validateForm() {
        if (formOrganizationId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Organization required", "Select an organization.");
            return false;
        }
        if (isBlank(medicine.getMedicineCode())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Medicine code required", "Enter medicine code.");
            return false;
        }
        if (isBlank(medicine.getMedicineName())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Medicine name required", "Enter medicine name.");
            return false;
        }
        if (isBlank(medicine.getUnit())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Unit required", "Select a medicine unit.");
            return false;
        }
        if (medicine.getPurchasePrice() != null && medicine.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid purchase price", "Purchase price cannot be negative.");
            return false;
        }
        if (medicine.getSellingPrice() != null && medicine.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid selling price", "Selling price cannot be negative.");
            return false;
        }
        if (medicine.getStockQuantity() != null && medicine.getStockQuantity() < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid stock", "Stock quantity cannot be negative.");
            return false;
        }
        if (medicine.getReorderLevel() != null && medicine.getReorderLevel() < 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Invalid reorder level", "Reorder level cannot be negative.");
            return false;
        }
        return true;
    }

    private void handleSaveResult(GeneralConstants result) {
        switch (result) {
            case SUCCESSFUL:
                addMessage(FacesMessage.SEVERITY_INFO, "Saved", addOperation ? "Medicine added successfully." : "Medicine updated successfully.");
                fetchMedicineList();
                PrimeFaces.current().executeScript("PF('manageMedicineDialog').hide()");
                break;
            case ENTRY_ALREADY_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Duplicate", "Medicine code already exists.");
                break;
            case ENTRY_NOT_EXISTS:
                addMessage(FacesMessage.SEVERITY_WARN, "Missing", "Medicine does not exist anymore.");
                break;
            default:
                addMessage(FacesMessage.SEVERITY_ERROR, "Save failed", "Unable to save medicine.");
                break;
        }
        PrimeFaces.current().ajax().update("form:medicineMainPanel", "form:messages", "form:dialogMessages");
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
