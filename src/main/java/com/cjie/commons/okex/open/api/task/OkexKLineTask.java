package com.cjie.commons.okex.open.api.task;


import com.alibaba.fastjson.JSON;
import com.cjie.commons.okex.open.api.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.mapper.CurrencyKlineMapper;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPairMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyKline;
import com.cjie.cryptocurrency.quant.model.CurrencyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@Slf4j(topic = "time")
public class OkexKLineTask {

    @Autowired
    private SpotProductAPIService spotProductAPIService;

    @Autowired
    private CurrencyKlineMapper currencyKlineMapper;

    @Autowired
    private CurrencyPairMapper currencyPairMapper;

    @Scheduled(cron = "5 * * * * ?")
    public void kline() throws Exception {
        log.info("get okex 1min kline begin");
        getKline("1min", "");
        log.info("get okex 1min kline end");
    }

    @Scheduled(cron = "4 */5 * * * ?")
    public void kline5m() throws Exception {
        log.info("get okex 5min kline begin");
        getKline("5min", "_5m");
        log.info("get okex 5min kline end");

    }
    @Scheduled(cron = "6 */9 * * * ?")
    public void kline15m() throws Exception {
        log.info("get okex 15min kline begin");
        getKline("15min", "_15m");
        log.info("get okex 15min kline end");

    }

    @Scheduled(cron = "6 */21 * * * ?")
    public void kline30m() throws Exception {
        log.info("get okex 30min kline begin");
        getKline("30min", "_30m");
        log.info("get okex 30min kline end");

    }

    @Scheduled(cron = "8 */42 * * * ?")
    public void kline60m() throws Exception {
        log.info("get okex 60min kline begin");
        getKline("60min", "_60m");
        log.info("get okex 60min kline end");

    }
    @Scheduled(cron = "8 7 */9 * * ?")
    public void kline1month() throws Exception {
        log.info("get okex 1month kline begin");
        getKline("1mon", "_1m");
        log.info("get okex 1mon kline end");

    }

    @Scheduled(cron = "8 13 */2 * * ?")
    public void kline1day() throws Exception {
        log.info("get okex 1day kline begin");
        getKline("1day", "_1d");
        log.info("get okex 1day kline end");

    }

    @Scheduled(cron = "8 17 */6 * * ?")
    public void kline1week() throws Exception {
        log.info("get okex 1week kline begin");
        getKline("1week", "_1w");
        log.info("get okex 1week kline end");

    }
    @Scheduled(cron = "8 22 4 */5 * ?")
    public void kline1eya() throws Exception {
        log.info("get okex 1yeay kline begin");
        getKline("1year", "_1y");
        log.info("get okex 1year kline end");

    }
    private void getKline(String type, String suffix) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            List<CurrencyPair> currencies = currencyPairMapper.getAllCurrency("okex");
            for (CurrencyPair symbol : currencies) {
                String baseCurrency = symbol.getBaseCurrency();
                String quotaCurrency = symbol.getQuotaCurrency();
                try {
                    String currSuffix = suffix;
                    if (type.equals("1min")) {
                        currSuffix = currSuffix + "_" + quotaCurrency.toLowerCase();
                    }
                    List<String[]> klines =  spotProductAPIService.getCandles("okex", baseCurrency + "-" + quotaCurrency, getGranularity(type), null, null);
                    for (String[] data : klines) {
                        log.info(JSON.toJSONString(data));
                        if (currencyKlineMapper.getCurrencyLine(dateFormat.parse(data[0]),
                                baseCurrency, quotaCurrency, "okex", currSuffix) != null) {
                            continue;
                        }
                        CurrencyKline kline = CurrencyKline.builder().klineTime(dateFormat.parse(data[0]))
                                .amount(BigDecimal.ZERO)
                                .count(0)
                                .baseCurrency(baseCurrency)
                                .quotaCurrency(quotaCurrency)
                                .open(new BigDecimal(data[3]))
                                .close(new BigDecimal(data[4]))
                                .high(new BigDecimal(data[2]))
                                .low(new BigDecimal(data[1]))
                                .vol(new BigDecimal(data[5]))
                                .site("okex")
                                .suffix(currSuffix)
                                .build();
                        log.info("{}-{}-{}--,{}", type, baseCurrency, quotaCurrency, JSON.toJSONString(data));


                        //currencyKlineMapper.insert(kline);
                    }
                } catch (Exception e) {
                    log.error("kline error,{}-{}-{}--",type,baseCurrency,quotaCurrency,e);
                }
            }


        } catch (Exception e) {
            log.error("get kline error", e);
        }
    }

    private Integer getGranularity(String type) {
        if ("1min".equals(type)) {
            return 60;
        }
        if ("5min".equals(type)) {
            return 300;
        }
        if ("15min".equals(type)) {
            return 900;
        }
        if ("30min".equals(type)) {
            return 1800;
        }
        if ("60min".equals(type)) {
            return 3600;
        }
        if ("1mon".equals(type)) {
            return 3600 * 24 * 30;
        }
        if ("1day".equals(type)) {
            return 3600 * 24 ;
        }
        if ("1week".equals(type)) {
            return 3600 * 24 * 7;
        }
        if ("1year".equals(type)) {
            return 3600 * 24 * 360;
        }
        return 60;
    }
}
