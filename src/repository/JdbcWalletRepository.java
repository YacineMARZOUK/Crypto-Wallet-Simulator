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
                // CORRECTION : Récupérer l'ID et le définir sur le wallet
                UUID id = (UUID) rs.getObject("id");
                CryptoType type = CryptoType.valueOf(rs.getString("type").trim().toUpperCase());
                String addr = rs.getString("address");
                BigDecimal balance = rs.getBigDecimal("balance");

                Wallet wallet = new Wallet(type, addr);
                wallet.setId(id); // IMPORTANT: Définir l'ID pour pouvoir faire updateBalance
                wallet.setBalance(balance); // Utiliser setBalance au lieu de credit

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