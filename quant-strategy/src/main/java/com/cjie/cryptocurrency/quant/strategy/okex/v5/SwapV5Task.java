package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Component
@Slf4j
public class SwapV5Task {

    @Autowired
    private SwapV5Service swapService;


    @Scheduled(cron = "3/11 * * * * ?")
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
//        swapService.netGrid("okexsub2","MANA-USDT-SWAP", "1", 0.03, 20.0, 0, 0);
        //swapService.netGrid("okexsub1","LTC-USDT-SWAP", "1", 0.03, 200.0, 0, 0);
//        swapService.netGrid("okex","BTC-USDT-SWAP", "0.2", 0.01, 100.0, 0, 0);
        //swapService.netGrid("okexsub1","ETH-USDT-SWAP", "1", 0.03, 200.0, 0, 0);
//        swapService.netGrid("okexsub1","XRP-USDT-SWAP", "1", 0.03, 200.0, 0, 0);


    }

    @Scheduled(cron = "1/13 * * * * ?")
    public  void swapAndSpotHedging() {
//        swapService.swapAndSpotHedging("okexsub3", "BTC-USDT-SWAP", "BTC-USDT", 0.02, 0.1);
//        swapService.swapAndSpotHedging("okexsub3", "ETH-USDT-SWAP", "ETH-USDT", 0.03, 0.2);
//        swapService.swapAndSpotHedging("okexmock1", "XRP-USDT-SWAP", "XRP-USDT", 0.01, 1);
//        swapService.swapAndSpotHedging("okexmock1", "SUI-USDT-SWAP", "SUI-USDT", 0.01, 10);
        swapService.swapAndSpotHedging("okexsub3", "SOL-USDT-SWAP", "SOL-USDT", 0.03, 0.3);
//        swapService.swapAndSpotHedging("okexsub3", "SUI-USDT-SWAP", "SUI-USDT", 0.03, 20);
//        swapService.swapAndSpotHedging("okexsub3", "ADA-USDT-SWAP", "ADA-USDT", 0.03, 0.5);
        swapService.swapAndSpotHedging("okexsub3", "BNB-USDT-SWAP", "BNB-USDT", 0.03, 5);
        swapService.swapAndSpotHedging("okexsub3", "XRP-USDT-SWAP", "XRP-USDT", 0.03, 0.2);
//        swapService.swapAndSpotHedging("okexsub3", "LINK-USDT-SWAP", "LINK-USDT", 0.03, 5);
//        swapService.swapAndSpotHedging("okexsub3", "DOGE-USDT-SWAP", "DOGE-USDT", 0.03, 0.3);


        swapService.swapAndSpotHedging("okexsub3", "XAUT-USDT-SWAP", "XAUT-USDT", 0.02, 20);

    }


    @Scheduled(cron = "43 25 * * * ?")
    public void monitorSwapProfit() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStartTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStartTime = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime monthStartTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        swapService.computeSwapBenefit("okexsub1",
                dayStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub1本日永续合约收益");
        swapService.computeSwapBenefit("okexsub1",
                weekStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub1本周永续合约收益");
        swapService.computeSwapBenefit("okexsub1",
                monthStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub1本月永续合约收益");
    }

    @Scheduled(cron = "43 55 * * * ?")
    public void monitorSwapProfit2() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStartTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStartTime = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime monthStartTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        swapService.computeSwapBenefit("okexsub3",
                dayStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本日永续合约收益");
        swapService.computeSwapBenefit("okexsub3",
                weekStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本周永续合约收益");
        swapService.computeSwapBenefit("okexsub3",
                monthStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本月永续合约收益");
    }

}
