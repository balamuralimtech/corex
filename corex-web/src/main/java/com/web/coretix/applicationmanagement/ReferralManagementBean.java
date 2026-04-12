package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IReferralManagementService;
import com.module.coretix.systemmanagement.IOrganizationService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.OrganizationReferralProfile;
import com.persist.coretix.modal.coretix.ReferralAttribution;
import com.persist.coretix.modal.coretix.ReferrerProfile;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.usermanagement.UserDetails;
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
import java.util.stream.Collectors;

@Named("referralManagementBean")
@Scope("session")
public class ReferralManagementBean implements Serializable {

    @Inject
    private transient IReferralManagementService referralManagementService;

    @Inject
    private transient IUserAdministrationService userAdministrationService;

    @Inject
    private transient IOrganizationService organizationService;

    private List<ReferrerProfile> referrerProfiles = new ArrayList<>();
    private List<OrganizationReferralProfile> organizationReferralProfiles = new ArrayList<>();
    private List<ReferralAttribution> referralAttributions = new ArrayList<>();

    private ReferrerProfile selectedReferrerProfile = new ReferrerProfile();
    private OrganizationReferralProfile selectedOrganizationReferralProfile = new OrganizationReferralProfile();

    private String selectedUserName;
    private String selectedOrganizationName;
    private String referrerName;
    private String referrerCategory = "INTERNAL_USER";
    private BigDecimal commissionPercentage;
    private String referrerNotes;
    private boolean referrerActive = true;

    private String organizationReferralNotes;
    private boolean organizationReferralActive = true;

