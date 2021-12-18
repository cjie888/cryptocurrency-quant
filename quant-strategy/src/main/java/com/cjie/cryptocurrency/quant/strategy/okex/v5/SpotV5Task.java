package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SpotV5Task {

    @Autowired
    private SpotV5Service spotService;

    @Scheduled(cron = "7/13 * * * * ?")
    public  void netGrid() {

        spotService.netGrid("okexsub2", "MANA-USDT", "1", 0.03);

//ltc 6  xrp 3 etc 5
    }
}
