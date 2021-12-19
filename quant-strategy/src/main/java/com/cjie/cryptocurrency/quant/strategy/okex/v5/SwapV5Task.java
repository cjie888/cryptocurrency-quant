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


    @Scheduled(cron = "3/13 * * * * ?")
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
        swapService.netGrid("okexsub2","MANA-USDT-SWAP", "1", 0.03, 200.0, 0, 0);
     //   swapService.transfer("LTC-USD-SWAP", 0.3);
//        swapService.netGrid("ETH-USDT-SWAP", "140", 0.6, 5.0);
        //swapService.netGrid("BTC-USDT-SWAP", "1", 100.0, 30.0, 0, 7000);
        //swapService.transfer("BTC-USDT-SWAP", 10.0);
        //swapService.transfer("BCH-USDT-SWAP", 10.0);
        //swapService.netGrid("BCH-USDT-SWAP", "1", 1.5, 10.0, 27, 220);
        //swapService.netGrid("okex","EOS-USDT-SWAP", "1", 0.03, 50.0, 0, 1);
        //swapService.netGrid("okexsub1","ATOM-USDT-SWAP", "1", 0.08, 50.0, 0, 1);
        //swapService.netGrid("okexsub1","XRP-USDT-SWAP", "1", 0.008, 50.0, 0, 0.01);
        //swapService.netGrid("UNI-USDT-SWAP", "1", 0.05, 5.0, 0, 1);




    }

}
