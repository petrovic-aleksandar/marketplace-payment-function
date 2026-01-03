package me.aco.marketplace.payment.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Database configuration for PostgreSQL connection.
 * Uses HikariCP for connection pooling.
 */
public class DatabaseConfig {
    
    private static HikariDataSource dataSource;
    
    /**
     * Get or create the DataSource singleton.
     * Connection details are read from environment variables.
     */
    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            
            // Read from environment variables (set in local.settings.json or Azure config)
            String jdbcUrl = System.getenv("DB_URL");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");
            
            // Default values for local development
            if (jdbcUrl == null) {
                jdbcUrl = "jdbc:postgresql://localhost:5432/marketplace";
            }
            if (username == null) {
                username = "postgres";
            }
            if (password == null) {
                password = "postgres";
            }
            
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            
            // Connection pool settings optimized for serverless
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(600000);
            
            dataSource = new HikariDataSource(config);
        }
        
        return dataSource;
    }
    
    /**
     * Close the datasource (call this on shutdown if needed)
     */
    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
