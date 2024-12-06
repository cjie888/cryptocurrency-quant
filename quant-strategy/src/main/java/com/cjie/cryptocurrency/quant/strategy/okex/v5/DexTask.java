package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class DexTask {

    @Autowired
    private DexService dexService;

    @Scheduled(cron = "7/17 * * * * ?")
    public  void netGrid() {
//        dexService.sell("501", "85cQsFgbi8mBZxiPppbpPXuV7j1hA8tBwhjF4gKW6mHg", new BigDecimal("0.00004"),new BigDecimal("1000000"));
        dexService.sell("501", "721VwRWjDbEDMvaKr3y1TQhX6YV3zj4cQKTxh7typump", new BigDecimal("0.0004"),new BigDecimal("120000"));
        dexService.sell("501", "5U5vTXQnpoFY8waWZhVkBGbMin2p5mfd6tuRScK2pump", new BigDecimal("0.0005"),new BigDecimal("100000"));

    }
}
