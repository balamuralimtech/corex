package com.web.payrollx.menu;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import com.web.coretix.menu.AppMenuGroup;
import com.web.coretix.menu.AppMenuItem;
import com.web.coretix.menu.MenuContributor;

@Named
public class PayrollxMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup employeeManagement = new AppMenuGroup("payrollx_employee_management", "Employee Management",
                "pi pi-fw pi-users", 100, "true")
                .addItem(new AppMenuItem("payrollx_employee_dashboard", "Dashboard", "pi pi-fw pi-chart-line",
                        "/pages/payrollx/employee-management/dashboard.xhtml", 5, "true"))
                .addItem(new AppMenuItem("payrollx_employee_directory", "Employee Directory", "pi pi-fw pi-list",
                        "/pages/payrollx/employee-management/employee-directory.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payrollx_employee_profile", "Employee Profile", "pi pi-fw pi-id-card",
                        "/pages/payrollx/employee-management/employee-profile.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payrollx_org_structure", "Departments & Roles", "pi pi-fw pi-sitemap",
                        "/pages/payrollx/employee-management/departments-roles.xhtml", 30, "true"));

        AppMenuGroup leaveManagement = new AppMenuGroup("payrollx_leave_management", "Leave Management",
                "pi pi-fw pi-calendar-minus", 110, "true")
                .addItem(new AppMenuItem("payrollx_leave_dashboard", "Dashboard", "pi pi-fw pi-chart-line",
                        "/pages/payrollx/leave-management/dashboard.xhtml", 5, "true"))
                .addItem(new AppMenuItem("payrollx_leave_requests", "Leave Requests", "pi pi-fw pi-send",
                        "/pages/payrollx/leave-management/leave-requests.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payrollx_leave_approvals", "Leave Approvals", "pi pi-fw pi-check-square",
                        "/pages/payrollx/leave-management/leave-approvals.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payrollx_leave_balances", "Leave Balances", "pi pi-fw pi-chart-bar",
                        "/pages/payrollx/leave-management/leave-balances.xhtml", 30, "true"));

        AppMenuGroup payrollManagement = new AppMenuGroup("payrollx_payroll_management", "Payroll Management",
                "pi pi-fw pi-wallet",
                100, "true")
                .addItem(new AppMenuItem("payrollx_payroll_dashboard", "Dashboard", "pi pi-fw pi-chart-line",
                        "/pages/payrollx/payroll-management/dashboard.xhtml", 5, "true"))
                .addItem(new AppMenuItem("payrollx_payroll_runs", "Payroll Runs", "pi pi-fw pi-calendar",
                        "/pages/payrollx/payroll-management/payroll-runs.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payrollx_salary_components", "Salary Components", "pi pi-fw pi-sliders-h",
                        "/pages/payrollx/payroll-management/salary-components.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payrollx_payslips", "Payslips", "pi pi-fw pi-file-pdf",
                        "/pages/payrollx/payroll-management/payslips.xhtml", 30, "true"));

        return Arrays.asList(employeeManagement, leaveManagement, payrollManagement);
    }
}
