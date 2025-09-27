package service;

import entity.Wallet;
import entity.CryptoType;
import entity.Transaction;
import entity.TransactionStatus;
import Repository.DatabaseConnection;
import util.AddressGenerator;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WalletService {

    private final Connection connection;

    public WalletService() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }


    public Wallet createWallet(CryptoType type) throws SQLException {
        String address = generateAddress(type);
        Wallet wallet = new Wallet(type, address);
        saveWalletToDatabase(wallet);
        return wallet;
    }

    private String generateAddress(CryptoType type) {
        return switch (type) {
            case BITCOIN -> AddressGenerator.bitcoinAdress();
            case ETHEREUM -> AddressGenerator.ethereumAddress();
            default -> UUID.randomUUID().toString();
        };
    }

    private void saveWalletToDatabase(Wallet wallet) throws SQLException {
        String sql = "INSERT INTO wallets (id, type, address, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, wallet.getId());
            stmt.setString(2, wallet.getType().name());
            stmt.setString(3, wallet.getAddress());
            stmt.setBigDecimal(4, wallet.getBalance());
            stmt.executeUpdate();
        }
    }


    public void creditWallet(Wallet wallet, BigDecimal amount) throws SQLException {
        wallet.credit(amount);
        updateWalletBalance(wallet);
    }

    public void debitWallet(Wallet wallet, BigDecimal amount) throws SQLException {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour ce débit.");
        }
        wallet.debit(amount);
        updateWalletBalance(wallet);
    }

    private void updateWalletBalance(Wallet wallet) throws SQLException {
        String sql = "UPDATE wallets SET balance = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, wallet.getBalance());
            stmt.setObject(2, wallet.getId());
            stmt.executeUpdate();
        }
    }


    public Optional<Wallet> findWalletByAddress(String address) throws SQLException {
        String sql = "SELECT id, type, address, balance FROM wallets WHERE address = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, address);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Wallet wallet = new Wallet(
                        CryptoType.valueOf(rs.getString("type")),
                        rs.getString("address")
                );
                wallet.credit(rs.getBigDecimal("balance")); // initialise balance
                return Optional.of(wallet);
            }
        }
        return Optional.empty();
    }


    public List<Transaction> getPendingTransactions(Wallet wallet) {
        return wallet.getTransactions()
                .stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.PENDING)
                .collect(Collectors.toList());
    }

    public boolean validateAddress(CryptoType type, String address) {
        Pattern btcPattern = Pattern.compile("^(bc1|1|3)[a-zA-Z0-9]{20,}$");
        Pattern ethPattern = Pattern.compile("^0x[a-fA-F0-9]{40}$");

        return switch (type) {
            case BITCOIN -> btcPattern.matcher(address).matches();
            case ETHEREUM -> ethPattern.matcher(address).matches();
            default -> true;
        };
    }
}
