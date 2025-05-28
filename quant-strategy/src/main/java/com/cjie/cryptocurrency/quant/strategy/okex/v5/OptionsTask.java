package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OptionsTask {

    @Autowired
    private OptionsService optionsService;

    @Scheduled(cron = "3/11 * * * * ?")
    public  void netGrid() {
//        optionsService.swapAndOptionHedging("okexmock1", "ETH-USDT-SWAP", "ETH", 0.01, 1);
//        optionsService.swapAndOptionHedging("okexmock1", "BTC-USDT-SWAP", "BTC", 0.01, 1);
        optionsService.swapAndOptionHedging("okexsub3", "ETH-USDT-SWAP", "ETH", 0.02, 1);
    }
}
