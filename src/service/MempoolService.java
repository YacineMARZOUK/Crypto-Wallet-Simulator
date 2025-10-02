package service;

import entity.CryptoType;
import entity.FeePriority;
import entity.Mempool;
import entity.Transaction;
import entity.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import repository.JdbcTransactionRepository;
import repository.TransactionRepository;

public class MempoolService {

    private final Mempool mempool;
    private final TransactionRepository txRepo;
    private final Random random = new Random();

    public MempoolService(CryptoType network, Connection conn) {
        this.mempool = new Mempool(network);
        this.txRepo = new JdbcTransactionRepository(conn);

        // AJOUT : Charger les transactions PENDING depuis la DB au démarrage
        loadPendingTransactions();
    }

    // Nouvelle méthode pour charger les transactions PENDING
    private void loadPendingTransactions() {
        try {
            List<Transaction> pendingTxs = txRepo.findPendingByType(mempool.getType());
            for (Transaction tx : pendingTxs) {
                mempool.addTransaction(tx);
            }
            System.out.println("✅ " + pendingTxs.size() + " transaction(s) chargée(s) depuis la base de données");
        } catch (SQLException e) {
            System.err.println("⚠️ Erreur lors du chargement des transactions: " + e.getMessage());
        }
    }

    public Mempool getMempool() {
        return mempool;
    }

    // Générer N transactions aléatoires et les ajouter au mempool + DB
    public void generateRandomTransactions(int count) throws SQLException {
        List<Transaction> txs = IntStream.range(0, count)
                .mapToObj(i -> randomTransaction())
                .collect(Collectors.toList());

        for (Transaction tx : txs) {
            mempool.addTransaction(tx);
            txRepo.save(tx);
        }
    }

    private Transaction randomTransaction() {
        CryptoType type = mempool.getPendingTxs().isEmpty() ? CryptoType.BITCOIN : mempool.getPendingTxs().get(0).getType();
        String src = type == CryptoType.BITCOIN ? util.AddressGenerator.bitcoinAdress() : util.AddressGenerator.ethereumAddress();
        String dest = type == CryptoType.BITCOIN ? util.AddressGenerator.bitcoinAdress() : util.AddressGenerator.ethereumAddress();

        BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 0.5 + 0.01);
        FeePriority priority = FeePriority.values()[random.nextInt(FeePriority.values().length)];

        BigDecimal fees;
        if (type == CryptoType.BITCOIN) {
            fees = new BitcoinFeeCalculator().calculateFees(amount, priority);
        } else {
            fees = new EthereumFeeCalculator().calculateFees(amount, priority);
        }

        return new Transaction(src, dest, amount, fees, priority, TransactionStatus.PENDING, LocalDateTime.now(), type);
    }

    public String getPositionInfo(String txId) {
        List<Transaction> pending = mempool.getPendingTxs();

        // Debug : afficher le nombre de transactions dans le mempool
        System.out.println("🔍 Nombre de transactions dans le mempool: " + pending.size());

        int pos = -1;
        for (int i = 0; i < pending.size(); i++) {
            if (pending.get(i).getId().toString().equals(txId)) {
                pos = i + 1;
                break;
            }
        }

        if (pos == -1) {
            return "Transaction introuvable dans le mempool (peut-être déjà confirmée ou ID incorrect)";
        }

        long estimatedMinutes = pos * 10L;
        return String.format("Votre transaction est en position %d sur %d — Temps estimé: %d min",
                pos, pending.size(), estimatedMinutes);
    }

    public void processBlock(int blockSize) throws SQLException {
        List<Transaction> pending = mempool.getPendingTxs();
        int count = Math.min(blockSize, pending.size());
        List<Transaction> toConfirm = pending.subList(0, count);

        for (Transaction tx : toConfirm) {
            tx.setStatus(TransactionStatus.CONFIRMED);
            txRepo.update(tx);
        }

        mempool.getPendingTxs().removeAll(toConfirm);
    }
}