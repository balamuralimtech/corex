package com.persist.shipx.quotation.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.shipx.quotation.Quotation;

import java.util.List;

public interface IQuotationDAO {

    GeneralConstants addQuotation(Quotation quotation);

    GeneralConstants updateQuotation(Quotation quotation);

    GeneralConstants deleteQuotation(Quotation quotation);

    List<Quotation> getQuotationList();

    Quotation getQuotationById(Integer id);
}
