package service;

import java.math.BigDecimal;
import entity.Transaction;

public class BitcoinFeeCalculator implements FeeCalculator {
    private static final BigDecimal ECONOMIC_RATE = new BigDecimal("5");
    private static final BigDecimal STANDARD_RATE = new BigDecimal("10");
    private static final BigDecimal FAST_RATE = new BigDecimal("20");

    @Override
    public BigDecimal calculateFees (Transaction tx){
        BigDecimal txSize = new BigDecimal("250");
        BigDecimal rate;
        switch (tx.getPriority()){
            case ECONOMIQUE:
                rate = ECONOMIC_RATE;
                break;
            case STANDARD:
                rate = STANDARD_RATE;
                break;
            case RAPIDE:
                rate = FAST_RATE;
                break;
            default :
                rate = STANDARD_RATE;

        }
        BigDecimal satoshis = txSize.multiply(rate);
        return satoshis.divide(new BigDecimal("100000000"), 8, BigDecimal.ROUND_HALF_UP);

    }


}
