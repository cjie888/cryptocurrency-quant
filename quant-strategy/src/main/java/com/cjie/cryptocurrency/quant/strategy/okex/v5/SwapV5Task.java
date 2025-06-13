package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SwapV5Task {

    @Autowired
    private SwapV5Service swapService;


    @Scheduled(cron = "3/11 * * * * ?")
    public  void netGrid() {
        //swapService.netGrid("BTC-USD-SWAP", "1", 80.0, 0.002, 45, 7800);
        //swapService.transfer("BTC-USD-SWAP",  0.002);
        //swapService.netGrid("ETH-USD-SWAP", "1", 1.2,0.1, 119, 150);
       // swapService.transfer("ETH-USD-SWAP",0.1);
//        swapService.netGrid("EOS-USD-SWAP", "2", 0.018, 1.0);
        //swapService.netGrid("BCH-USD-SWAP", "1", 1.5, 0.1, 190, 255);
       // swapService.transfer("BCH-USD-SWAP", 0.1);
        //swapService.netGrid("XRP-USD-SWAP", "1", 0.0016,20.0, 200, 0.1970);
  //      swapService.transfer("XRP-USD-SWAP", 20.0);
//        swapService.netGrid("okexsub2","MANA-USDT-SWAP", "1", 0.03, 20.0, 0, 0);
        //swapService.netGrid("okexsub1","LTC-USDT-SWAP", "1", 0.03, 200.0, 0, 0);
//        swapService.netGrid("okex","BTC-USDT-SWAP", "0.2", 0.01, 100.0, 0, 0);
        //swapService.netGrid("okexsub1","ETH-USDT-SWAP", "1", 0.03, 200.0, 0, 0);
        //swapService.netGrid("okexsub1","XRP-USDT-SWAP", "1", 0.03, 200.0, 0, 0);


    }

    @Scheduled(cron = "1/13 * * * * ?")
    public  void swapAndSpotHedging() {
        swapService.swapAndSpotHedging("okexsub1", "BTC-USDT-SWAP", "BTC-USDT", 0.02, 0.5);
        swapService.swapAndSpotHedging("okexsub1", "ETH-USDT-SWAP", "ETH-USDT", 0.03, 1);
//        swapService.swapAndSpotHedging("okexmock1", "XRP-USDT-SWAP", "XRP-USDT", 0.01, 1);
//        swapService.swapAndSpotHedging("okexmock1", "SUI-USDT-SWAP", "SUI-USDT", 0.01, 10);
        swapService.swapAndSpotHedging("okexsub1", "SOL-USDT-SWAP", "SOL-USDT", 0.03, 1);
        swapService.swapAndSpotHedging("okexsub1", "SUI-USDT-SWAP", "SUI-USDT", 0.03, 30);
        swapService.swapAndSpotHedging("okexsub1", "ADA-USDT-SWAP", "ADA-USDT", 0.03, 1);
        swapService.swapAndSpotHedging("okexsub1", "BNB-USDT-SWAP", "BNB-USDT", 0.02, 20);
        swapService.swapAndSpotHedging("okexsub1", "XRP-USDT-SWAP", "XRP-USDT", 0.03, 1);

    }

}
