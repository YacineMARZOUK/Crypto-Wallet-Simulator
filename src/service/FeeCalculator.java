package service;

import java.math.BigDecimal;

import entity.FeePriority;
import entity.Transaction;

public interface FeeCalculator {
    BigDecimal calculateFees(BigDecimal amount, FeePriority priority);
}
