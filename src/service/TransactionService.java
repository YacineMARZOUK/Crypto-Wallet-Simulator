package service;

import entity.*;
import repository.TransactionRepository;
import repository.WalletRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MempoolService mempoolService;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              MempoolService mempoolService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.mempoolService = mempoolService;
    }

    public Transaction createTransaction(String sourceAddress, String destinationAddress,
                                         BigDecimal amount, BigDecimal fees,  FeePriority priority,
                                         CryptoType type) throws SQLException {

        Optional<Wallet> sourceWalletOpt = walletRepository.findByAddress(sourceAddress);
        if (sourceWalletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet source introuvable.");
        }
        Wallet sourceWallet = sourceWalletOpt.get();

        if (sourceWallet.getBalance().compareTo(amount.add(fees)) < 0) {
            throw new IllegalArgumentException("Solde insuffisant.");
        }

        sourceWallet.debit(amount.add(fees));
        walletRepository.updateBalance(sourceWallet.getId(), sourceWallet.getBalance());

        Transaction tx = new Transaction(
                sourceAddress,
                destinationAddress,
                amount,
                fees,
                priority,
                TransactionStatus.PENDING,
                LocalDateTime.now(),
                type
        );

        transactionRepository.save(tx);

        mempoolService.getMempool().addTransaction(tx);

        return tx;
    }
}
