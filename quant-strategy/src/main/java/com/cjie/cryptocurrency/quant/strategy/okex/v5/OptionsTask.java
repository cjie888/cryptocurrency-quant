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
    public  void swapAndOptionHedging() {
//        optionsService.swapAndOptionHedging("okexmock1", "ETH-USDT-SWAP", "ETH", 0.01, 1);
//        optionsService.swapAndOptionHedging("okexmock1", "BTC-USDT-SWAP", "BTC", 0.01, 1);
//        optionsService.swapAndOptionHedging("okexsub3", "ETH-USDT-SWAP", "ETH", 0.025, 1);

    }

    @Scheduled(cron = "45 59 15 * * ?")  // 15:59:20 执行
    @Scheduled(cron = "55 59 15 * * ?")
    @Scheduled(cron = "3/11 * * * * ?")
    public void netGrid() {
        optionsService.netGrid("okex", "BTC-USDT-SWAP", "BTC", 5, 0.01);
        optionsService.netGrid("okex", "ETH-USDT-SWAP", "ETH", 5, 0.013);
    }
}
