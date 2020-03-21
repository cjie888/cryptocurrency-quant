package com.cjie.cryptocurrency.quant.strategy.okex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpotTask {

    @Autowired
    private SpotService spotService;

    @Scheduled(cron = "7/20 * * * * ?")
    public  void netGrid() {

        spotService.netGrid("okexsub1", "ETH-USDT", "0.05000000", 0.03);
        spotService.netGrid("okexsub1", "BTC-USDT", "0.00100000", 0.03);
        spotService.netGrid("okexsub1", "BCH-USDT", "0.04000000", 0.03);
//ltc 6  xrp 3 etc 5
    }
}
