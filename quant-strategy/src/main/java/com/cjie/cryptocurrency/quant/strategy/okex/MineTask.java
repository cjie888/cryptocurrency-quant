package com.cjie.cryptocurrency.quant.strategy.okex;


import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MineTask {

    @Autowired
    private MineService mineService;


    //@Scheduled(cron = "* */60 * * * ?")
    public void balance() throws JobExecutionException {
        log.info("start balance");
        try {
            mineService.collectBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("end balance");


    }

    @Scheduled(cron = "*/7 * * * * ?")
    public void mineCurrency1() throws JobExecutionException {
        log.info("start mining");
        //log.info(JSON.toJSONString(spotAccountAPIService.getAccountByCurrency("btc")));
        try {
            mineService.mine1("okex","bch", "btc", 0.005);
            Thread.sleep(1000);
            mineService.mine1("okex","eos", "eth", 0.005);
            Thread.sleep(1000);
            mineService.mine1("okex","eos", "btc", 0.005);
            Thread.sleep(1000);
            mineService.mine1("okex","zil", "eth", 0.005);

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("end mining");


    }
    @Scheduled(cron = "*/5 * * * * ?")
    public void mineCurrency3() throws JobExecutionException {
        log.info("start mining");
        //log.info(JSON.toJSONString(spotAccountAPIService.getAccountByCurrency("btc")));
        try {
            //mineService.mine3("okex","eos", "btc", 0.003, 0.5);
            //Thread.sleep(1000);
            mineService.mine3("okex","okb", "usdt", 0.003, 0.5);
            mineService.mine3("okexsub1","eth", "usdt", 0.003, 0.5);
            //mineService.mine3("okexsub2","btc", "usdt", 0.003, 0.5);
            //Thread.sleep(1000);
            //mineService.mine3("oktop","bch", "eth", 0.005, 0.5);
            //mineService.mine3("cac", "eth", 0.005);
        } catch (Exception e) {
            log.error("error mining", e);
        }
        log.info("end mining");


    }

    @Scheduled(cron = "*/30 * * * * ?")
    public void mineCurrency4() throws JobExecutionException {
        log.info("start mining");
        //log.info(JSON.toJSONString(spotAccountAPIService.getAccountByCurrency("btc")));
        try {
            //mineService.mine3("okex","eos", "btc", 0.003, 0.5);
            //Thread.sleep(1000);
            //mineService.mine3("okexsub1","eth", "usdt", 0.003, 0.5);
            mineService.mine4("okexsub2","btc", "usdt", 0.003, 2, 10);
            mineService.mine4("okexsub2","eos", "usdt", 0.003, 2, 10);
            //Thread.sleep(1000);
            //mineService.mine3("oktop","bch", "eth", 0.005, 0.5);
            //mineService.mine3("cac", "eth", 0.005);
        } catch (Exception e) {
            log.error("error mining", e);
        }
        log.info("end mining");


    }
}
