package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.BankDetails;

import java.util.List;

public interface IBankDetailsDAO {

    public GeneralConstants addBankDetails(BankDetails bankDetails);

    public GeneralConstants updateBankDetails(BankDetails bankDetails);

    public GeneralConstants deleteBankDetails(BankDetails bankDetails);

    public BankDetails getBankDetails(int id);

    public BankDetails getBankDetailsByOrgId(int orgId);

    public List<BankDetails> getBankDetailsList();

    public List<BankDetails> getBankDetailsListByOrgId(int orgId);

}

