package repository;

import entity.CryptoType;
import entity.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void save(Transaction tx) throws SQLException;
    void update(Transaction tx) throws SQLException;
    List<Transaction> findPendingByType(CryptoType type) throws SQLException;
    Optional<Transaction> findById(String id) throws SQLException;
    List<Transaction> findAll() throws SQLException;
}
