package entity;

import java.time.Instant;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private UUID id  ;
    private String sourceAddress;
    private String destinationAddress;
    private BigDecimal amount;
    private BigDecimal fees;
    private FeePriority priority;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private CryptoType type;

    public Transaction(String sourceAddress, String destinationAddress, BigDecimal amount, BigDecimal fees, FeePriority priority, TransactionStatus status, LocalDateTime createdAt, CryptoType type) {
        this.id = UUID.randomUUID();
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.amount = amount;
        this.fees = fees;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.type = type;
    }
    private String generateHashedId(String src, String dst, LocalDateTime createdAt) {
        String raw = src + dst + createdAt.toString() + Math.random();
        return Integer.toHexString(raw.hashCode()); // Simple hash (peut être remplacé par SHA-256)
    }

    public UUID getId() {
        return id;
    }
    public String getSourceAddress(){
        return sourceAddress;
    }
    public String getDestinationAddress(){
        return destinationAddress;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public BigDecimal getFees(){
        return fees;
    }
    public FeePriority getPriority(){
        return priority;
    }
    public TransactionStatus getStatus(){
        return status;
    }
    public LocalDateTime  getCreatedAt(){
        return createdAt;
    }
    public CryptoType getType(){
        return type;
    }
    public void setId(UUID  id) {
        this.id = id;
    }
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }
    public void setPriority(FeePriority priority) {
        this.priority = priority;
    }
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void settype(CryptoType type) {
        this.type = type;
    }

    public void markConfirmed() {
        this.status = TransactionStatus.CONFIRMED;
    }



    public void markRejected() {
        this.status = TransactionStatus.REJECTED;
    }




}
