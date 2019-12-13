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


    @Scheduled(cron = "*/11 * * * * ?")
    public  void netGrid() {
        swapService.netGrid("BTC-USD-SWAP", "1", 60.0);
        swapService.netGrid("ETH-USD-SWAP", "2", 0.6);
        swapService.netGrid("EOS-USD-SWAP", "2", 0.015);
        swapService.netGrid("BCH-USD-SWAP", "2", 0.7);
        swapService.netGrid("XRP-USD-SWAP", "2", 0.001);



    }
}
