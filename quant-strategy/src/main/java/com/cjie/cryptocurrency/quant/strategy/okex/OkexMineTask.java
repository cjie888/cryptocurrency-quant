package com.cjie.cryptocurrency.quant.strategy.okex;


import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OkexMineTask {

    @Autowired
    private OkexMineService mineService;

    //@Scheduled(cron = "* */2 * * * ?")
    public void mineCurrency1() throws JobExecutionException {
        log.info("start mining");
        //log.info(JSON.toJSONString(spotAccountAPIService.getAccountByCurrency("btc")));
        try {
            mineService.mine1("cac", "eth", 0.005);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("end mining");


    }
    @Scheduled(cron = "*/30 * * * * ?")
    public void mineCurrency3() throws JobExecutionException {
        log.info("start mining");
        //log.info(JSON.toJSONString(spotAccountAPIService.getAccountByCurrency("btc")));
        try {
            mineService.mine3("eos", "btc", 0.005, 0.5);
            mineService.mine3("okb", "usdt", 0.005, 0.8);
            mineService.mine3("ltc", "eth", 0.005, 0.5);
            //mineService.mine3("cac", "eth", 0.005);
        } catch (Exception e) {
            log.error("error mining", e);
        }
        log.info("end mining");


    }
}
