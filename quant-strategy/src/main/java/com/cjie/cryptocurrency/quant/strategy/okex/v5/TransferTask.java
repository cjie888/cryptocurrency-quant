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

//        transferService.transfer("okexsub2", "MANA", "1", 3.0);
        transferService.transfer("okex", "DOGE", "100", 3.0);
        transferService.transfer("okex", "OKB", "0.5", 3.0);
        transferService.transfer("okex", "AVAX", "0.5", 3.0);
//        transferService.transfer("okex", "BTC", "0.0005", 3.0);
//        transferService.transfer("okexsub1", "ETC", "1", 3.0);
//        transferService.transfer("okexsub1", "ATOM", "1", 3.0);
        transferService.transfer("okex", "XTZ", "10", 3.0);
        transferService.transfer("okex", "IOTA", "30", 3.0);
//        transferService.transfer("okex", "ZEC", "0.2", 3.0);
        transferService.transfer("okex", "FIL", "1", 3.0);
        transferService.transfer("okex", "DOT", "2", 3.0);
        transferService.transfer("okex", "BCH", "0.05", 3.0);

        transferService.transfer("okex", "ADA", "20", 3.0);
        transferService.transfer("okex", "AAVE", "0.1", 3.0);
//        transferService.transfer("okex", "THETA", "5", 3.0);
//        transferService.transfer("okex", "XMR", "0.1", 3.0);
//
        transferService.transfer("okex", "XRP", "20", 3.0);
        transferService.transfer("okex", "LTC", "0.2", 3.0);

        transferService.transfer("okex", "UNI", "2", 3.0);
//        transferService.transfer("okex", "DASH", "0.1", 3.0);
        transferService.transfer("okex", "XLM", "30", 3.0);
//        transferService.transfer("okex", "COMP", "0.05", 3.0);
//
        transferService.transfer("okex", "LINK", "1", 3.0);
//        transferService.transfer("okexsub1", "SUSHI", "1", 3.0);
//
//        transferService.transfer("okex", "ZIL", "100", 3.0);
//        transferService.transfer("okex", "GRT", "10", 3.0);
//        transferService.transfer("okex", "EOS", "2", 3.0);
//
//        transferService.transfer("okexsub1", "BSV", "0.05", 3.0);
//        transferService.transfer("okex", "DYDX", "5", 3.0);
//        transferService.transfer("okex", "FTM", "10", 3.0);
//        transferService.transfer("okex", "OP", "5", 3.0);
//        transferService.transfer("okex", "APT", "1", 3.0);
        transferService.transfer("okex", "BNB", "0.05", 3.0);
        transferService.transfer("okex", "TON", "2", 3.0);

//        transferService.transfer("okexsub2", "MATIC", "10", 3.0);
//        transferService.transfer("okex", "ARB", "20", 3.0);
//        transferService.transfer("okex", "BLUR", "20", 3.0);
        transferService.transfer("okex", "SUI", "10", 3.0);
        transferService.transfer("okex", "ICX", "10", 3.0);

        transferService.transfer("okex", "ONDO", "10", 3.0);

    }

    @Scheduled(cron = "29 55 */1 * * ?")
    public void transfer2() {
        transferService.transfer("okexsub2", "ETH", "0.01", 3.0);
        transferService.transfer("okexsub2", "SOL", "0.1", 3.0);
        transferService.transfer("okexsub2", "SHIB", "400000", 3.0);
        transferService.transfer("okexsub2", "TRX", "40", 3.0);
        transferService.transfer("okexsub2", "HBAR", "50", 3.0);
    }
}
