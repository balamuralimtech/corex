package com.coretix.installer;

public final class DbInstallConfig {
    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final char[] password;
    private final InstallTarget target;

    public DbInstallConfig(String host, int port, String databaseName, String username, char[] password, InstallTarget target) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.target = target;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public InstallTarget getTarget() {
        return target;
    }
}
