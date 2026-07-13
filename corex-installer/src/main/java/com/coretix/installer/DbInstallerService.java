package com.coretix.installer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class DbInstallerService {
    public void install(DbInstallConfig config, InstallListener listener) throws InstallerException {
        List<InstallStep> steps = ManifestLoader.loadSteps(config.getTarget());
        listener.onStart(steps.size());
        listener.onMessage("Preparing installation for target: " + config.getTarget());

        ensureDatabase(config, listener);

        if (steps.isEmpty()) {
            listener.onMessage("No SQL files were configured for the selected target.");
            listener.onComplete();
            return;
        }

        String jdbcUrl = buildDatabaseJdbcUrl(config);
        listener.onMessage("Connected target database: " + config.getDatabaseName());

        try (Connection connection = DriverManager.getConnection(jdbcUrl, config.getUsername(), new String(config.getPassword()))) {
            connection.setAutoCommit(true);
            for (int index = 0; index < steps.size(); index++) {
                InstallStep step = steps.get(index);
                listener.onStepStarted(index + 1, steps.size(), step);
                listener.onMessage("Starting [" + step.getModuleName() + "] " + step.getDisplayName());
                executeScript(connection, step);
                listener.onProgress(index + 1, steps.size(), step);
                listener.onMessage("Completed [" + step.getModuleName() + "] " + step.getDisplayName());
            }
        } catch (SQLException e) {
            throw new InstallerException("Database installation failed: " + e.getMessage(), e);
        } finally {
            listener.onProgress(steps.size(), steps.size(), null);
        }

        listener.onComplete();
    }

    private void ensureDatabase(DbInstallConfig config, InstallListener listener) throws InstallerException {
        String jdbcUrl = buildServerJdbcUrl(config);
        String databaseName = sanitizeIdentifier(config.getDatabaseName());
        listener.onMessage("Connecting to MySQL server at " + config.getHost() + ":" + config.getPort());

        try (Connection connection = DriverManager.getConnection(jdbcUrl, config.getUsername(), new String(config.getPassword()));
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            listener.onMessage("Database ready: " + databaseName);
        } catch (SQLException e) {
            throw new InstallerException("Unable to create or access database '" + config.getDatabaseName() + "': " + e.getMessage(), e);
        }
    }

    private void executeScript(Connection connection, InstallStep step) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(step.getSql());
        }
    }

    private String buildServerJdbcUrl(DbInstallConfig config) {
        return "jdbc:mysql://" + config.getHost() + ":" + config.getPort()
                + "/?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    }

    private String buildDatabaseJdbcUrl(DbInstallConfig config) {
        return "jdbc:mysql://" + config.getHost() + ":" + config.getPort()
                + "/" + config.getDatabaseName()
                + "?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    }

    private String sanitizeIdentifier(String identifier) throws InstallerException {
        if (!identifier.matches("[A-Za-z0-9_\\-]+")) {
            throw new InstallerException("Database name contains unsupported characters.");
        }
        return identifier;
    }
}
