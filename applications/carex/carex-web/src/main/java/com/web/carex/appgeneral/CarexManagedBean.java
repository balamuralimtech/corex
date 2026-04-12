package com.web.carex.appgeneral;

import com.module.coretix.systemmanagement.IOrganizationService;
import com.persist.coretix.modal.systemmanagement.Organizations;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CarexManagedBean {

    protected Integer fetchCurrentOrganizationId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        Object organizationId = sessionMap.get("organizationId");
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    public String getCurrentOrganizationName() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return "";
        }

        Object organizationName = facesContext.getExternalContext().getSessionMap().get("organizationName");
        return organizationName instanceof String ? (String) organizationName : "";
    }

    public boolean isApplicationAdmin() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return false;
        }

        Object userType = facesContext.getExternalContext().getSessionMap().get("userType");
        return userType instanceof String && "APPLICATION_ADMIN".equalsIgnoreCase(((String) userType).trim());
    }

    protected Integer resolveAccessibleOrganizationId(Integer requestedOrganizationId) {
        if (isApplicationAdmin()) {
            return requestedOrganizationId;
        }

        Integer currentOrganizationId = fetchCurrentOrganizationId();
        if (currentOrganizationId == null) {
            return null;
        }

        if (requestedOrganizationId == null || !currentOrganizationId.equals(requestedOrganizationId)) {
            return currentOrganizationId;
        }
        return requestedOrganizationId;
    }

    protected List<Organizations> getAccessibleOrganizations(IOrganizationService organizationService) {
        List<Organizations> organizations = new ArrayList<>();
        if (organizationService == null) {
            return organizations;
        }

        if (isApplicationAdmin()) {
            organizations.addAll(organizationService.getOrganizationsList());
            return organizations;
        }

        Integer currentOrganizationId = fetchCurrentOrganizationId();
        if (currentOrganizationId == null) {
            return organizations;
        }

        Organizations organization = organizationService.getOrganizationById(currentOrganizationId);
        if (organization != null) {
            organizations.add(organization);
        }
        return organizations;
    }

    protected Integer resolveDefaultOrganizationId(List<Organizations> organizations, Integer requestedOrganizationId) {
        if (isApplicationAdmin()) {
            return requestedOrganizationId;
        }

        return resolveAccessibleOrganizationId(requestedOrganizationId);
    }
}
