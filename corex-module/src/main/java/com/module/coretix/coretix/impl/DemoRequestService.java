package com.module.coretix.coretix.impl;

import com.module.coretix.coretix.IDemoRequestService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.DemoRequest;
import com.persist.coretix.modal.coretix.dao.IDemoRequestDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class DemoRequestService implements IDemoRequestService {

    @Inject
    private IDemoRequestDAO demoRequestDAO;

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants addDemoRequest(DemoRequest demoRequest) {
        return demoRequestDAO.addDemoRequest(demoRequest);
    }

    @Override
    public List<DemoRequest> getRecentDemoRequests(int maxResults) {
        return demoRequestDAO.getRecentDemoRequests(maxResults);
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants updateDemoRequestStatus(int demoRequestId, boolean demoDone, String doneBy) {
        return demoRequestDAO.updateDemoRequestStatus(demoRequestId, demoDone, doneBy);
    }
}