    public void initializePageAttributes() {
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().isPostback()) {
            return;
        }
        fetchAll();
        resetReferrerForm();
        resetOrganizationForm();
    }

    public void saveReferrerProfile() {
        ReferrerProfile referrerProfile = new ReferrerProfile();
        if (selectedReferrerProfile != null && selectedReferrerProfile.getId() > 0) {
            referrerProfile.setId(selectedReferrerProfile.getId());
            referrerProfile.setReferralCode(selectedReferrerProfile.getReferralCode());
        }
        UserDetails userDetails = resolveUser(selectedUserName);
        referrerProfile.setUserDetails(userDetails);
        referrerProfile.setReferrerName(blank(referrerName) ? (userDetails == null ? "" : userDetails.getUserName()) : referrerName.trim());
        referrerProfile.setReferrerCategory(referrerCategory);
        referrerProfile.setCommissionPercentage(isExternalReferrer() ? commissionPercentage : null);
        referrerProfile.setNotes(referrerNotes);
        referrerProfile.setActive(referrerActive);

        GeneralConstants result = referralManagementService.saveReferrerProfile(referrerProfile);
        notifyResult(result, "Referrer profile saved.");
        if (result == GeneralConstants.SUCCESSFUL) {
            fetchAll();
            resetReferrerForm();
        }
    }

    public void saveOrganizationReferralProfile() {
        OrganizationReferralProfile organizationReferralProfile = new OrganizationReferralProfile();
        if (selectedOrganizationReferralProfile != null && selectedOrganizationReferralProfile.getId() > 0) {
            organizationReferralProfile.setId(selectedOrganizationReferralProfile.getId());
            organizationReferralProfile.setReferralCode(selectedOrganizationReferralProfile.getReferralCode());
        }
        organizationReferralProfile.setOrganization(resolveOrganization(selectedOrganizationName));
        organizationReferralProfile.setNotes(organizationReferralNotes);
        organizationReferralProfile.setActive(organizationReferralActive);

        GeneralConstants result = referralManagementService.saveOrganizationReferralProfile(organizationReferralProfile);
        notifyResult(result, "Organization referral saved.");
        if (result == GeneralConstants.SUCCESSFUL) {
            fetchAll();
            resetOrganizationForm();
        }
    }

    public void editReferrerProfile() {
        if (selectedReferrerProfile == null) {
            return;
        }
        selectedUserName = selectedReferrerProfile.getUserDetails() == null ? "" : selectedReferrerProfile.getUserDetails().getUserName();
        referrerName = selectedReferrerProfile.getReferrerName();
        referrerCategory = selectedReferrerProfile.getReferrerCategory();
        commissionPercentage = selectedReferrerProfile.getCommissionPercentage();
        referrerNotes = selectedReferrerProfile.getNotes();
        referrerActive = selectedReferrerProfile.isActive();
    }

    public void editOrganizationReferralProfile() {
        if (selectedOrganizationReferralProfile == null) {
            return;
        }
        selectedOrganizationName = selectedOrganizationReferralProfile.getOrganization() == null
                ? "" : selectedOrganizationReferralProfile.getOrganization().getOrganizationName();
        organizationReferralNotes = selectedOrganizationReferralProfile.getNotes();
        organizationReferralActive = selectedOrganizationReferralProfile.isActive();
    }

    public void deleteReferrerProfile() {
        if (selectedReferrerProfile == null || selectedReferrerProfile.getId() <= 0) {
            return;
        }
        GeneralConstants result = referralManagementService.deleteReferrerProfile(selectedReferrerProfile.getId());
        notifyDeleteResult(result, "Referrer profile removed.");
        if (result == GeneralConstants.SUCCESSFUL) {
            fetchAll();
            resetReferrerForm();
        }
    }

    public void deleteOrganizationReferralProfile() {
        if (selectedOrganizationReferralProfile == null || selectedOrganizationReferralProfile.getId() <= 0) {
            return;
        }
        GeneralConstants result = referralManagementService.deleteOrganizationReferralProfile(selectedOrganizationReferralProfile.getId());
        notifyDeleteResult(result, "Organization referral removed.");
        if (result == GeneralConstants.SUCCESSFUL) {
            fetchAll();
            resetOrganizationForm();
        }
    }

    public List<String> completeUser(String query) {
        String search = query == null ? "" : query.toLowerCase();
        return userAdministrationService.getUserDetailsList().stream()
                .filter(userDetails -> userDetails.getUserName() != null
                        && userDetails.getUserName().toLowerCase().startsWith(search))
                .filter(this::isUserAvailableForSelection)
                .map(UserDetails::getUserName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public List<String> completeOrganization(String query) {
        String search = query == null ? "" : query.toLowerCase();
        return organizationService.getOrganizationsList().stream()
                .filter(organization -> organization.getOrganizationName() != null
                        && organization.getOrganizationName().toLowerCase().startsWith(search))
                .filter(this::isOrganizationAvailableForSelection)
                .map(Organizations::getOrganizationName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private void fetchAll() {
        referrerProfiles = referralManagementService.getReferrerProfileList();
        organizationReferralProfiles = referralManagementService.getOrganizationReferralProfileList();
        referralAttributions = referralManagementService.getReferralAttributions();
    }

    private void resetReferrerForm() {
        selectedReferrerProfile = new ReferrerProfile();
        selectedUserName = "";
        referrerName = "";
        referrerCategory = "GENERAL_USER";
        commissionPercentage = null;
        referrerNotes = "";
        referrerActive = true;
    }

    private void resetOrganizationForm() {
        selectedOrganizationReferralProfile = new OrganizationReferralProfile();
        selectedOrganizationName = "";
        organizationReferralNotes = "";
        organizationReferralActive = true;
    }

    private UserDetails resolveUser(String userName) {
        if (blank(userName)) {
            return null;
        }
        return userAdministrationService.getUserDetailEntityByUserName(userName.trim());
    }

    private Organizations resolveOrganization(String organizationName) {
        if (blank(organizationName)) {
            return null;
        }
        return organizationService.getOrganizationsEntityByOrganizationName(organizationName.trim());
    }

    private void notifyResult(GeneralConstants result, String successMessage) {
        if (result == GeneralConstants.SUCCESSFUL) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(successMessage));
            PrimeFaces.current().ajax().update("form:messages", "form:mainPanel");
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", result == GeneralConstants.ENTRY_ALREADY_EXISTS ? "Referral code already exists." : "Unable to save."));
        PrimeFaces.current().ajax().update("form:messages");
    }

    private void notifyDeleteResult(GeneralConstants result, String successMessage) {
        if (result == GeneralConstants.SUCCESSFUL) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(successMessage));
            PrimeFaces.current().ajax().update("form:messages", "form:mainPanel");
            return;
        }

        String detail;
        if (result == GeneralConstants.ENTRY_IN_USE) {
            detail = "This referral profile is already used in subscription attribution and cannot be deleted.";
        } else if (result == GeneralConstants.ENTRY_NOT_EXISTS) {
            detail = "Referral profile not found.";
        } else {
            detail = "Unable to delete.";
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
        PrimeFaces.current().ajax().update("form:messages");
    }

    private boolean isUserAvailableForSelection(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() <= 0) {
            return false;
        }

        if (selectedReferrerProfile != null
                && selectedReferrerProfile.getUserDetails() != null
                && selectedReferrerProfile.getUserDetails().getUserId() == userDetails.getUserId()) {
            return true;
        }

        return referralManagementService.getReferrerProfileByUserId(userDetails.getUserId()) == null;
    }

    private boolean isOrganizationAvailableForSelection(Organizations organization) {
        if (organization == null || organization.getId() <= 0) {
            return false;
        }

        if (selectedOrganizationReferralProfile != null
                && selectedOrganizationReferralProfile.getOrganization() != null
                && selectedOrganizationReferralProfile.getOrganization().getId() == organization.getId()) {
            return true;
        }

        return referralManagementService.getOrganizationReferralProfileByOrganizationId(organization.getId()) == null;
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public void onReferrerCategoryChange() {
        if (!isExternalReferrer()) {
            commissionPercentage = null;
        }
    }

    public boolean isExternalReferrer() {
        return "EXTERNAL_USER".equalsIgnoreCase(referrerCategory);
    }

    public List<String> getReferrerCategories() {
        return Arrays.asList("APPLICATION_ADMIN", "APP_USER", "GENERAL_USER", "EXTERNAL_USER");
    }

    public List<ReferrerProfile> getReferrerProfiles() {
        return referrerProfiles;
    }

    public List<OrganizationReferralProfile> getOrganizationReferralProfiles() {
        return organizationReferralProfiles;
    }

    public List<ReferralAttribution> getReferralAttributions() {
        return referralAttributions;
    }

    public ReferrerProfile getSelectedReferrerProfile() {
        return selectedReferrerProfile;
    }

    public void setSelectedReferrerProfile(ReferrerProfile selectedReferrerProfile) {
        this.selectedReferrerProfile = selectedReferrerProfile;
    }

    public OrganizationReferralProfile getSelectedOrganizationReferralProfile() {
        return selectedOrganizationReferralProfile;
    }

    public void setSelectedOrganizationReferralProfile(OrganizationReferralProfile selectedOrganizationReferralProfile) {
        this.selectedOrganizationReferralProfile = selectedOrganizationReferralProfile;
    }

    public String getSelectedUserName() {
        return selectedUserName;
    }

    public void setSelectedUserName(String selectedUserName) {
        this.selectedUserName = selectedUserName;
    }

    public String getSelectedOrganizationName() {
        return selectedOrganizationName;
    }

    public void setSelectedOrganizationName(String selectedOrganizationName) {
        this.selectedOrganizationName = selectedOrganizationName;
    }

    public String getReferrerName() {
        return referrerName;
    }

    public void setReferrerName(String referrerName) {
        this.referrerName = referrerName;
    }

    public String getReferrerCategory() {
        return referrerCategory;
    }

    public void setReferrerCategory(String referrerCategory) {
        this.referrerCategory = referrerCategory;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getReferrerNotes() {
        return referrerNotes;
    }

    public void setReferrerNotes(String referrerNotes) {
        this.referrerNotes = referrerNotes;
    }

    public boolean isReferrerActive() {
        return referrerActive;
    }

    public void setReferrerActive(boolean referrerActive) {
        this.referrerActive = referrerActive;
    }

    public String getOrganizationReferralNotes() {
        return organizationReferralNotes;
    }

    public void setOrganizationReferralNotes(String organizationReferralNotes) {
        this.organizationReferralNotes = organizationReferralNotes;
    }

    public boolean isOrganizationReferralActive() {
        return organizationReferralActive;
    }

    public void setOrganizationReferralActive(boolean organizationReferralActive) {
        this.organizationReferralActive = organizationReferralActive;
    }
}
