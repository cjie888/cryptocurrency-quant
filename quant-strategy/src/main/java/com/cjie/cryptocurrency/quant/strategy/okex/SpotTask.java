package com.cjie.cryptocurrency.quant.strategy.okex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.awt.Symbol;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SpotTask {

    @Autowired
    private SpotService spotService;

    //@Scheduled(cron = "7/17 * * * * ?")
    public  void netGrid() {

        spotService.netGrid("okex", "EOS-USDT", "2", 0.03);
        spotService.netGrid("okex", "OKT-USDT", "1", 0.03);
        spotService.netGrid("okex", "DOGE-USDT", "1000", 0.03);
        spotService.netGrid("okex", "UNI-USDT", "1", 0.03);
        spotService.netGrid("okex", "XLM-USDT", "50", 0.03);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        spotService.netGrid("okex", "IOTA-USDT", "30", 0.03);
        spotService.netGrid("okex", "XTZ-USDT", "10", 0.03);
        spotService.netGrid("okex", "AAVE-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "AVAX-USDT", "1", 0.03);
        spotService.netGrid("okex", "ICX-USDT", "10", 0.03);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        spotService.netGrid("okex", "THETA-USDT", "5", 0.03);



//ltc 6  xrp 3 etc 5
    }

    //@Scheduled(cron = "3/19 * * * * ?")
    public  void netGrid2()  {

        spotService.netGrid("okex", "BTC-USDT", "0.00100000", 0.03);
        spotService.netGrid("okex", "OKB-USDT", "10", 0.03);
        spotService.netGrid("okex", "XMR-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "ZEC-USDT", "0.2", 0.03);
        spotService.netGrid("okex", "FIL-USDT", "1", 0.03);
        spotService.netGrid("okex", "ZIL-USDT", "100", 0.03);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        spotService.netGrid("okex", "ADA-USDT", "20", 0.03);
        spotService.netGrid("okex", "XEM-USDT", "30", 0.03);
        spotService.netGrid("okex", "DASH-USDT", "0.1", 0.03);
        spotService.netGrid("okex", "GRT-USDT", "10", 0.03);
        spotService.netGrid("okex", "COMP-USDT", "0.05", 0.03);





//ltc 6  xrp 3 etc 5
    }

    @Scheduled(cron = "7 */30 * * * ?")
    public  void benefit() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<String> symbols1 = new ArrayList();
        symbols1.add("ETH-USDT");
        symbols1.add("BCH-USDT");
        symbols1.add("LTC-USDT");
        symbols1.add("XRP-USDT");
        symbols1.add("ETC-USDT");
        symbols1.add("BSV-USDT");
        symbols1.add("SUSHI-USDT");
        symbols1.add("ATOM-USDT");
        symbols1.add("LINK-USDT");
        symbols1.add("DOT-USDT");


        //spotService.netGrid("okexsub2", "MANA-USDT", "1", 0.03);
        spotService.computeBenefit("每日买卖cjie888", startTime, now, symbols1);

    }

    @Scheduled(cron = "7 17 */2 * * ?")
    public  void benefit2() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<String> symbols1 = new ArrayList();
        symbols1.add("ETH-USDT");
        symbols1.add("BCH-USDT");
        symbols1.add("LTC-USDT");
        symbols1.add("XRP-USDT");
        symbols1.add("ETC-USDT");
        symbols1.add("BSV-USDT");
        symbols1.add("SUSHI-USDT");
        symbols1.add("ATOM-USDT");
        symbols1.add("LINK-USDT");
        symbols1.add("DOT-USDT");
        spotService.computeBenefit("每月买卖1", startTime, now, symbols1);

    }

    @Scheduled(cron = "7 37 */6 * * ?")
    public  void benefit3() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusMonths(2);
        List<String> symbols1 = new ArrayList();
        symbols1.add("ETH-USDT");
        symbols1.add("BCH-USDT");
        symbols1.add("LTC-USDT");
        symbols1.add("XRP-USDT");
        symbols1.add("ETC-USDT");
        symbols1.add("BSV-USDT");
        symbols1.add("SUSHI-USDT");
        symbols1.add("ATOM-USDT");
        symbols1.add("LINK-USDT");
        symbols1.add("DOT-USDT");
        spotService.computeBenefit("每季买卖1", startTime, now, symbols1);

    }

    //@Scheduled(cron = "17 47 */6 * * ?")
    public  void benefit4() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusMonths(11);

        List<String> symbols1 = new ArrayList();
        symbols1.add("ETH-USDT");
        symbols1.add("BCH-USDT");
        symbols1.add("LTC-USDT");
        symbols1.add("XRP-USDT");
        symbols1.add("ETC-USDT");
        symbols1.add("BSV-USDT");
        symbols1.add("SUSHI-USDT");
        symbols1.add("ATOM-USDT");
        symbols1.add("LINK-USDT");
        symbols1.add("DOT-USDT");
        spotService.computeBenefit("每年买卖1", startTime, now, symbols1);

    }
}
