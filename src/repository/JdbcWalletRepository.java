package repository;

import entity.Wallet;
import entity.CryptoType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class JdbcWalletRepository implements WalletRepository {

    private final Connection connection;

    public JdbcWalletRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Wallet wallet) throws SQLException {
        String sql = "INSERT INTO wallet (id, type, address, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, wallet.getId());
            stmt.setString(2, wallet.getType().name());
            stmt.setString(3, wallet.getAddress());
            stmt.setBigDecimal(4, wallet.getBalance());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateBalance(UUID walletId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE wallet SET balance = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setObject(2, walletId);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Wallet> findByAddress(String address) throws SQLException {
        String sql = "SELECT id, type, address, balance FROM wallet WHERE address = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, address);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID id = (UUID) rs.getObject("id");
                CryptoType type = CryptoType.valueOf(rs.getString("type").trim().toUpperCase());
                String addr = rs.getString("address");
                BigDecimal balance = rs.getBigDecimal("balance");

                Wallet wallet = new Wallet(type, addr);
                wallet.setId(id);
                // CRITIQUE: Définir directement le solde, ne PAS utiliser credit()
                // car credit() AJOUTE au solde existant (qui est 0 par défaut)
                if (wallet.getBalance() == null || wallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                    wallet.setBalance(balance);
                } else {
                    // Si le wallet a déjà un solde (constructor), on le remplace
                    wallet.setBalance(balance);
                }

                return Optional.of(wallet);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Wallet> findAll() throws SQLException {
        String sql = "SELECT id, type, address, balance FROM wallet";
        List<Wallet> wallets = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                UUID id = (UUID) rs.getObject("id");
                CryptoType type = CryptoType.valueOf(rs.getString("type").trim().toUpperCase());
                String address = rs.getString("address");
                BigDecimal balance = rs.getBigDecimal("balance");

                Wallet wallet = new Wallet(type, address);
                wallet.setId(id);
                wallet.setBalance(balance);

                wallets.add(wallet);
            }
        }
        return wallets;
    }
}