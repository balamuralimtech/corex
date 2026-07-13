package com.coretix.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class ManifestLoader {
    private ManifestLoader() {
    }

    static List<InstallStep> loadSteps(InstallTarget target) throws InstallerException {
        List<InstallStep> steps = new ArrayList<>();
        if (target.includeCorex()) {
            steps.addAll(loadManifest("CoreX", "db/corex-db"));
        }
        if (target.includeShipx()) {
            steps.addAll(loadManifest("ShipX", "db/applications/shipx/shipx-db"));
        }
        if (target.includeCarex()) {
            steps.addAll(loadManifest("CareX", "db/applications/carex/carex-db"));
        }
        if (target.includePayrollx()) {
            steps.addAll(loadManifest("PayrollX", "db/applications/payrollx/payrollx-db"));
        }
        return steps;
    }

    private static List<InstallStep> loadManifest(String moduleName, String resourceBase) throws InstallerException {
        String manifestPath = resourceBase + "/install-order.txt";
        List<InstallStep> steps = new ArrayList<>();

        try (InputStream inputStream = ManifestLoader.class.getClassLoader().getResourceAsStream(manifestPath)) {
            if (inputStream == null) {
                throw new InstallerException("Manifest resource not found: " + manifestPath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    String resourcePath = resourceBase + "/" + trimmed;
                    String sql = readResource(resourcePath);
                    steps.add(new InstallStep(moduleName, resourcePath, trimmed, sql));
                }
            }
        } catch (IOException e) {
            throw new InstallerException("Failed to read manifest: " + manifestPath, e);
        }

        return steps;
    }

    private static String readResource(String resourcePath) throws InstallerException {
        try (InputStream inputStream = ManifestLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new InstallerException("SQL resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InstallerException("Failed to read SQL resource: " + resourcePath, e);
        }
    }
}
