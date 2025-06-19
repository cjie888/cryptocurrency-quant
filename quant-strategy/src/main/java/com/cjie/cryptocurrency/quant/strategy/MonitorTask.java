package com.cjie.cryptocurrency.quant.strategy;

import com.cjie.cryptocurrency.quant.strategy.okex.v5.OptionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MonitorTask {

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private UpbbitService upbbitService;

    @Scheduled(cron = "3 10 * * * ?")
    public void monitorOptionsIV() {
        optionsService.monitorIV("okex", "BTC-USD-250926-100000-C", "BTC", "250926");
        optionsService.monitorIV("okex", "BTC-USD-250926-110000-C", "BTC", "250926");
        optionsService.monitorIV("okex", "BTC-USD-250926-120000-C", "BTC", "250926");
        optionsService.monitorIV("okex", "BTC-USD-250926-130000-C", "BTC", "250926");
        optionsService.monitorIV("okex", "BTC-USD-250926-140000-C", "BTC", "250926");
        optionsService.monitorIV("okex", "BTC-USD-250926-150000-C", "BTC", "250926");

        optionsService.monitorIV("okex", "ETH-USD-250926-2800-C", "ETH", "250926");
        optionsService.monitorIV("okex", "ETH-USD-250926-3000-C", "ETH", "250926");
        optionsService.monitorIV("okex", "ETH-USD-250926-3200-C", "ETH", "250926");
        optionsService.monitorIV("okex", "ETH-USD-250926-3500-C", "ETH", "250926");
        optionsService.monitorIV("okex", "ETH-USD-250926-3800-C", "ETH", "250926");

    }

   @Scheduled(cron = "3 * * * * ?")
    public void monitorOptionsIVSkew() {
        optionsService.monitorIvSkew("okex", "BTC");
        optionsService.monitorIvSkew("okex", "ETH");

    }

    @Scheduled(cron = "17 * * * * ?")
    public void monitorUpbitNewCoin() {
        upbbitService.monitorNewCoin();
    }

}
