/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.web.coretix.applicationmanagement;

import com.module.coretix.coretix.IDemoRequestService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.DemoRequest;
import com.web.coretix.constants.SessionAttributes;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpSession;

@Named("demoRequestsBean")
@Scope("session")
public class DemoRequestsBean implements Serializable {

    private List<DemoRequestRow> demoRequests = new ArrayList<>();
    private boolean datatableRendered;
    private int recordsCount;
    private int totalRequests;
    private int completedDemoCount;
    private int pendingDemoCount;
    private int requestsLast7Days;
    private String completionRateLabel = "0%";

    @Inject
    private transient IDemoRequestService demoRequestService;

    public void initializePageAttributes() {
        fetchDemoRequests();
    }

    public void searchButtonAction() {
        fetchDemoRequests();
    }

    public void toggleDemoStatus(DemoRequestRow demoRequestRow) {
        if (demoRequestRow == null) {
            return;
        }

        boolean markAsDone = !demoRequestRow.isDemoDone();
        String actor = resolveCurrentUsername();
        GeneralConstants result = demoRequestService.updateDemoRequestStatus(demoRequestRow.getId(), markAsDone, actor);

        if (result == GeneralConstants.SUCCESSFUL) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            markAsDone ? "Demo marked as done." : "Demo marked as pending."));
            fetchDemoRequests();
            return;
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update demo status."));
    }

    private void fetchDemoRequests() {
        demoRequests = new ArrayList<>();
        datatableRendered = false;
        recordsCount = 0;
        totalRequests = 0;
        completedDemoCount = 0;
        pendingDemoCount = 0;
        requestsLast7Days = 0;
        completionRateLabel = "0%";

        List<DemoRequest> requests = demoRequestService.getRecentDemoRequests(250);
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }

        long sevenDaysAgoMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        for (DemoRequest request : requests) {
            if (request.isDemoDone()) {
                completedDemoCount++;
            } else {
                pendingDemoCount++;
            }
            if (request.getCreatedAt() != null && request.getCreatedAt().getTime() >= sevenDaysAgoMillis) {
                requestsLast7Days++;
            }
            demoRequests.add(new DemoRequestRow(
                    request.getId(),
                    request.getClinicName(),
                    request.getWorkEmail(),
                    request.getNotes(),
                    request.getCreatedAt(),
                    request.isDemoDone(),
                    request.getDemoDoneAt(),
                    request.getDemoDoneBy()
            ));
        }

        totalRequests = demoRequests.size();
        recordsCount = demoRequests.size();
        datatableRendered = !demoRequests.isEmpty();
        if (totalRequests > 0) {
            double completionRate = (completedDemoCount * 100.0) / totalRequests;
            completionRateLabel = String.format("%.1f%%", completionRate);
        }
    }

    private String resolveCurrentUsername() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return "APPLICATION_ADMIN";
        }
        Object session = context.getExternalContext().getSession(false);
        if (!(session instanceof HttpSession)) {
            return "APPLICATION_ADMIN";
        }
        Object userName = ((HttpSession) session).getAttribute(SessionAttributes.USERNAME.getName());
        if (userName instanceof String && !((String) userName).trim().isEmpty()) {
            return ((String) userName).trim();
        }
        return "APPLICATION_ADMIN";
    }

    public List<DemoRequestRow> getDemoRequests() {
        return demoRequests;
    }

    public void setDemoRequests(List<DemoRequestRow> demoRequests) {
        this.demoRequests = demoRequests;
    }

    public boolean isDatatableRendered() {
        return datatableRendered;
    }

    public void setDatatableRendered(boolean datatableRendered) {
        this.datatableRendered = datatableRendered;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getCompletedDemoCount() {
        return completedDemoCount;
    }

    public int getPendingDemoCount() {
        return pendingDemoCount;
    }

    public int getRequestsLast7Days() {
        return requestsLast7Days;
    }

    public String getCompletionRateLabel() {
        return completionRateLabel;
    }

    public static class DemoRequestRow implements Serializable {
        private final int id;
        private final String clinicName;
        private final String workEmail;
        private final String notes;
        private final Timestamp createdAt;
        private final boolean demoDone;
        private final Timestamp demoDoneAt;
        private final String demoDoneBy;

        public DemoRequestRow(int id, String clinicName, String workEmail, String notes, Timestamp createdAt,
                              boolean demoDone, Timestamp demoDoneAt, String demoDoneBy) {
            this.id = id;
            this.clinicName = clinicName;
            this.workEmail = workEmail;
            this.notes = notes;
            this.createdAt = createdAt;
            this.demoDone = demoDone;
            this.demoDoneAt = demoDoneAt;
            this.demoDoneBy = demoDoneBy;
        }

        public int getId() {
            return id;
        }

        public String getClinicName() {
            return clinicName;
        }

        public String getWorkEmail() {
            return workEmail;
        }

        public String getNotes() {
            return notes;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public boolean isDemoDone() {
            return demoDone;
        }

        public Timestamp getDemoDoneAt() {
            return demoDoneAt;
        }

        public String getDemoDoneBy() {
            return demoDoneBy;
        }
    }
}
