package com.cjie.cryptocurrency.quant.strategy;

import com.cjie.cryptocurrency.quant.strategy.binance.BinanceFundingRateChecker;
import com.cjie.cryptocurrency.quant.strategy.okex.v5.OptionsService;
import com.cjie.cryptocurrency.quant.strategy.okex.v5.SwapV5Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Component
@Slf4j
public class MonitorTask {

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private UpbbitService upbbitService;

    @Autowired
    private SwapV5Service swapV5Service;

    @Autowired
    private BinanceFundingRateChecker binanceFundingRateChecker;
//    @Scheduled(cron = "3 10 * * * ?")
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

//   @Scheduled(cron = "3 13 * * * ?")
    public void monitorOptionsIVSkew() {
        optionsService.monitorIvSkew("okex", "BTC");
        optionsService.monitorIvSkew("okex", "ETH");

    }

//    @Scheduled(cron = "17 * * * * ?")
    public void monitorUpbitNewCoin() {
        upbbitService.monitorNewCoin();
    }

//    @Scheduled(cron = "37 53 * * * ?")
    public void butterfly() {
//        optionsService.ironCondor("okexmock1", "BTC", 1, 4, 0.03, 0.01);
//        optionsService.ironCondor("okexmock1", "ETH", 1, 4, 0.03, 0.01);
        optionsService.dynamicDeltaHedging("okex", "ETH-USDT-SWAP", "ETH", 0.03, 10);

    }

    @Scheduled(cron = "37 */10 * * * ?")
    public void binanceFundingRateChecker() {
        binanceFundingRateChecker.monitorHighFundingRatePairs();
    }


}
