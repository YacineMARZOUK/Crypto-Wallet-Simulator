package repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.CryptoType;
import entity.FeePriority;
import entity.Transaction;
import entity.TransactionStatus;

public class JdbcTransactionRepository implements TransactionRepository {
    private Connection conn;
    private static final Logger logger = Logger.getLogger(JdbcTransactionRepository.class.getName());

    public JdbcTransactionRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Transaction tx) throws SQLException {
        String sql = "INSERT INTO tx (id, type, source_address, dest_address, amount, created_at, priority, fees, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tx.getId());// <-- plus UUID, juste String hash
            ps.setString(2, tx.getType().name());
            ps.setString(3, tx.getSourceAddress());
            ps.setString(4, tx.getDestinationAddress());
            ps.setBigDecimal(5, tx.getAmount());
            ps.setTimestamp(6, Timestamp.valueOf(tx.getCreatedAt())); // conversion LocalDateTime -> Timestamp
            ps.setString(7, tx.getPriority().name());
            ps.setBigDecimal(8, tx.getFees());
            ps.setString(9, tx.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to save transaction", ex);
            throw ex;
        }
    }
    @Override
    public void update(Transaction tx) throws SQLException {
        String sql = "UPDATE tx SET " +
                "type = ?, source_address = ?, dest_address = ?, " +
                "amount = ?, created_at = ?, priority = ?, fees = ?, status = ? " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tx.getType().name());
            ps.setString(2, tx.getSourceAddress());
            ps.setString(3, tx.getDestinationAddress());
            ps.setBigDecimal(4, tx.getAmount());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(tx.getCreatedAt()));
            ps.setString(6, tx.getPriority().name());
            ps.setBigDecimal(7, tx.getFees());
            ps.setString(8, tx.getStatus().name());
            ps.setObject(9, tx.getId());
            ps.executeUpdate();
        }
    }


    @Override
    public List<Transaction> findPendingByType(CryptoType type) throws SQLException {
        String sql = "SELECT * FROM tx WHERE type = ? AND status = 'PENDING'";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = mapRowToTransaction(rs);
                    list.add(tx);
                }
            }
        }
        return list;
    }
    @Override
    public Optional<Transaction> findById(String id) throws SQLException {
        String sql = "SELECT * FROM tx WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTransaction(rs));
                }
            }
        }
        return Optional.empty();
    }




    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        CryptoType type = CryptoType.valueOf(rs.getString("type").trim().toUpperCase());
        String sourceAddress = rs.getString("source_address");
        String destinationAddress = rs.getString("dest_address");
        BigDecimal amount = rs.getBigDecimal("amount");
        BigDecimal fees = rs.getBigDecimal("fees");
        FeePriority priority = FeePriority.valueOf(rs.getString("priority").trim());
        TransactionStatus status = TransactionStatus.valueOf(rs.getString("status"));
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        Transaction tx = new Transaction(sourceAddress, destinationAddress, amount, fees, priority, status, createdAt, type);
        tx.setId(id);
        return tx;
    }


}
