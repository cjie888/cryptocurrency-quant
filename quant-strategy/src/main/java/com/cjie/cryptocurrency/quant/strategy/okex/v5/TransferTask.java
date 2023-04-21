package com.cjie.cryptocurrency.quant.strategy.okex.v5;


import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferTask {


    @Autowired
    private TransferService transferService;

    @Scheduled(cron = "29 50 */1 * * ?")
    public void transfer() {

        transferService.transfer("okexsub2", "MANA", "1", 3.0);
        transferService.transfer("okex", "DOGE", "1000", 3.0);
        transferService.transfer("okex", "OKB", "10", 3.0);
        transferService.transfer("okexsub1", "ETH", "0.05", 3.0);
        transferService.transfer("okex", "AVAX", "1", 3.0);
        transferService.transfer("okex", "BTC", "0.001", 3.0);
        transferService.transfer("okexsub1", "ETC", "1", 3.0);
        transferService.transfer("okexsub1", "ATOM", "1", 3.0);
        transferService.transfer("okex", "XTZ", "10", 3.0);
        transferService.transfer("okex", "IOTA", "30", 3.0);
        transferService.transfer("okex", "ZEC", "0.2", 3.0);
        transferService.transfer("okex", "FIL", "1", 3.0);
        transferService.transfer("okexsub1", "DOT", "1", 3.0);
        transferService.transfer("okexsub1", "BCH", "0.04", 3.0);

        transferService.transfer("okex", "ADA", "20", 3.0);
        transferService.transfer("okex", "AAVE", "0.1", 3.0);
        transferService.transfer("okex", "THETA", "5", 3.0);
        transferService.transfer("okex", "XMR", "0.1", 3.0);

        transferService.transfer("okexsub1", "XRP", "20", 3.0);
        transferService.transfer("okexsub1", "LTC", "0.1", 3.0);

        transferService.transfer("okex", "UNI", "1", 3.0);
        transferService.transfer("okex", "DASH", "0.1", 3.0);
        transferService.transfer("okex", "XLM", "50", 3.0);
        transferService.transfer("okex", "COMP", "0.05", 3.0);

        transferService.transfer("okexsub1", "LINK", "0.5", 3.0);
        transferService.transfer("okexsub1", "SUSHI", "1", 3.0);

        transferService.transfer("okex", "ZIL", "100", 3.0);
        transferService.transfer("okex", "GRT", "10", 3.0);
        transferService.transfer("okex", "EOS", "2", 3.0);
        transferService.transfer("okex", "XEM", "30", 3.0);

        transferService.transfer("okexsub1", "BSV", "0.05", 3.0);
        transferService.transfer("okex", "DYDX", "5", 3.0);
        transferService.transfer("okex", "FTM", "10", 3.0);
        transferService.transfer("okex", "OP", "5", 3.0);
        transferService.transfer("okex", "APT", "1", 3.0);
        transferService.transfer("okex", "BNB", "0.05", 3.0);
        transferService.transfer("okex", "TON", "10", 3.0);


        transferService.transfer("okexsub2", "SOL", "1", 3.0);
        transferService.transfer("okexsub2", "MATIC", "10", 3.0);


    }
}
