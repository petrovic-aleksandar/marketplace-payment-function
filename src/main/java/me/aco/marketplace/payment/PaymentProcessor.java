package me.aco.marketplace.payment;

import java.sql.Connection;
import java.sql.SQLException;

import me.aco.marketplace.payment.database.DatabaseConfig;
import me.aco.marketplace.payment.database.TransferRepository;
import me.aco.marketplace.payment.database.UserRepository;

/**
 * Core payment processing logic extracted from marketplace-spring-ca.
 * Now integrated with PostgreSQL database using JDBC.
 */
public class PaymentProcessor {
    
    /**
     * Process a payment and return the result.
     * This method contains the core business logic from AddPaymentCommandHandler.
     * 
     * @param command The payment command containing userId and amount
     * @return PaymentResult with the outcome of the payment
     */
    public PaymentResult processPayment(AddPaymentCommand command) {
        Connection conn = null;
        
        try {
            // Validate the command
            command.validate();
            
            // Get database connection
            conn = DatabaseConfig.getDataSource().getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Create repositories
            UserRepository userRepo = new UserRepository(conn);
            TransferRepository transferRepo = new TransferRepository(conn);
            
            // 1. Fetch user from database
            UserRepository.User user = userRepo.findById(command.userId());
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + command.userId());
            }
            
            if (!user.isActive()) {
                throw new IllegalArgumentException("User account is not active");
            }
            
            // 2. Add balance to user
            user.addBalance(command.amount());
            
            // 3. Create PaymentTransfer entity
            Long transferId = transferRepo.savePaymentTransfer(command.userId(), command.amount());
            
            // 4. Update user balance in database
            userRepo.updateBalance(command.userId(), user.getBalance());
            
            // Commit transaction
            conn.commit();
            
            return PaymentResult.success(
                transferId,
                command.userId(),
                command.amount(),
                user.getBalance()
            );
            
        } catch (IllegalArgumentException e) {
            rollback(conn);
            return PaymentResult.failure(
                command.userId(),
                command.amount(),
                e.getMessage()
            );
        } catch (SQLException e) {
            rollback(conn);
            return PaymentResult.failure(
                command.userId(),
                command.amount(),
                "Database error: " + e.getMessage()
            );
        } catch (Exception e) {
            rollback(conn);
            return PaymentResult.failure(
                command.userId(),
                command.amount(),
                "Unexpected error: " + e.getMessage()
            );
        } finally {
            closeConnection(conn);
        }
    }
    
    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                // Log error but don't throw
                System.err.println("Failed to rollback transaction: " + e.getMessage());
            }
        }
    }
    
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Log error but don't throw
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
}
