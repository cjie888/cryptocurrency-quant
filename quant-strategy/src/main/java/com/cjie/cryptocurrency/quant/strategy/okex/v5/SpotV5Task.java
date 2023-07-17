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

    @Scheduled(cron = "3/29 * * * * ?")
    public  void netGrid() {

        spotService.netGrid("okexsub2", "MANA-USDT", "1", 0.03);
//        spotService.netGrid("okexsub1", "ETH-USDT", "0.05000000", 0.03);
//        spotService.netGrid("okexsub1", "BCH-USDT", "0.04000000", 0.03);
//        spotService.netGrid("okexsub1", "LTC-USDT", "0.1", 0.03);
//        spotService.netGrid("okexsub1", "XRP-USDT", "20", 0.03);
//        spotService.netGrid("okexsub1", "ETC-USDT", "1", 0.03);
//        spotService.netGrid("okexsub1", "BSV-USDT", "0.05000000", 0.03);

        try {
            Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        }
        spotService.netGrid("okex", "EOS-USDT", "2", 0.03);
//        spotService.netGrid("okex", "OKT-USDT", "1", 0.03);
//        spotService.netGrid("okex", "DOGE-USDT", "1000", 0.03);
//        spotService.netGrid("okex", "UNI-USDT", "1", 0.03);
//        spotService.netGrid("okex", "XLM-USDT", "50", 0.03);
//        spotService.netGrid("okex", "DYDX-USDT", "5", 0.03);


        try {
            Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        spotService.netGrid("okex", "IOTA-USDT", "30", 0.03);
//        spotService.netGrid("okex", "XTZ-USDT", "10", 0.03);
//        spotService.netGrid("okex", "AAVE-USDT", "0.1", 0.03);
//        spotService.netGrid("okex", "AVAX-USDT", "1", 0.03);
//        spotService.netGrid("okex", "ICX-USDT", "10", 0.03);
//        spotService.netGrid("okex", "OP-USDT", "5", 0.03);
        try {
            Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        spotService.netGrid("okex", "THETA-USDT", "5", 0.03);
//        spotService.netGrid("okex", "APT-USDT", "1", 0.03);
//        spotService.netGrid("okex", "BNB-USDT", "0.05", 0.03);
//        spotService.netGrid("okex", "TON-USDT", "10", 0.03);
        spotService.netGrid("okex", "FTM-USDT", "10", 0.03);
//        spotService.netGrid("okex", "ARB-USDT", "20", 0.03);



//ltc 6  xrp 3 etc 5
    }


    @Scheduled(cron = "11/23 * * * * ?")
    public  void netGrid2() {
//        spotService.netGrid("okex", "SUI-USDT", "10", 0.03);
//        spotService.netGrid("okexsub1", "SUSHI-USDT", "1", 0.03);
//        spotService.netGrid("okexsub1", "ATOM-USDT", "1", 0.03);
//        spotService.netGrid("okexsub1", "LINK-USDT", "0.50000000", 0.03);
//        spotService.netGrid("okexsub1", "DOT-USDT", "1", 0.03);
        spotService.netGrid("okexsub2", "SOL-USDT", "1", 0.03);
//        spotService.netGrid("okex", "BLUR-USDT", "20", 0.03);


        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        spotService.netGrid("okex", "BTC-USDT", "0.00100000", 0.03);
//        //spotService.netGrid("okex", "OKB-USDT", "10", 0.03);
//        spotService.netGrid("okex", "XMR-USDT", "0.1", 0.03);
//        spotService.netGrid("okex", "ZEC-USDT", "0.2", 0.03);
//        spotService.netGrid("okex", "FIL-USDT", "1", 0.03);
//        spotService.netGrid("okex", "ZIL-USDT", "100", 0.03);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        spotService.netGrid("okex", "ADA-USDT", "20", 0.03);
        spotService.netGrid("okex", "XEM-USDT", "30", 0.03);
//        spotService.netGrid("okex", "DASH-USDT", "0.1", 0.03);
//        spotService.netGrid("okex", "GRT-USDT", "10", 0.03);
//        spotService.netGrid("okex", "COMP-USDT", "0.05", 0.03);
        spotService.netGrid("okexsub2", "MATIC-USDT", "10", 0.03);

    }
}
