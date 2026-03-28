package com.module.shipx.quotation;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.quotation.Quotation;

import java.util.List;

public interface IQuotationService {

    GeneralConstants addQuotation(UserActivityTO userActivityTO, Quotation quotation);

    GeneralConstants updateQuotation(UserActivityTO userActivityTO, Quotation quotation);

    GeneralConstants deleteQuotation(UserActivityTO userActivityTO, Quotation quotation);

    GeneralConstants sendQuotationEmail(UserActivityTO userActivityTO, Integer organizationId, Quotation quotation);

    List<Quotation> getQuotationList();

    Quotation getQuotationById(Integer id);
}
