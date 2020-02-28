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


    @Scheduled(cron = "13 */1 * * * ?")
    public  void netGrid() {
       // swapService.netGrid("BTC-USD-SWAP", "1", 70.0, 0.002);
        swapService.transfer("BTC-USD-SWAP",  0.002);
        swapService.netGrid("ETH-USD-SWAP", "1", 1.2,0.1);
       // swapService.transfer("ETH-USD-SWAP",0.1);
//        swapService.netGrid("EOS-USD-SWAP", "2", 0.018, 1.0);
//        swapService.netGrid("BCH-USD-SWAP", "2", 0.8, 0.1);
        swapService.transfer("BCH-USD-SWAP", 0.1);
    //    swapService.netGrid("XRP-USD-SWAP", "2", 0.0015,20.0);
        swapService.transfer("XRP-USD-SWAP", 20.0);
//        swapService.netGrid("LTC-USD-SWAP", "2", 0.25, 0.3);
        swapService.transfer("LTC-USD-SWAP", 0.3);
//        swapService.netGrid("ETH-USDT-SWAP", "140", 0.6, 5.0);
        swapService.transfer("BTC-USDT-SWAP", 10.0);
        swapService.transfer("BCH-USDT-SWAP", 10.0);


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
