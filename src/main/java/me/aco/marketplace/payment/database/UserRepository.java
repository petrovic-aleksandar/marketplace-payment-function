package me.aco.marketplace.payment.database;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Repository for User database operations.
 * Uses JDBC for direct database access.
 */
public class UserRepository {
    
    private final Connection connection;
    
    public UserRepository(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Find user by ID.
     */
    public User findById(Long userId) throws SQLException {
        String sql = "SELECT id, username, name, email, balance, active FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBigDecimal("balance"),
                        rs.getBoolean("active")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Update user balance.
     */
    public void updateBalance(Long userId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setLong(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("User not found: " + userId);
            }
        }
    }
    
    /**
     * Simple User data class for this repository.
     */
    public static class User {
        private final Long id;
        private final String username;
        private final String name;
        private final String email;
        private BigDecimal balance;
        private final boolean active;
        
        public User(Long id, String username, String name, String email, BigDecimal balance, boolean active) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.email = email;
            this.balance = balance;
            this.active = active;
        }
        
        public Long getId() {
            return id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getName() {
            return name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public BigDecimal getBalance() {
            return balance;
        }
        
        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void addBalance(BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            this.balance = this.balance.add(amount);
        }
    }
}
