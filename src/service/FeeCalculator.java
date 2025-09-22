package service;

import java.math.BigDecimal;
import com.crypto.model.Transaction;

public interface FeeCalculator {
    BigDecimal calculateFees(Transaction tx);
}
