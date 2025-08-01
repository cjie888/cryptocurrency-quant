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
public class OptionsTask {

    @Autowired
    private OptionsService optionsService;

    @Scheduled(cron = "3/11 * * * * ?")
    public  void swapAndOptionHedging() {
//        optionsService.swapAndOptionHedging("okexmock1", "ETH-USDT-SWAP", "ETH", 0.01, 1);
//        optionsService.swapAndOptionHedging("okexmock1", "BTC-USDT-SWAP", "BTC", 0.01, 1);
        optionsService.swapAndOptionHedging("okexsub3", "ETH-USDT-SWAP", "ETH", 0.03, 1);

    }

//    @Scheduled(cron = "45 59 15 * * ?")  // 15:59:20 执行
//    @Scheduled(cron = "55 59 15 * * ?")
//    @Scheduled(cron = "3/11 * * * * ?")
    public void netGrid1() {
        optionsService.netGrid1("okex", "BTC-USDT-SWAP", "BTC", 1, 0.02, 0.005);
        optionsService.netGrid1("okex", "ETH-USDT-SWAP", "ETH", 1, 0.013,0.012);
    }

    @Scheduled(cron = "40 45 23 * * ?")  // 15:59:20 执行
//    @Scheduled(cron = "52 06 16 * * ?")
//    @Scheduled(cron = "3/15 * * * * ?")
    public void netGrid2() {
//        optionsService.netGrid2("okex", "BTC-USDT-SWAP", "BTC", 1, 0.015, 0.008, 0.007);
//        optionsService.netGrid2("okex", "ETH-USDT-SWAP", "ETH", 1, 0.03, 0.016,0.015);
    }

//    @Scheduled(cron = "52 35 * * * ?")
    @Scheduled(cron = "3/15 * * * * ?")
    public void dynamicDeltaHedging() {
         optionsService.dynamicDeltaHedging("okex", "ETH-USDT-SWAP", "ETH", 0.03, 10);
         optionsService.dynamicDeltaHedging("okex", "BTC-USDT-SWAP", "BTC", 0.025, 10);
    }

    @Scheduled(cron = "40 15 6 * * ?")
    public void coveredCall() {
        optionsService.coveredCall("okexsub3", "ETH", 1, 0.02);
        optionsService.coveredCall("okexsub3", "BTC", 2, 0.01);
    }

    @Scheduled(cron = "43 15 * * * ?")
    //@Scheduled(cron = "3/15 * * * * ?")
    public void computeOptionBenefit(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStartTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStartTime = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime monthStartTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        optionsService.computeOptionBenefit("okex", "BTC-USDT-SWAP", "BTC",
                dayStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okex本日期权BTC收益");
        optionsService.computeOptionBenefit("okex", "ETH-USDT-SWAP", "ETH", dayStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okex本日期权ETH收益");
        optionsService.computeOptionBenefit("okex", "BTC-USDT-SWAP","BTC", weekStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okex本周期权BTC收益");
        optionsService.computeOptionBenefit("okex", "ETH-USDT-SWAP","ETH", weekStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okex本周期权ETH收益");
        optionsService.computeOptionBenefit("okex", "BTC-USDT-SWAP","BTC", monthStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okex本月期权BTC收益");
        optionsService.computeOptionBenefit("okex", "ETH-USDT-SWAP","ETH", monthStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okex本月期权ETH收益");

    }


    @Scheduled(cron = "43 45 * * * ?")
    //@Scheduled(cron = "3/15 * * * * ?")
    public void computeOptionBenefit2(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStartTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStartTime = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime monthStartTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        optionsService.computeOptionBenefit("okexsub3", "BTC-USDT-SWAP", "BTC",
                dayStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本日期权BTC收益");
        optionsService.computeOptionBenefit("okexsub3", "ETH-USDT-SWAP", "ETH", dayStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本日期权ETH收益");
        optionsService.computeOptionBenefit("okexsub3", "BTC-USDT-SWAP","BTC", weekStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本周期权BTC收益");
        optionsService.computeOptionBenefit("okexsub3", "ETH-USDT-SWAP","ETH", weekStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本周期权ETH收益");
        optionsService.computeOptionBenefit("okexsub3", "BTC-USDT-SWAP","BTC", monthStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本月期权BTC收益");
        optionsService.computeOptionBenefit("okexsub3", "ETH-USDT-SWAP","ETH", monthStartTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(), "okexsub3本月期权ETH收益");

    }


    @Scheduled(cron = "37 03 18 * * ?")
    public void butterfly() {
//        optionsService.butterfly("okex", "BTC", 1, 2, 0.02);
//        optionsService.butterfly("okex", "ETH", 1, 2, 0.05);

    }
}
