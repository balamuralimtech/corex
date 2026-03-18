package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.BankDetails;

import java.util.List;

public interface IBankDetailsService {

public GeneralConstants addBankDetails(UserActivityTO userActivityTO, BankDetails bankDetails);

public GeneralConstants updateBankDetails(UserActivityTO userActivityTO,BankDetails bankDetails);

public GeneralConstants deleteBankDetails(UserActivityTO userActivityTO,BankDetails bankDetails);

public BankDetails getBankDetailsById(int id);

public BankDetails getBankDetailsByOrgId(int orgId);

public List<BankDetails> getBankDetailsList();

public List<BankDetails> getBankDetailsListByOrgId(int orgId);

}
