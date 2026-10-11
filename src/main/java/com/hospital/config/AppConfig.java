package com.hospital.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager that loads database properties from db.properties.
 * Implements lazy, thread-safe configuration loading.
 */
public class AppConfig {

    private static volatile AppConfig instance;
    private final Properties properties = new Properties();

    private AppConfig() {
        loadProperties();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                setDefaults();
            }
        } catch (Exception e) {
            setDefaults();
        }
    }

    private void setDefaults() {
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.password", "root");
        properties.setProperty("db.fallback.enabled", "true");
        properties.setProperty("db.fallback.driver", "org.h2.Driver");
        properties.setProperty("db.fallback.url", "jdbc:h2:mem:hospital_management;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        properties.setProperty("db.fallback.user", "sa");
        properties.setProperty("db.fallback.password", "");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public String getDbUser() {
        return getProperty("db.user", "root");
    }

    public String getDbPassword() {
        return getProperty("db.password", "root");
    }

    public boolean isFallbackEnabled() {
        return Boolean.parseBoolean(getProperty("db.fallback.enabled", "true"));
    }

    public String getFallbackDriver() {
        return getProperty("db.fallback.driver", "org.h2.Driver");
    }

    public String getFallbackUrl() {
        return getProperty("db.fallback.url", "jdbc:h2:mem:hospital_management;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
    }

    public String getFallbackUser() {
        return getProperty("db.fallback.user", "sa");
    }

    public String getFallbackPassword() {
        return getProperty("db.fallback.password", "");
    }
}
