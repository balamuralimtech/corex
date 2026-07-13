package com.coretix.installer;

public final class InstallStep {
    private final String moduleName;
    private final String resourcePath;
    private final String displayName;
    private final String sql;

    public InstallStep(String moduleName, String resourcePath, String displayName, String sql) {
        this.moduleName = moduleName;
        this.resourcePath = resourcePath;
        this.displayName = displayName;
        this.sql = sql;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSql() {
        return sql;
    }
}
