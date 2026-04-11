package com.module.coretix.coretix;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.DemoRequest;

import java.util.List;

public interface IDemoRequestService {

    GeneralConstants addDemoRequest(DemoRequest demoRequest);

    List<DemoRequest> getRecentDemoRequests(int maxResults);

    GeneralConstants updateDemoRequestStatus(int demoRequestId, boolean demoDone, String doneBy);
}
