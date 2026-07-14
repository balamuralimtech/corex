package com.web.payrollx.employee;

import com.web.coretix.usermanagement.userProfileBean;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named("payrollxEmployeeProfileBean")
@Scope("session")
public class PayrollxEmployeeProfileBean extends userProfileBean {

    private static final long serialVersionUID = 1L;
}
