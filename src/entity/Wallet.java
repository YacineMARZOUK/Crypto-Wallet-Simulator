package com.crypto.model;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Stream;

public class Wallet {
    private UUID id;
    private CryptoType type;
    private String address;
    private BigDecimal balance;
    private String adresse;
    private List<Transaction> transactions;

    public Wallet (CryptoType type, String address){
        this.id = UUID.randomUUID();
        this.type = type;
        this.adresse = address;
        this.balance = BigDecimal.ZERO;
        this.transactions = new ArrayList<>();
    }
    public UUID getId() {
        return id;
    }


    public CryptoType getType() {
        return type;
    }


    public String getAddress() {
        return address;
    }


    public BigDecimal getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction tx) {
        transactions.add(tx);
    }

    public Optional<Transaction> getTransactionById(UUID txId) {
        return transactions.stream()
                .filter(tx -> tx.getId().equals(txId))
                .findFirst();
    }

    public Stream<Transaction> getPendingTransactions() {
        return transactions.stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.PENDING);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}