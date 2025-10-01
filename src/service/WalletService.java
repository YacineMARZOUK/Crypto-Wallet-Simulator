package service;

import entity.Wallet;
import entity.CryptoType;
import entity.Transaction;
import entity.TransactionStatus;
import repository.WalletRepository;
import util.AddressGenerator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }


    public Wallet createWallet(CryptoType type) throws SQLException {
        String address = generateAddress(type);
        Wallet wallet = new Wallet(type, address);
        walletRepository.save(wallet);
        return wallet;
    }

    public void creditWallet(Wallet wallet, BigDecimal amount) throws SQLException {
        wallet.credit(amount);
        walletRepository.updateBalance(wallet.getId(), wallet.getBalance());
    }

    public void debitWallet(Wallet wallet, BigDecimal amount) throws SQLException {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour ce débit.");
        }
        wallet.debit(amount);
        walletRepository.updateBalance(wallet.getId(), wallet.getBalance());
    }

    public Optional<Wallet> findWalletByAddress(String address) throws SQLException {
        return walletRepository.findByAddress(address);
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


    private String generateAddress(CryptoType type) {
        return switch (type) {
            case BITCOIN -> AddressGenerator.bitcoinAdress();
            case ETHEREUM -> AddressGenerator.ethereumAddress();
            default -> UUID.randomUUID().toString();
        };
    }
}