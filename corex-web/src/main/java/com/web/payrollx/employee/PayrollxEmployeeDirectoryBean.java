package com.web.payrollx.employee;

import com.web.coretix.usermanagement.UserAdministrationBean;
import com.persist.coretix.modal.usermanagement.UserDetails;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named("payrollxEmployeeDirectoryBean")
@Scope("session")
public class PayrollxEmployeeDirectoryBean extends UserAdministrationBean {

    private static final long serialVersionUID = 1L;

    public String getEmployeeScopeViewLabel() {
        return isApplicationAdmin() ? "Application-wide employee view" : "Organization-scoped employee view";
    }

    public String employeeFirstName(UserDetails user) {
        String userName = normalizedUserName(user);
        if (userName.isEmpty()) {
            return "-";
        }
        String[] parts = userName.split("\\s+", 2);
        return parts[0];
    }

    public String employeeLastName(UserDetails user) {
        String userName = normalizedUserName(user);
        if (userName.isEmpty()) {
            return "-";
        }
        String[] parts = userName.split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "-";
    }

    public String employeeDesignation(UserDetails user) {
        return "-";
    }

    public String employeeRole(UserDetails user) {
        return user != null && user.getRole() != null && user.getRole().getRoleName() != null
                ? user.getRole().getRoleName()
                : "-";
    }

    public String employeeDepartment(UserDetails user) {
        return "-";
    }

    public String employeeCompany(UserDetails user) {
        return user != null && user.getOrganization() != null && user.getOrganization().getOrganizationName() != null
                ? user.getOrganization().getOrganizationName()
                : "-";
    }

    public String employeeCountry(UserDetails user) {
        return user != null && user.getCountry() != null && user.getCountry().getName() != null
                ? user.getCountry().getName()
                : "-";
    }

    private String normalizedUserName(UserDetails user) {
        return user == null || user.getUserName() == null ? "" : user.getUserName().trim();
    }
}
