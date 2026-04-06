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
package com.web.coretix.applicationserverlogsanddb;

import com.web.coretix.appgeneral.GenericManagedBean;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;

@Named("errorLogMonitorBean")
@Scope("session")
public class ErrorLogMonitorBean extends GenericManagedBean implements Serializable {

    private static final long serialVersionUID = 8219680256328584138L;
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private ErrorLogMonitorSupport.ErrorLogSnapshot snapshot = ErrorLogMonitorSupport.ErrorLogSnapshot.empty();

    public void initializePageAttributes() {
        refreshView();
    }

    public void refreshView() {
        snapshot = ErrorLogMonitorSupport.getLatestSnapshot();
    }

    public void autoRefreshMonitor() {
        refreshView();
    }

    public List<ErrorLogMonitorSupport.ErrorIncident> getIncidents() {
        return snapshot == null ? Collections.emptyList() : snapshot.getIncidents();
    }

    public int getIncidentCount() {
        return getIncidents().size();
    }

    public int getErrorCount() {
        return snapshot == null ? 0 : snapshot.getErrorCount();
    }

    public int getWarningCount() {
        return snapshot == null ? 0 : snapshot.getWarningCount();
    }

    public int getExceptionCount() {
        return snapshot == null ? 0 : snapshot.getExceptionCount();
    }

    public int getCriticalCount() {
        return snapshot == null ? 0 : snapshot.getCriticalCount();
    }

    public int getImpactedUserCount() {
        return snapshot == null ? 0 : snapshot.getImpactedUserCount();
    }

    public int getImpactedLogFileCount() {
        return snapshot == null ? 0 : snapshot.getImpactedLogFileCount();
    }

    public int getScannedFileCount() {
        return snapshot == null ? 0 : snapshot.getScannedFileCount();
    }

    public boolean isIncidentsAvailable() {
        return !getIncidents().isEmpty();
    }

    public String getLastScanLabel() {
        if (snapshot == null || snapshot.getScannedAt() <= 0L) {
            return "Not scanned yet";
        }

        return Instant.ofEpochMilli(snapshot.getScannedAt())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DISPLAY_DATE_FORMAT);
    }

    public String getLeadSummary() {
        if (!isIncidentsAvailable()) {
            return "No warning, error, or exception signatures were detected in the monitored server log set.";
        }

        ErrorLogMonitorSupport.ErrorIncident incident = getIncidents().get(0);
        return incident.getSeverityLabel() + " detected in " + incident.getLogFileName()
                + " at " + incident.getDetectedAtLabel() + ".";
    }

    public String getLeadUserOperation() {
        if (!isIncidentsAvailable()) {
            return "The monitor will attach the nearest user operation when a correlated activity is available.";
        }

        ErrorLogMonitorSupport.ErrorIncident incident = getIncidents().get(0);
        return incident.getImpactedUserName() + " | " + incident.getOperationSummary();
    }

    public String severityTone(ErrorLogMonitorSupport.ErrorIncident incident) {
        if (incident == null) {
            return "info";
        }
        if ("ERROR".equals(incident.getSeverity()) || "EXCEPTION".equals(incident.getSeverity())) {
            return "danger";
        }
        return "warning";
    }

    public String severityCardStyle(ErrorLogMonitorSupport.ErrorIncident incident) {
        if (incident == null) {
            return "";
        }
        if ("ERROR".equals(incident.getSeverity()) || "EXCEPTION".equals(incident.getSeverity())) {
            return "monitor-pulse";
        }
        return "";
    }
}
