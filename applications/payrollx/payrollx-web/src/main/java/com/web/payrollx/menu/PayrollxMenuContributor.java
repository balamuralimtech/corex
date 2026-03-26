package com.web.payrollx.menu;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.web.coretix.menu.AppMenuGroup;
import com.web.coretix.menu.AppMenuItem;
import com.web.coretix.menu.MenuContributor;

@ApplicationScoped
public class PayrollxMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup payrollRun = new AppMenuGroup("payrollx_payroll_run", "Payroll Run", "pi pi-fw pi-wallet",
                100, "true")
                .addItem(new AppMenuItem("payrollx_pay_cycle_board", "Pay Cycle Board", "pi pi-fw pi-calendar",
                        "/pages/payrollx/payroll-run/pay-cycle-board.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payrollx_salary_preview", "Salary Preview", "pi pi-fw pi-eye",
                        "/pages/payrollx/payroll-run/salary-preview.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payrollx_payout_register", "Payout Register", "pi pi-fw pi-money-bill",
                        "/pages/payrollx/payroll-run/payout-register.xhtml", 30, "true"));

        AppMenuGroup attendance = new AppMenuGroup("payrollx_attendance", "Attendance", "pi pi-fw pi-clock",
                110, "true")
                .addItem(new AppMenuItem("payrollx_shift_board", "Shift Board", "pi pi-fw pi-calendar-clock",
                        "/pages/payrollx/attendance/shift-board.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payrollx_timesheet_review", "Timesheet Review", "pi pi-fw pi-check",
                        "/pages/payrollx/attendance/timesheet-review.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payrollx_leave_register", "Leave Register", "pi pi-fw pi-briefcase",
                        "/pages/payrollx/attendance/leave-register.xhtml", 30, "true"));

        AppMenuGroup complianceDesk = new AppMenuGroup("payrollx_compliance_desk", "Compliance Desk",
                "pi pi-fw pi-shield", 120, "true")
                .addItem(new AppMenuItem("payrollx_tax_summary", "Tax Summary", "pi pi-fw pi-file",
                        "/pages/payrollx/compliance-desk/tax-summary.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payrollx_statutory_filings", "Statutory Filings", "pi pi-fw pi-upload",
                        "/pages/payrollx/compliance-desk/statutory-filings.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payrollx_audit_trail", "Audit Trail", "pi pi-fw pi-history",
                        "/pages/payrollx/compliance-desk/audit-trail.xhtml", 30, "true"));

        return Arrays.asList(payrollRun, attendance, complianceDesk);
    }
}
