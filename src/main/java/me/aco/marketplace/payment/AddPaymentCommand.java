package me.aco.marketplace.payment;

import java.math.BigDecimal;

public record AddPaymentCommand(
    Long userId, 
    BigDecimal amount
) {
    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
