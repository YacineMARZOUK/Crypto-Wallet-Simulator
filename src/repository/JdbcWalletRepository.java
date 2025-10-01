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
                Wallet wallet = new Wallet(
                        CryptoType.valueOf(rs.getString("type").trim().toUpperCase()),
                        rs.getString("address")
                );
                wallet.credit(rs.getBigDecimal("balance"));
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
                Wallet wallet = new Wallet(
                        CryptoType.valueOf(rs.getString("type")),
                        rs.getString("address")
                );
                wallet.credit(rs.getBigDecimal("balance"));
                wallets.add(wallet);
            }
        }
        return wallets;
    }
}
