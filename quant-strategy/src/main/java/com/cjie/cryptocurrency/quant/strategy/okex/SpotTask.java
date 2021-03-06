package com.cjie.cryptocurrency.quant.strategy.okex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SpotTask {

    @Autowired
    private SpotService spotService;

    @Scheduled(cron = "7/11 * * * * ?")
    public  void netGrid() {

        spotService.netGrid("okex", "EOS-USDT", "2", 0.03);
        spotService.netGrid("okex", "OKT-USDT", "1", 0.03);
        spotService.netGrid("okexsub1", "XRP-USDT", "20", 0.03);
        spotService.netGrid("okexsub1", "ETC-USDT", "1", 0.03);
        spotService.netGrid("okexsub1", "BSV-USDT", "0.05000000", 0.03);
        spotService.netGrid("okex", "DOGE-USDT", "1000", 0.03);
        spotService.netGrid("okexsub1", "ATOM-USDT", "1", 0.03);
        spotService.netGrid("okexsub1", "LINK-USDT", "0.50000000", 0.03);
        spotService.netGrid("okexsub1", "DOT-USDT", "1", 0.03);
        spotService.netGrid("okex", "UNI-USDT", "1", 0.03);
        spotService.netGrid("okex", "XLM-USDT", "50", 0.03);
        spotService.netGrid("okex", "IOTA-USDT", "30", 0.03);
        spotService.netGrid("okex", "XTZ-USDT", "10", 0.03);
        spotService.netGrid("okex", "AAVE-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "AVAX-USDT", "1", 0.03);
        spotService.netGrid("okex", "ICX-USDT", "10", 0.03);
        spotService.netGrid("okex", "THETA-USDT", "5", 0.03);



//ltc 6  xrp 3 etc 5
    }

    @Scheduled(cron = "3/7 * * * * ?")
    public  void netGrid2() {

        spotService.netGrid("okexsub1", "ETH-USDT", "0.05000000", 0.03);
        spotService.netGrid("okex", "BTC-USDT", "0.00100000", 0.03);
        spotService.netGrid("okexsub1", "BCH-USDT", "0.04000000", 0.03);
        spotService.netGrid("okexsub1", "LTC-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "OKB-USDT", "10", 0.03);
        spotService.netGrid("okex", "XMR-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "ZEC-USDT", "0.2", 0.03);
        spotService.netGrid("okex", "FIL-USDT", "1", 0.03);
        spotService.netGrid("okex", "ZIL-USDT", "100", 0.03);
        spotService.netGrid("okex", "ADA-USDT", "20", 0.03);
        spotService.netGrid("okex", "XEM-USDT", "30", 0.03);
        spotService.netGrid("okex", "DASH-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "GRT-USDT", "10", 0.03);
        spotService.netGrid("okexsub1", "SUSHI-USDT", "1", 0.03);
        spotService.netGrid("okex", "COMP-USDT", "0.05", 0.03);





//ltc 6  xrp 3 etc 5
    }

    @Scheduled(cron = "7 */30 * * * ?")
    public  void benefit() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        spotService.computeBenefit("每日买卖", startTime, now);

    }

    @Scheduled(cron = "7 17 */2 * * ?")
    public  void benefit2() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        spotService.computeBenefit("每月买卖", startTime, now);

    }

    @Scheduled(cron = "7 37 */6 * * ?")
    public  void benefit3() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusMonths(2);
        spotService.computeBenefit("每季买卖", startTime, now);

    }

    @Scheduled(cron = "17 47 */6 * * ?")
    public  void benefit4() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusMonths(11);
        spotService.computeBenefit("每年买卖", startTime, now);

    }
}
