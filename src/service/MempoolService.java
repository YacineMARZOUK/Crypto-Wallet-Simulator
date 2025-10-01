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
            mempool.addTransaction(tx);   // Trie automatiquement par frais
            txRepo.save(tx);              // Sauvegarde dans DB
        }
    }

    private Transaction randomTransaction() {
        CryptoType type = mempool.getPendingTxs().isEmpty() ? CryptoType.BITCOIN : mempool.getPendingTxs().get(0).getType();
        String src = type == CryptoType.BITCOIN ? util.AddressGenerator.bitcoinAdress() : util.AddressGenerator.ethereumAddress();
        String dest = type == CryptoType.BITCOIN ? util.AddressGenerator.bitcoinAdress() : util.AddressGenerator.ethereumAddress();

        BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 0.5 + 0.01);
        FeePriority priority = FeePriority.values()[random.nextInt(FeePriority.values().length)];

        // Calcul des frais selon le type de crypto
        BigDecimal fees;
        if (type == CryptoType.BITCOIN) {
            fees = new BitcoinFeeCalculator().calculateFees(
                    new Transaction(src, dest, amount, BigDecimal.ZERO, priority, TransactionStatus.PENDING, LocalDateTime.now(), type)
            );
        } else {
            fees = new EthereumFeeCalculator().calculateFees(
                    new Transaction(src, dest, amount, BigDecimal.ZERO, priority, TransactionStatus.PENDING, LocalDateTime.now(), type)
            );
        }

        return new Transaction(src, dest, amount, fees, priority, TransactionStatus.PENDING, LocalDateTime.now(), type);
    }

    // Retourne la position et temps estimé dans le mempool
    public String getPositionInfo(String txId) {
        List<Transaction> pending = mempool.getPendingTxs();
        int pos = -1;
        for (int i = 0; i < pending.size(); i++) {
            if (pending.get(i).getId().equals(txId)) {
                pos = i + 1; // position 1-based
                break;
            }
        }
        if (pos == -1) return "Transaction introuvable dans le mempool";
        long estimatedMinutes = pos * 10L; // hypothèse : 1 bloc / 10 min
        return String.format("Votre transaction est en position %d sur %d — Temps estimé: %d min", pos, pending.size(), estimatedMinutes);
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
