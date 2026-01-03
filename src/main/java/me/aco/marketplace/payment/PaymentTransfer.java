package me.aco.marketplace.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simplified PaymentTransfer entity without JPA annotations.
 * This represents the core domain model for payment transfers.
 */
public class PaymentTransfer {
    
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    
    public PaymentTransfer() {
        this.createdAt = LocalDateTime.now();
    }
    
    public PaymentTransfer(Long userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public String toString() {
        return "PaymentTransfer{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                '}';
    }
}
