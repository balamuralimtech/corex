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

import com.module.coretix.usermanagement.IUserActivityService;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.web.coretix.general.NotificationService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErrorLogMonitorSupport {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogMonitorSupport.class);
    private static final Object SCAN_LOCK = new Object();
    private static final long REFRESH_INTERVAL_MS = 30_000L;
    private static final long BACKGROUND_SCAN_INTERVAL_SECONDS = 180L;
    private static final long ACTIVITY_MATCH_WINDOW_MS = 10 * 60 * 1000L;
    private static final int MAX_FILES_TO_SCAN = 80;
    private static final int MAX_INCIDENTS = 150;
    private static final int MAX_INCIDENTS_PER_FILE = 35;
    private static final int MAX_TRACKED_SIGNATURES = 2_000;
    private static final String SOURCE_SERVER = "SERVER";
    private static final String SOURCE_APPLICATION = "APPLICATION";
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter LOG_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS", Locale.ENGLISH);
    private static final DateTimeFormatter BRACKET_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS", Locale.ENGLISH);
    private static final Set<String> NOTIFIED_SIGNATURES =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private static volatile ErrorLogSnapshot latestSnapshot = ErrorLogSnapshot.empty();
    private static volatile long lastScanAt;
    private static volatile ScheduledExecutorService scheduler;
    private static volatile boolean schedulerRunning;

    private ErrorLogMonitorSupport() {
    }

    public static void startBackgroundMonitoring(IUserActivityService userActivityService, String logsLocation) {
        if (userActivityService == null || logsLocation == null || logsLocation.trim().isEmpty()) {
            logger.warn("Skipping error log background monitoring because dependencies are incomplete.");
            return;
        }

        synchronized (SCAN_LOCK) {
            if (schedulerRunning && scheduler != null && !scheduler.isShutdown()) {
                return;
            }

            scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "corex-error-log-monitor");
                thread.setDaemon(true);
                return thread;
            });

            Runnable scanTask = () -> {
                try {
                    getSnapshot(userActivityService, logsLocation, true, true);
                } catch (Exception exception) {
                    logger.error("Background error log monitor scan failed.", exception);
                }
            };

            scheduler.scheduleAtFixedRate(scanTask, 5L, BACKGROUND_SCAN_INTERVAL_SECONDS, TimeUnit.SECONDS);
            schedulerRunning = true;
            logger.info("Error log background monitoring started. Scan interval: {} seconds",
                    BACKGROUND_SCAN_INTERVAL_SECONDS);
        }
    }

    public static void stopBackgroundMonitoring() {
        synchronized (SCAN_LOCK) {
            if (scheduler != null) {
                scheduler.shutdownNow();
                scheduler = null;
            }
            schedulerRunning = false;
            logger.info("Error log background monitoring stopped.");
        }
    }

    public static ErrorLogSnapshot getLatestSnapshot() {
        return latestSnapshot == null ? ErrorLogSnapshot.empty() : latestSnapshot;
    }

    public static ErrorLogSnapshot getSnapshot(IUserActivityService userActivityService, String logsLocation,
                                               boolean forceRefresh, boolean notifyAdmins) {
        long now = System.currentTimeMillis();
        synchronized (SCAN_LOCK) {
            if (!forceRefresh && latestSnapshot != null && (now - lastScanAt) < REFRESH_INTERVAL_MS) {
                return latestSnapshot;
            }

            ErrorLogSnapshot snapshot = scanLogs(userActivityService, logsLocation, now);
            latestSnapshot = snapshot;
            lastScanAt = now;

            if (notifyAdmins) {
                notifyApplicationAdmins(snapshot);
            }

            return snapshot;
        }
    }

    private static ErrorLogSnapshot scanLogs(IUserActivityService userActivityService, String logsLocation, long scannedAt) {
        List<ErrorIncident> incidents = new ArrayList<>();
        List<File> logFiles = collectLogFiles(logsLocation);
        List<UserActivities> activities = loadRecentActivities(userActivityService);
        Set<String> impactedUsers = new HashSet<>();
        Set<String> impactedFiles = new HashSet<>();

        for (File logFile : logFiles) {
            parseFile(logFile, activities, incidents);
        }

        incidents.sort(Comparator.comparing(ErrorIncident::getDetectedAtSortable).reversed());
        if (incidents.size() > MAX_INCIDENTS) {
            incidents = new ArrayList<>(incidents.subList(0, MAX_INCIDENTS));
        }

        int errorCount = 0;
        int warningCount = 0;
        int exceptionCount = 0;
        int criticalCount = 0;

        for (ErrorIncident incident : incidents) {
            impactedFiles.add(incident.getLogFileName());
            if (incident.getImpactedUserName() != null && !incident.getImpactedUserName().trim().isEmpty()) {
                impactedUsers.add(incident.getImpactedUserName());
            }

            switch (incident.getSeverity()) {
                case "ERROR":
                    errorCount++;
                    break;
                case "WARNING":
                    warningCount++;
                    break;
                case "EXCEPTION":
                    exceptionCount++;
                    break;
                default:
                    break;
            }

            if (incident.isCritical()) {
                criticalCount++;
            }
        }

        return new ErrorLogSnapshot(
                incidents,
                errorCount,
                warningCount,
                exceptionCount,
                criticalCount,
                countBySource(incidents, SOURCE_SERVER),
                countBySource(incidents, SOURCE_APPLICATION),
                impactedUsers.size(),
                impactedFiles.size(),
                scannedAt,
                logFiles.size());
    }

    private static int countBySource(List<ErrorIncident> incidents, String sourceType) {
        if (incidents == null || incidents.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (ErrorIncident incident : incidents) {
            if (incident != null && sourceType.equals(incident.getSourceType())) {
                count++;
            }
        }
        return count;
    }

    private static List<File> collectLogFiles(String logsLocation) {
        if (logsLocation == null || logsLocation.trim().isEmpty()) {
            return Collections.emptyList();
        }

        File root = new File(logsLocation);
        if (!root.exists() || !root.isDirectory()) {
            return Collections.emptyList();
        }

        List<File> files = new ArrayList<>();
        collectLogFilesRecursively(root, files);
        files.sort(Comparator.comparingLong(File::lastModified).reversed());
        if (files.size() > MAX_FILES_TO_SCAN) {
            return new ArrayList<>(files.subList(0, MAX_FILES_TO_SCAN));
        }
        return files;
    }

    private static void collectLogFilesRecursively(File directory, Collection<File> files) {
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                collectLogFilesRecursively(child, files);
            } else if (isSupportedLogFile(child.getName())) {
                files.add(child);
            }
        }
    }

    private static boolean isSupportedLogFile(String fileName) {
        if (fileName == null) {
            return false;
        }

        String normalizedName = fileName.toLowerCase(Locale.ENGLISH);
        return normalizedName.endsWith(".log")
                || normalizedName.endsWith(".out")
                || normalizedName.endsWith(".txt");
    }

    private static List<UserActivities> loadRecentActivities(IUserActivityService userActivityService) {
        if (userActivityService == null) {
            return Collections.emptyList();
        }

        try {
            List<UserActivities> activities = userActivityService.getUserActivitiesList();
            if (activities == null || activities.isEmpty()) {
                return Collections.emptyList();
            }

            activities.sort((left, right) -> {
                Timestamp leftTime = left.getCreatedAt();
                Timestamp rightTime = right.getCreatedAt();
                long leftValue = leftTime == null ? 0L : leftTime.getTime();
                long rightValue = rightTime == null ? 0L : rightTime.getTime();
                return Long.compare(rightValue, leftValue);
            });

            if (activities.size() > 800) {
                return new ArrayList<>(activities.subList(0, 800));
            }
            return activities;
        } catch (Exception exception) {
            logger.warn("Unable to load user activities for error log correlation.", exception);
            return Collections.emptyList();
        }
    }

    private static void parseFile(File logFile, List<UserActivities> activities, List<ErrorIncident> incidents) {
        if (logFile == null || !logFile.exists() || incidents.size() >= MAX_INCIDENTS) {
            return;
        }

        int incidentsForCurrentFile = 0;
        try (BufferedReader reader = Files.newBufferedReader(logFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (incidents.size() >= MAX_INCIDENTS) {
                    return;
                }
                if (incidentsForCurrentFile >= MAX_INCIDENTS_PER_FILE) {
                    return;
                }

                ErrorIncident incident = buildIncident(logFile, line, activities);
                if (incident != null) {
                    incidents.add(incident);
                    incidentsForCurrentFile++;
                }
            }
        } catch (IOException exception) {
            logger.warn("Unable to parse log file {}", logFile.getAbsolutePath(), exception);
        }
    }

    private static ErrorIncident buildIncident(File logFile, String line, List<UserActivities> activities) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String upperCaseLine = line.toUpperCase(Locale.ENGLISH);
        if (!upperCaseLine.contains("ERROR")
                && !upperCaseLine.contains("WARN")
                && !upperCaseLine.contains("WARNING")
                && !line.contains("Exception")
                && !upperCaseLine.contains("SEVERE")) {
            return null;
        }

        String severity = resolveSeverity(line, upperCaseLine);
        LocalDateTime detectedAt = resolveTimestamp(line, logFile.lastModified());
        UserActivities relatedActivity = resolveRelatedActivity(line, detectedAt, activities);
        String message = normalizeMessage(line);
        String signature = buildSignature(logFile.getName(), severity, detectedAt, message);
        String operation = relatedActivity == null ? "Unable to correlate with a user activity"
                : resolveOperationSummary(relatedActivity);

        return new ErrorIncident(
                signature,
                severity,
                detectedAt,
                logFile.getName(),
                resolveSourceType(logFile),
                message,
                relatedActivity == null ? "-" : safeValue(relatedActivity.getUserName()),
                operation,
                relatedActivity == null ? "-" : formatTimestamp(relatedActivity.getCreatedAt()),
                relatedActivity == null ? "-" : safeValue(relatedActivity.getSessionId()),
                relatedActivity == null ? "-" : safeValue(relatedActivity.getIpAddress()));
    }

    private static String resolveSourceType(File logFile) {
        if (logFile == null) {
            return SOURCE_SERVER;
        }

        String normalizedPath = logFile.getAbsolutePath().replace('\\', '/').toLowerCase(Locale.ENGLISH);
        if (normalizedPath.contains("/logs/coretix/")
                || normalizedPath.contains("/logs/carex/")
                || normalizedPath.contains("/application/")
                || normalizedPath.contains("coretix.log")
                || normalizedPath.contains("persist.log")) {
            return SOURCE_APPLICATION;
        }
        return SOURCE_SERVER;
    }

    private static String resolveSeverity(String line, String upperCaseLine) {
        if (line.contains("Exception")) {
            return "EXCEPTION";
        }
        if (upperCaseLine.contains("SEVERE") || upperCaseLine.contains("ERROR")) {
            return "ERROR";
        }
        return "WARNING";
    }

    private static LocalDateTime resolveTimestamp(String line, long lastModified) {
        if (line.length() >= 24) {
            String prefix = line.substring(0, 24);
            try {
                return LocalDateTime.parse(prefix, LOG_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }
        }

        if (line.startsWith("[") && line.length() >= 24) {
            String candidate = line.substring(1, 24);
            try {
                return LocalDateTime.parse(candidate, BRACKET_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }
        }

        return Instant.ofEpochMilli(lastModified).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static UserActivities resolveRelatedActivity(String line, LocalDateTime detectedAt,
                                                         List<UserActivities> activities) {
        if (activities == null || activities.isEmpty()) {
            return null;
        }

        for (UserActivities activity : activities) {
            if (activity.getSessionId() != null && !activity.getSessionId().trim().isEmpty()
                    && line.contains(activity.getSessionId())) {
                return activity;
            }
        }

        for (UserActivities activity : activities) {
            if (activity.getUserName() != null && !activity.getUserName().trim().isEmpty()
                    && line.contains(activity.getUserName())) {
                return activity;
            }
        }

        UserActivities nearestActivity = null;
        long nearestDistance = Long.MAX_VALUE;
        for (UserActivities activity : activities) {
            if (activity.getCreatedAt() == null) {
                continue;
            }

            long distance = Math.abs(Timestamp.valueOf(detectedAt).getTime() - activity.getCreatedAt().getTime());
            if (distance <= ACTIVITY_MATCH_WINDOW_MS && distance < nearestDistance) {
                nearestActivity = activity;
                nearestDistance = distance;
            }
        }

        return nearestActivity;
    }

    private static String resolveOperationSummary(UserActivities activity) {
        String activityType = safeValue(activity.getActivityType());
        String description = safeValue(activity.getActivityDescription());
        if ("-".equals(description)) {
            return activityType;
        }
        if ("-".equals(activityType)) {
            return description;
        }
        return activityType + " - " + description;
    }

    private static String normalizeMessage(String line) {
        String sanitized = line.replace('\t', ' ').replaceAll("\\s+", " ").trim();
        if (sanitized.length() > 220) {
            return sanitized.substring(0, 217) + "...";
        }
        return sanitized;
    }

    private static String buildSignature(String fileName, String severity, LocalDateTime detectedAt, String message) {
        return fileName + "|" + severity + "|" + detectedAt + "|" + message;
    }

    private static String safeValue(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static void notifyApplicationAdmins(ErrorLogSnapshot snapshot) {
        if (snapshot == null || snapshot.getIncidents().isEmpty()) {
            return;
        }

        List<ErrorIncident> newlyDetectedIncidents = new ArrayList<>();
        for (ErrorIncident incident : snapshot.getIncidents()) {
            if (NOTIFIED_SIGNATURES.add(incident.getSignature())) {
                newlyDetectedIncidents.add(incident);
            }
        }

        if (newlyDetectedIncidents.isEmpty()) {
            return;
        }

        if (NOTIFIED_SIGNATURES.size() > MAX_TRACKED_SIGNATURES) {
            NOTIFIED_SIGNATURES.clear();
            for (ErrorIncident incident : snapshot.getIncidents()) {
                NOTIFIED_SIGNATURES.add(incident.getSignature());
            }
        }

        ErrorIncident latestIncident = newlyDetectedIncidents.get(0);
        String message;
        if (newlyDetectedIncidents.size() == 1) {
            message = "Log monitor detected a new " + latestIncident.getSeverityLabel().toLowerCase(Locale.ENGLISH)
                    + " in " + latestIncident.getLogFileName() + " at " + latestIncident.getDetectedAtLabel()
                    + ". Review Error Log Monitoring.";
        } else {
            message = "Log monitor detected " + newlyDetectedIncidents.size()
                    + " new warning/error events. Review Error Log Monitoring.";
        }

        NotificationService.sendGrowlMessageToApplicationAdmins(message);
    }

    private static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return timestamp.toLocalDateTime().format(DISPLAY_DATE_FORMAT);
    }

    public static final class ErrorLogSnapshot implements Serializable {
        private final List<ErrorIncident> incidents;
        private final int errorCount;
        private final int warningCount;
        private final int exceptionCount;
        private final int criticalCount;
        private final int serverIncidentCount;
        private final int applicationIncidentCount;
        private final int impactedUserCount;
        private final int impactedLogFileCount;
        private final long scannedAt;
        private final int scannedFileCount;

        ErrorLogSnapshot(List<ErrorIncident> incidents, int errorCount, int warningCount, int exceptionCount,
                         int criticalCount, int serverIncidentCount, int applicationIncidentCount,
                         int impactedUserCount, int impactedLogFileCount,
                         long scannedAt, int scannedFileCount) {
            this.incidents = incidents;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.exceptionCount = exceptionCount;
            this.criticalCount = criticalCount;
            this.serverIncidentCount = serverIncidentCount;
            this.applicationIncidentCount = applicationIncidentCount;
            this.impactedUserCount = impactedUserCount;
            this.impactedLogFileCount = impactedLogFileCount;
            this.scannedAt = scannedAt;
            this.scannedFileCount = scannedFileCount;
        }

        public static ErrorLogSnapshot empty() {
            return new ErrorLogSnapshot(Collections.emptyList(), 0, 0, 0, 0, 0, 0, 0, 0, 0L, 0);
        }

        public List<ErrorIncident> getIncidents() {
            return incidents;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getWarningCount() {
            return warningCount;
        }

        public int getExceptionCount() {
            return exceptionCount;
        }

        public int getCriticalCount() {
            return criticalCount;
        }

        public int getServerIncidentCount() {
            return serverIncidentCount;
        }

        public int getApplicationIncidentCount() {
            return applicationIncidentCount;
        }

        public int getImpactedUserCount() {
            return impactedUserCount;
        }

        public int getImpactedLogFileCount() {
            return impactedLogFileCount;
        }

        public long getScannedAt() {
            return scannedAt;
        }

        public int getScannedFileCount() {
            return scannedFileCount;
        }
    }

    public static final class ErrorIncident implements Serializable {
        private final String signature;
        private final String severity;
        private final LocalDateTime detectedAt;
        private final String logFileName;
        private final String sourceType;
        private final String message;
        private final String impactedUserName;
        private final String operationSummary;
        private final String operationTimeLabel;
        private final String sessionId;
        private final String ipAddress;

        ErrorIncident(String signature, String severity, LocalDateTime detectedAt, String logFileName, String sourceType, String message,
                      String impactedUserName, String operationSummary, String operationTimeLabel,
                      String sessionId, String ipAddress) {
            this.signature = signature;
            this.severity = severity;
            this.detectedAt = detectedAt;
            this.logFileName = logFileName;
            this.sourceType = sourceType;
            this.message = message;
            this.impactedUserName = impactedUserName;
            this.operationSummary = operationSummary;
            this.operationTimeLabel = operationTimeLabel;
            this.sessionId = sessionId;
            this.ipAddress = ipAddress;
        }

        public String getSignature() {
            return signature;
        }

        public String getSeverity() {
            return severity;
        }

        public String getSeverityLabel() {
            if ("ERROR".equals(severity)) {
                return "Error";
            }
            if ("EXCEPTION".equals(severity)) {
                return "Exception";
            }
            return "Warning";
        }

        public boolean isCritical() {
            return "ERROR".equals(severity) || "EXCEPTION".equals(severity);
        }

        public LocalDateTime getDetectedAtSortable() {
            return detectedAt;
        }

        public String getDetectedAtLabel() {
            return detectedAt == null ? "-" : detectedAt.format(DISPLAY_DATE_FORMAT);
        }

        public String getLogFileName() {
            return logFileName;
        }

        public String getSourceType() {
            return sourceType;
        }

        public String getSourceLabel() {
            return SOURCE_APPLICATION.equals(sourceType) ? "Application Log" : "Server Log";
        }

        public String getMessage() {
            return message;
        }

        public String getImpactedUserName() {
            return impactedUserName;
        }

        public String getOperationSummary() {
            return operationSummary;
        }

        public String getOperationTimeLabel() {
            return operationTimeLabel;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getIpAddress() {
            return ipAddress;
        }
    }
}
