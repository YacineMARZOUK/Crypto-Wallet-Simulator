package com.crypto.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Mempool {
    private CryptoType network;
    private List<Transaction> pendingTxs;

    public List<Transaction> getPendingTxs() {
        return pendingTxs;
    }

    public Mempool(CryptoType network){
        this.network = network;
        this.pendingTxs = new ArrayList<>();
    }

    public void addTransaction(Transaction tx){
        pendingTxs.add(tx);
        sortByFees();
    }

    public void sortByFees(){
        pendingTxs.sort(Comparator.comparing(Transaction::getFees).reversed().thenComparing(Transaction::getCreatedAt));
    }

}
