package service;
import java.math.BigDecimal;

import entity.FeePriority;
import entity.Transaction;

public class EthereumFeeCalculator implements FeeCalculator {
private static final BigDecimal ECONOMIQUE_GAS_PRICE = new BigDecimal("10");
private static final BigDecimal STANDARD_GAS_PRICE = new BigDecimal("20");
private static final BigDecimal FAST_GAS_PRICE = new BigDecimal("40");
private static final BigDecimal GAS_LIMIT = new BigDecimal("21000");

@Override
    public BigDecimal calculateFees (BigDecimal amount, FeePriority priority){
    BigDecimal gasPrice;
    switch (priority){
        case ECONOMIQUE:
            gasPrice = ECONOMIQUE_GAS_PRICE;
            break;
        case STANDARD:
            gasPrice = STANDARD_GAS_PRICE;
            break;
        case RAPIDE:
            gasPrice =  FAST_GAS_PRICE;
            break;
        default:
            gasPrice = STANDARD_GAS_PRICE;
    }
    BigDecimal totalGwei = GAS_LIMIT.multiply(gasPrice);
    return totalGwei.divide(new BigDecimal("1000000000"), 9, BigDecimal.ROUND_HALF_UP);
}

}
