package service;

import com.crypto.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class MempoolService {
    private final Mempool mempool;
    private final Random random;

    public MempoolService(CryptoType network) {
        this.random = new Random();
        this.mempool = new Mempool(network);

    }

    public Mempool getMempool() {
        return mempool;
    }


}
