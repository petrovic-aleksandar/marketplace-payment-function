package me.aco.marketplace.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResult(
    Long transferId,
    Long userId,
    BigDecimal amount,
    BigDecimal newBalance,
    LocalDateTime timestamp,
    String status
) {
    public static PaymentResult success(Long transferId, Long userId, BigDecimal amount, BigDecimal newBalance) {
        return new PaymentResult(
            transferId,
            userId,
            amount,
            newBalance,
            LocalDateTime.now(),
            "SUCCESS"
        );
    }
    
    public static PaymentResult failure(Long userId, BigDecimal amount, String errorMessage) {
        return new PaymentResult(
            null,
            userId,
            amount,
            null,
            LocalDateTime.now(),
            "FAILED: " + errorMessage
        );
    }
}
