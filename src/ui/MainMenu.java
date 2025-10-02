package ui;

import repository.*;
import service.*;
import entity.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MainMenu {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        WalletRepository walletRepo = new JdbcWalletRepository(conn);
        TransactionRepository txRepo = new JdbcTransactionRepository(conn);

        MempoolService mempoolService = new MempoolService(CryptoType.BITCOIN, conn);
        WalletService walletService = new WalletService(walletRepo);
        TransactionService transactionService = new TransactionService(walletRepo, txRepo, mempoolService);

        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1": createWallet(walletService); break;
                    case "2": makeTransaction(transactionService, walletService); break;
                    case "3": viewPositionInMempool(mempoolService); break;
                    case "4": viewMempool(mempoolService); break;
                    case "5": confirmBlock(mempoolService); break;
                    case "0": running = false; break;
                    default: System.out.println("❌ Choix invalide !");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("👋 Au revoir !");
    }

    private static void printMenu() {
        System.out.println("\n===== SIMULATEUR MEMPOOL =====");
        System.out.println("1 - Créer un wallet");
        System.out.println("2 - Créer une transaction");
        System.out.println("3 - Voir position d'une transaction dans le mempool");
        System.out.println("4 - Consulter l'état du mempool");
        System.out.println("5 - Miner un bloc (confirmer transactions)");
        System.out.println("0 - Quitter");
        System.out.print("👉 Votre choix: ");
    }

    private static void createWallet(WalletService walletService) throws SQLException {
        System.out.println("Choisir le type de wallet : 1=BITCOIN, 2=ETHEREUM");
        String choice = scanner.nextLine();

        CryptoType type = choice.equals("2") ? CryptoType.ETHEREUM : CryptoType.BITCOIN;

        Wallet wallet = walletService.createWallet(type);
        System.out.println("✅ Wallet créé !");
        System.out.println("ID: " + wallet.getId());
        System.out.println("Adresse: " + wallet.getAddress());
        System.out.println("Solde: " + wallet.getBalance());
    }

    private static void makeTransaction(TransactionService transactionService, WalletService walletService) throws SQLException {
        System.out.print("Adresse source : ");
        String source = scanner.nextLine();

        // Vérifier que le wallet existe
        Wallet sourceWallet = walletService.findWalletByAddress(source)
                .orElseThrow(() -> new IllegalArgumentException("Wallet source introuvable."));

        System.out.print("Adresse destination : ");
        String destination = scanner.nextLine();

        System.out.print("Montant : ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        System.out.println("Choisir le niveau de frais : 1=ECONOMIQUE, 2=STANDARD, 3=RAPIDE");
        String feeChoice = scanner.nextLine();
        FeePriority priority;
        switch (feeChoice) {
            case "1": priority = FeePriority.ECONOMIQUE; break;
            case "3": priority = FeePriority.RAPIDE; break;
            default: priority = FeePriority.STANDARD;
        }

        BigDecimal fees;
        if (sourceWallet.getType() == CryptoType.BITCOIN) {
            fees = new BitcoinFeeCalculator().calculateFees(amount, priority);
        } else {
            fees = new EthereumFeeCalculator().calculateFees(amount, priority);
        }

        Transaction tx = transactionService.createTransaction(
                source,
                destination,
                amount,
                fees,
                priority,
                sourceWallet.getType()
        );

        System.out.println("✅ Transaction créée avec succès !");
        System.out.println("ID: " + tx.getId());
        System.out.println("Montant: " + tx.getAmount());
        System.out.println("Frais: " + tx.getFees());
        System.out.println("Statut: " + tx.getStatus());
        System.out.println("Priorité: " + tx.getPriority());
        System.out.println("Type de crypto: " + tx.getType());
        System.out.println("\n💡 La transaction est en attente dans le mempool.");
        System.out.println("   Utilisez l'option 5 pour miner un bloc et confirmer les transactions.");
    }

    private static void viewPositionInMempool(MempoolService mempoolService) {
        System.out.println("\n💡 Entrez l'ID UUID de votre transaction");
        System.out.println("Exemple: 1bf4a7f2-5d8a-4f44-8cb7-96c591932268");
        System.out.print("ID de la transaction: ");
        String id = scanner.nextLine().trim();

        String info = mempoolService.getPositionInfo(id);
        System.out.println(info);
    }

    private static void viewMempool(MempoolService mempoolService) {
        List<Transaction> pending = mempoolService.getMempool().getPendingTxs();

        System.out.println("\n=== ÉTAT DU MEMPOOL ===");
        System.out.printf("%-40s | %-10s | %-10s | %-10s%n", "Transaction ID", "Montant", "Frais", "Statut");
        System.out.println("--------------------------------------------------------------------------------");
        for (Transaction tx : pending) {
            System.out.printf("%-40s | %-10s | %-10s | %-10s%n",
                    tx.getId(), tx.getAmount(), tx.getFees(), tx.getStatus());
        }
        System.out.println("\nTotal: " + pending.size() + " transaction(s) en attente");
    }

    private static void confirmBlock(MempoolService mempoolService) throws SQLException {
        List<Transaction> pending = mempoolService.getMempool().getPendingTxs();

        if (pending.isEmpty()) {
            System.out.println("⚠️ Aucune transaction en attente dans le mempool.");
            return;
        }

        System.out.println("Transactions en attente: " + pending.size());
        System.out.print("Taille du bloc (nombre de tx à confirmer) : ");
        int size = Integer.parseInt(scanner.nextLine());

        System.out.println("\n⛏️ Mining en cours...\n");
        mempoolService.processBlock(size);
        System.out.println("\n✅ Bloc miné avec succès !");
    }
}