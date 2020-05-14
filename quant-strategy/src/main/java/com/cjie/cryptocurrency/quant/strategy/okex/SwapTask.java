package com.cjie.cryptocurrency.quant.strategy.okex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SwapTask {

    @Autowired
    private SwapService swapService;


    @Scheduled(cron = "13/20 * * * * ?")
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
      //  swapService.netGrid("LTC-USD-SWAP", "1", 0.3, 0.3, 130, 42.3);
     //   swapService.transfer("LTC-USD-SWAP", 0.3);
//        swapService.netGrid("ETH-USDT-SWAP", "140", 0.6, 5.0);
        swapService.netGrid("BTC-USDT-SWAP", "1", 80.0, 30.0, 0, 7000);
        //swapService.transfer("BTC-USDT-SWAP", 10.0);
        //swapService.transfer("BCH-USDT-SWAP", 10.0);
        //swapService.netGrid("BCH-USDT-SWAP", "1", 1.5, 10.0, 27, 220);


    }

    @Scheduled(cron = "*/17 * * * * ?")
    public void dualThrust() {
        swapService.dualTrust("BTC-USD-SWAP", 0.7);
        swapService.dualTrust("ETH-USD-SWAP", 0.7);
        swapService.dualTrust("EOS-USD-SWAP", 0.7);
        swapService.dualTrust("BCH-USD-SWAP", 0.7);
        swapService.dualTrust("XRP-USD-SWAP", 0.7);
        swapService.dualTrust("LTC-USD-SWAP", 0.7);

    }

    @Scheduled(cron = "7 */30 * * * ?")
    public  void benefit() {
        swapService.computeBenefit();

    }
}
