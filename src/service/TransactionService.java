package service;

import entity.*;
import repository.TransactionRepository;
import repository.WalletRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
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
                                         BigDecimal amount, BigDecimal fees, FeePriority priority,
                                         CryptoType type) throws SQLException {

        // 1. Vérifier que le wallet source existe
        Optional<Wallet> sourceWalletOpt = walletRepository.findByAddress(sourceAddress);
        if (sourceWalletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet source introuvable.");
        }
        Wallet sourceWallet = sourceWalletOpt.get();

        // 2. Calculer le montant total (montant + frais)
        BigDecimal totalAmount = amount.add(fees);

        System.out.println("\n💰 Vérification du solde:");
        System.out.println("   Solde actuel: " + sourceWallet.getBalance());
        System.out.println("   Montant à envoyer: " + amount);
        System.out.println("   Frais: " + fees);
        System.out.println("   Total requis: " + totalAmount);

        // 3. Vérifier le solde
        if (sourceWallet.getBalance().compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant. Requis: " + totalAmount + ", Disponible: " + sourceWallet.getBalance());
        }

        // 4. DÉBITER le wallet source (montant + frais)
        BigDecimal oldBalance = sourceWallet.getBalance();
        sourceWallet.debit(totalAmount);
        walletRepository.updateBalance(sourceWallet.getId(), sourceWallet.getBalance());

        System.out.println("   ✓ Wallet débité: " + oldBalance + " → " + sourceWallet.getBalance());

        // 5. Créer la transaction
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

        // 6. Sauvegarder en base
        transactionRepository.save(tx);

        // 7. Ajouter au mempool
        mempoolService.getMempool().addTransaction(tx);

        return tx;
    }




    public double findbyHighest() throws SQLException {
        List<Transaction> allTxs = transactionRepository.findAll();
        return allTxs.stream()
                .filter(tx-> tx.getStatus() == TransactionStatus.PENDING)
                .mapToDouble(tx -> tx.getFees().doubleValue()).sum();
    }


}