package com.hospital.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.hospital.config.AppConfig;

/**
 * Thread-safe Database Connection Provider implementing the Singleton Pattern.
 * 
 * OOP & Design Patterns Demonstrated:
 * 1. Singleton Pattern: Centralizes JDBC driver registration and connection parameters
 * 2. Connection Factory: Provides active Connection objects to callers with try-with-resources
 * 3. Graceful Fallback: Attempts primary MySQL database; if unreachable, creates an
 *    embedded in-memory MySQL-compatible database with an anchor keep-alive connection
 */
public class DBConnection {

    private static volatile DBConnection instance;
    private String activeUrl;
    private String activeUser;
    private String activePassword;
    private boolean usingFallback = false;
    private String activeDatabaseInfo = "Uninitialized";
    private Connection keepAliveConnection; // Holds in-memory DB in memory for JVM lifetime

    private DBConnection() {
        configureConnection();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    private synchronized void configureConnection() {
        AppConfig config = AppConfig.getInstance();

        // 1. Try Primary MySQL Connection
        try {
            Class.forName(config.getDbDriver());
            Connection testConn = DriverManager.getConnection(
                config.getDbUrl(),
                config.getDbUser(),
                config.getDbPassword()
            );
            // Connected to MySQL successfully
            this.activeUrl = config.getDbUrl();
            this.activeUser = config.getDbUser();
            this.activePassword = config.getDbPassword();
            this.usingFallback = false;
            this.activeDatabaseInfo = "MySQL (" + activeUrl.split("\\?")[0] + ")";

            DatabaseInitializer.initializeIfRequired(testConn);
            testConn.close();
            System.out.println("[DBConnection] Connected to primary MySQL database.");
            return;
        } catch (Exception mysqlEx) {
            System.err.println("[DBConnection] Primary MySQL connection unavailable: " + mysqlEx.getMessage());
        }

        // 2. Fallback to Embedded MySQL-Compatible In-Memory Database
        if (config.isFallbackEnabled()) {
            try {
                Class.forName(config.getFallbackDriver());
                this.activeUrl = config.getFallbackUrl();
                this.activeUser = config.getFallbackUser();
                this.activePassword = config.getFallbackPassword();
                this.usingFallback = true;
                this.activeDatabaseInfo = "Embedded Standalone Database (MySQL Mode)";

                // Keep anchor connection open so in-memory DB persists for JVM session
                this.keepAliveConnection = DriverManager.getConnection(activeUrl, activeUser, activePassword);
                DatabaseInitializer.initializeIfRequired(keepAliveConnection);
                System.out.println("[DBConnection] Initialized Embedded Standalone Database successfully.");
                return;
            } catch (Exception fbEx) {
                System.err.println("[DBConnection] Fallback database initialization failed: " + fbEx.getMessage());
            }
        }

        activeDatabaseInfo = "Disconnected / Connection Failed";
    }

    /**
     * Returns a new JDBC connection for caller use in try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        if (activeUrl == null) {
            configureConnection();
        }
        if (activeUrl == null) {
            throw new SQLException("No active database configuration available.");
        }
        return DriverManager.getConnection(activeUrl, activeUser, activePassword);
    }

    public boolean isUsingFallback() {
        return usingFallback;
    }

    public String getActiveDatabaseInfo() {
        return activeDatabaseInfo;
    }

    public synchronized boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void reconnect() {
        if (keepAliveConnection != null) {
            try {
                keepAliveConnection.close();
            } catch (SQLException ignored) {
            }
            keepAliveConnection = null;
        }
        configureConnection();
    }

    public synchronized void close() {
        if (keepAliveConnection != null) {
            try {
                keepAliveConnection.close();
            } catch (SQLException ignored) {
            }
            keepAliveConnection = null;
        }
    }
}
