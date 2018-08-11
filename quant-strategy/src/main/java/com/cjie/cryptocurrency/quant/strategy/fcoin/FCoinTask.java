package com.cjie.cryptocurrency.quant.strategy.fcoin;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.mapper.CurrencyBalanceMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyBalance;
import com.cjie.cryptocurrency.quant.mapper.CurrencyBalanceMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyBalance;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

@Component
@Slf4j
public class FCoinTask {

    @Autowired
    private FcoinUtils fcoinUtils;

    @Autowired
    private CurrencyBalanceMapper currencyBalanceMapper;

    //@Scheduled(cron = "1 * * * * ?")
    public void mineCurrency() throws JobExecutionException {
        try {
            //fcoinUtils.ftusdt1("ftusdt", "ft", "usdt",0);
            //fcoinUtils.ftusdt2("ftusdt", "ft", "usdt",0.05);
            //fcoinUtils.ftusdt1("btcusdt","btc","usdt",0);
            //fcoinUtils.ftusdt2("ftusdt","ft","usdt",0.01);
            fcoinUtils.ftusdt1("ftusdt", "ft", "usdt", 0.01);

            //fcoinUtils.ftusdt1("icxeth","icx","eth",0);
        }catch (Exception e){
            log.info("==========FcoinJob发生异常============",e);
            throw new JobExecutionException("ftustd 方法体执行异常");
        }
    }


    @Scheduled(cron = "*/10 * * * * ?")
    public void mineCurrency3() throws JobExecutionException {
        try {
            //fcoinUtils.ftusdt1("ftusdt", "ft", "usdt",0);
            //fcoinUtils.ftusdt2("ftusdt", "ft", "usdt",0.05);
            //fcoinUtils.ftusdt1("btcusdt","btc","usdt",0);
            //fcoinUtils.ftusdt2("ftusdt","ft","usdt",0.01);
            fcoinUtils.ftusdt3("ftusdt", "ft", "usdt", 0.001);

            //fcoinUtils.ftusdt1("icxeth","icx","eth",0);
        }catch (Exception e){
            log.info("==========FcoinJob发生异常============",e);
            throw new JobExecutionException("ftustd 方法体执行异常");
        }
    }


    @Scheduled(cron = "0 32 */1 * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")
    public void currencyBalance() throws JobExecutionException {
        try {

            String balanceStr = FcoinUtils.getBalance();
            JSONObject jsonObject = JSON.parseObject(balanceStr);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            jsonArray.stream().forEach(jsonObj -> {
                JSONObject obj = (JSONObject) jsonObj;
                BigDecimal available = new BigDecimal(obj.getString("available")).setScale(16, BigDecimal.ROUND_FLOOR);
                BigDecimal balance = new BigDecimal(obj.getString("balance")).setScale(16, BigDecimal.ROUND_FLOOR);
                BigDecimal hold = new BigDecimal(obj.getString("frozen")).setScale(16, BigDecimal.ROUND_FLOOR);
                String currency = obj.getString("currency");
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    CurrencyBalance currencyBalance = currencyBalanceMapper.getByCurrency(currency, "fcoin");
                    if (currencyBalance != null) {
                        currencyBalance.setAvailable(available);
                        currencyBalance.setBalance(balance);
                        currencyBalance.setHold(hold);
                        currencyBalance.setModifyTime(new Date());
                        currencyBalanceMapper.updateByPrimaryKey(currencyBalance);
                    } else {
                        currencyBalance = CurrencyBalance.builder().
                                currency(currency).available(available)
                                .balance(balance).hold(hold)
                                .site("fcoin").createTime(new Date()).build();
                        currencyBalanceMapper.insert(currencyBalance);
                    }
                }
            });
        }catch (Exception e){
            log.info("==========FcoinJob 获取余额发生异常============",e);
            throw new JobExecutionException("ftustd 获取余额发生异常 方法体执行异常");
        }
    }
}
