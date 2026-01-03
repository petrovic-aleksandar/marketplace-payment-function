package me.aco.marketplace.payment.database;

import me.aco.marketplace.payment.PaymentTransfer;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Repository for Transfer database operations.
 * Handles payment_transfers table.
 */
public class TransferRepository {
    
    private final Connection connection;
    
    public TransferRepository(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Save a payment transfer to the database.
     * This inserts into both 'transfers' and 'payment_transfers' tables
     * following the same structure as the Spring app.
     */
    public Long savePaymentTransfer(Long userId, BigDecimal amount) throws SQLException {
        // First insert into transfers table (parent table)
        String insertTransferSql = "INSERT INTO transfers (amount, transfer_type) VALUES (?, 'PAYMENT') RETURNING id";
        
        Long transferId;
        try (PreparedStatement stmt = connection.prepareStatement(insertTransferSql)) {
            stmt.setBigDecimal(1, amount);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    transferId = rs.getLong("id");
                } else {
                    throw new SQLException("Failed to create transfer");
                }
            }
        }
        
        // Then insert into payment_transfers table (child table)
        String insertPaymentSql = "INSERT INTO payment_transfers (id, user_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(insertPaymentSql)) {
            stmt.setLong(1, transferId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
        
        return transferId;
    }
    
    /**
     * Get a payment transfer by ID.
     */
    public PaymentTransfer findById(Long transferId) throws SQLException {
        String sql = """
            SELECT t.id, t.amount, t.created_at, pt.user_id
            FROM transfers t
            JOIN payment_transfers pt ON t.id = pt.id
            WHERE t.id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, transferId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PaymentTransfer transfer = new PaymentTransfer();
                    transfer.setId(rs.getLong("id"));
                    transfer.setAmount(rs.getBigDecimal("amount"));
                    transfer.setUserId(rs.getLong("user_id"));
                    return transfer;
                }
            }
        }
        
        return null;
    }
}
