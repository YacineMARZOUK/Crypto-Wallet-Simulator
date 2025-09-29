package repository;

import entity.Wallet;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface WalletRepository {
    void save(Wallet wallet) throws SQLException;
    void updateBalance(UUID walletId, BigDecimal newBalance) throws SQLException;
    Optional<Wallet> findByAddress(String address) throws SQLException;
    List<Wallet> findAll() throws SQLException;
}
