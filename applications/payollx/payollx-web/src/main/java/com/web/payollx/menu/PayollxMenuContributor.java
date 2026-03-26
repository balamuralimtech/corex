package com.web.payollx.menu;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.web.coretix.menu.AppMenuGroup;
import com.web.coretix.menu.AppMenuItem;
import com.web.coretix.menu.MenuContributor;

@ApplicationScoped
public class PayollxMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup payrollRun = new AppMenuGroup("payollx_payroll_run", "Payroll Run", "pi pi-fw pi-wallet",
                100, "true")
                .addItem(new AppMenuItem("payollx_pay_cycle_board", "Pay Cycle Board", "pi pi-fw pi-calendar",
                        "/pages/payollx/payroll-run/pay-cycle-board.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payollx_salary_preview", "Salary Preview", "pi pi-fw pi-eye",
                        "/pages/payollx/payroll-run/salary-preview.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payollx_payout_register", "Payout Register", "pi pi-fw pi-money-bill",
                        "/pages/payollx/payroll-run/payout-register.xhtml", 30, "true"));

        AppMenuGroup attendance = new AppMenuGroup("payollx_attendance", "Attendance", "pi pi-fw pi-clock",
                110, "true")
                .addItem(new AppMenuItem("payollx_shift_board", "Shift Board", "pi pi-fw pi-calendar-clock",
                        "/pages/payollx/attendance/shift-board.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payollx_timesheet_review", "Timesheet Review", "pi pi-fw pi-check",
                        "/pages/payollx/attendance/timesheet-review.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payollx_leave_register", "Leave Register", "pi pi-fw pi-briefcase",
                        "/pages/payollx/attendance/leave-register.xhtml", 30, "true"));

        AppMenuGroup complianceDesk = new AppMenuGroup("payollx_compliance_desk", "Compliance Desk",
                "pi pi-fw pi-shield", 120, "true")
                .addItem(new AppMenuItem("payollx_tax_summary", "Tax Summary", "pi pi-fw pi-file",
                        "/pages/payollx/compliance-desk/tax-summary.xhtml", 10, "true"))
                .addItem(new AppMenuItem("payollx_statutory_filings", "Statutory Filings", "pi pi-fw pi-upload",
                        "/pages/payollx/compliance-desk/statutory-filings.xhtml", 20, "true"))
                .addItem(new AppMenuItem("payollx_audit_trail", "Audit Trail", "pi pi-fw pi-history",
                        "/pages/payollx/compliance-desk/audit-trail.xhtml", 30, "true"));

        return Arrays.asList(payrollRun, attendance, complianceDesk);
    }
}
