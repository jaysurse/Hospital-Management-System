package com.hospital.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automates schema creation and seed data insertion.
 * Reads the canonical database/hospital_management.sql script and applies it.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initializeIfRequired(Connection conn) {
        if (conn == null) return;
        try {
            if (!tablesExist(conn)) {
                initializeDatabase(conn);
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Auto-check error: " + e.getMessage());
        }
    }

    public static boolean tablesExist(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "PATIENTS", new String[]{"TABLE"})) {
                if (rs.next()) return true;
            }
            try (ResultSet rs = meta.getTables(null, null, "patients", new String[]{"TABLE"})) {
                if (rs.next()) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void initializeDatabase(Connection conn) {
        List<String> sqlStatements = loadSqlStatements();
        if (sqlStatements.isEmpty()) {
            System.err.println("[DatabaseInitializer] No SQL initialization statements found.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) continue;
                // Skip CREATE DATABASE / USE commands if running inside embedded or existing connection
                if (trimmed.toUpperCase().startsWith("CREATE DATABASE") || trimmed.toUpperCase().startsWith("USE ")) {
                    continue;
                }
                try {
                    stmt.execute(trimmed);
                } catch (Exception e) {
                    // Log statement error non-fatally for cross-db compatibility
                    System.err.println("[DatabaseInitializer] Stmt warning (" + e.getMessage() + "): " + trimmed);
                }
            }
            System.out.println("[DatabaseInitializer] Schema and initial seed data applied successfully.");
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Execution failed: " + e.getMessage());
        }
    }

    private static List<String> loadSqlStatements() {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStmt = new StringBuilder();

        try (BufferedReader reader = getSqlReader()) {
            if (reader == null) return statements;

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignore SQL comments
                if (trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                    continue;
                }
                if (trimmed.isEmpty()) {
                    continue;
                }

                currentStmt.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String stmtStr = currentStmt.toString().trim();
                    if (stmtStr.endsWith(";")) {
                        stmtStr = stmtStr.substring(0, stmtStr.length() - 1).trim();
                    }
                    if (!stmtStr.isEmpty()) {
                        statements.add(stmtStr);
                    }
                    currentStmt.setLength(0);
                }
            }
            if (currentStmt.length() > 0) {
                String stmtStr = currentStmt.toString().trim();
                if (!stmtStr.isEmpty()) {
                    statements.add(stmtStr);
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Failed to load SQL script: " + e.getMessage());
        }

        return statements;
    }

    private static BufferedReader getSqlReader() {
        // Try file path first (project root)
        File file = new File("database/hospital_management.sql");
        if (file.exists() && file.isFile()) {
            try {
                return new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }

        // Fallback to classpath resource
        InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("hospital_management.sql");
        if (is != null) {
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        }

        return null;
    }
}
