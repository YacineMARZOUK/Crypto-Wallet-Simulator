package service;

import java.math.BigDecimal;
import entity.Transaction;

public interface FeeCalculator {
    BigDecimal calculateFees(Transaction tx);
}
