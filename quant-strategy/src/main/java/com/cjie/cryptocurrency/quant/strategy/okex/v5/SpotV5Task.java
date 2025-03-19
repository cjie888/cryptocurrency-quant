package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpotV5Task {

    @Autowired
    private SpotV5Service spotService;

    @Scheduled(cron = "3/17 * * * * ?")
    public  void netGrid() {

        //spotService.netGrid("okexsub2", "MANA-USDT", "1", 0.03);
        spotService.netGrid("okexsub2", "ETH-USDT", "0.01000000", 0.03);
        spotService.netGrid("okexsub2", "TRX-USDT", "40", 0.03);
        spotService.netGrid("okexsub2", "SHIB-USDT", "400000", 0.03);

        spotService.netGrid("okex", "BCH-USDT", "0.05", 0.03);
        spotService.netGrid("okex", "LTC-USDT", "0.2", 0.03);
        spotService.netGrid("okex", "XRP-USDT", "20", 0.03);
        spotService.netGrid("okex", "OM-USDT", "2", 0.03);
//        spotService.netGrid("okexsub1", "BSV-USDT", "0.05000000", 0.03);
       //spotService.netGrid("okex", "EOS-USDT", "2", 0.03);
//        spotService.netGrid("okex", "OKT-USDT", "1", 0.03);
        spotService.netGrid("okex", "DOGE-USDT", "100", 0.03);
        spotService.netGrid("okex", "UNI-USDT", "2", 0.03);
        spotService.netGrid("okex", "XLM-USDT", "30", 0.03);
//        spotService.netGrid("okex", "DYDX-USDT", "5", 0.03);
        spotService.netGrid("okex", "AAVE-USDT", "0.1", 0.03);
//        spotService.netGrid("okex", "OP-USDT", "5", 0.03);
//        spotService.netGrid("okex", "THETA-USDT", "5", 0.03);
//        spotService.netGrid("okex", "APT-USDT", "1", 0.03);
        spotService.netGrid("okex", "BNB-USDT", "0.05", 0.03);
        spotService.netGrid("okex", "TON-USDT", "2", 0.03);
//        spotService.netGrid("okex", "FTM-USDT", "10", 0.03);
//        spotService.netGrid("okex", "ARB-USDT", "20", 0.03);
        spotService.netGrid("okexsub2", "HBAR-USDT", "50", 0.03);
//        spotService.netGrid("okexsub2", "LTC-USDT", "0.2", 0.03);

        spotService.netGrid("okex", "APT-USDT", "3", 0.03);



//ltc 6  xrp 3 etc 5
    }


    @Scheduled(cron = "11/13 * * * * ?")
    public  void netGrid2() {
        spotService.netGrid("okex", "SUI-USDT", "10", 0.03);
//        spotService.netGrid("okexsub1", "SUSHI-USDT", "1", 0.03);
//        spotService.netGrid("okexsub1", "ATOM-USDT", "1", 0.03);
        spotService.netGrid("okex", "LINK-USDT", "1", 0.03);
        spotService.netGrid("okex", "DOT-USDT", "2", 0.03);
        spotService.netGrid("okexsub2", "SOL-USDT", "0.1", 0.03);
//        spotService.netGrid("okex", "BLUR-USDT", "20", 0.03);

        spotService.netGrid("okex", "BTC-USDT", "0.00050000", 0.02);
        spotService.netGrid("okex", "OKB-USDT", "0.5", 0.03);
//        spotService.netGrid("okex", "ZEC-USDT", "0.2", 0.03);
        spotService.netGrid("okex", "FIL-USDT", "1", 0.03);
//        spotService.netGrid("okex", "ZIL-USDT", "100", 0.03);
        spotService.netGrid("okex", "ADA-USDT", "20", 0.03);
//        spotService.netGrid("okex", "DASH-USDT", "0.1", 0.03);
//        spotService.netGrid("okex", "GRT-USDT", "10", 0.03);
//        spotService.netGrid("okex", "COMP-USDT", "0.05", 0.03);
        spotService.netGrid("okex", "AVAX-USDT", "0.5", 0.03);
        spotService.netGrid("okex", "ICX-USDT", "10", 0.03);
        spotService.netGrid("okex", "IOTA-USDT", "30", 0.03);
        spotService.netGrid("okex", "XTZ-USDT", "10", 0.03);

    }
}
