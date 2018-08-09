package com.cjie.cryptocurrency.quant.api.huobi.task;


import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import com.cjie.cryptocurrency.quant.mapper.CurrencyKlineMapper;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPairMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyPair;
import com.cjie.cryptocurrency.quant.model.CurrencyKline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

//@Component
@Slf4j
public class KLineTask {

    @Autowired
    private CurrencyKlineMapper currencyKlineMapper;

    @Autowired
    private CurrencyPairMapper currencyPairMapper;

    @Scheduled(cron = "5 * * * * ?")
    public void kline() throws Exception {
        log.info("get huobi 1min kline begin");
        getKline("1min", "");
        log.info("get huobi 1min kline end");
    }

    @Scheduled(cron = "4 */5 * * * ?")
    public void kline5m() throws Exception {
        log.info("get huobi 5min kline begin");
        getKline("5min", "_5m");
        log.info("get huobi 5min kline end");

    }
    @Scheduled(cron = "6 */9 * * * ?")
    public void kline15m() throws Exception {
        log.info("get huobi 15min kline begin");
        getKline("15min", "_15m");
        log.info("get huobi 15min kline end");

    }

    @Scheduled(cron = "6 */21 * * * ?")
    public void kline30m() throws Exception {
        log.info("get huobi 30min kline begin");
        getKline("30min", "_30m");
        log.info("get huobi 30min kline end");

    }

    @Scheduled(cron = "8 */42 * * * ?")
    public void kline60m() throws Exception {
        log.info("get huobi 60min kline begin");
        getKline("60min", "_60m");
        log.info("get huobi 60min kline end");

    }
    @Scheduled(cron = "8 7 */9 * * ?")
    public void kline1month() throws Exception {
        log.info("get huobi 1month kline begin");
        getKline("1mon", "_1m");
        log.info("get huobi 1mon kline end");

    }

    @Scheduled(cron = "8 13 */2 * * ?")
    public void kline1day() throws Exception {
        log.info("get huobi 1day kline begin");
        getKline("1day", "_1d");
        log.info("get huobi 1day kline end");

    }

    @Scheduled(cron = "8 17 */6 * * ?")
    public void kline1week() throws Exception {
        log.info("get huobi 1week kline begin");
        getKline("1week", "_1w");
        log.info("get huobi 1week kline end");

    }
    @Scheduled(cron = "8 22 4 */5 * ?")
    public void kline1eya() throws Exception {
        log.info("get huobi 1yeay kline begin");
        getKline("1year", "_1y");
        log.info("get huobi 1year kline end");

    }
    private void getKline(String type, String suffix) {
        try {
            HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
            HuobiApiRestClient client = factory.newRestClient();
            List<CurrencyPair> currencies = currencyPairMapper.getAllCurrency("huobi");
            for (CurrencyPair symbol : currencies) {
                String baseCurrency = symbol.getBaseCurrency();
                String quotaCurrency = symbol.getQuotaCurrency();
                try {
                    List<HuobiKLineData> list = client.kline(baseCurrency + quotaCurrency, type, 5);
                    for (HuobiKLineData data : list) {
                        if (currencyKlineMapper.getCurrencyLine(new Date(data.getId() * 1000),
                                baseCurrency, quotaCurrency, "huobi", suffix) != null) {
                            continue;
                        }
                        CurrencyKline kline = CurrencyKline.builder().klineTime(new Date(data.getId() * 1000))
                                .amount(new BigDecimal(data.getAmount()))
                                .count(data.getCount())
                                .baseCurrency(baseCurrency)
                                .quotaCurrency(quotaCurrency)
                                .open(new BigDecimal(data.getOpen()))
                                .close(new BigDecimal(data.getOpen()))
                                .high(new BigDecimal(data.getOpen()))
                                .low(new BigDecimal(data.getOpen()))
                                .vol(new BigDecimal(data.getVol()))
                                .site("huobi")
                                .suffix(suffix)
                                .build();
                        log.info("{}-{}-{}--,{}", type, baseCurrency, quotaCurrency, data);


                        currencyKlineMapper.insert(kline);
                    }
                } catch (Exception e) {
                    log.error("kline error,{}-{}-{}--",type,baseCurrency,quotaCurrency,e);
                }
            }


        } catch (Exception e) {
            log.error("get kline error", e);
        }
    }
}
