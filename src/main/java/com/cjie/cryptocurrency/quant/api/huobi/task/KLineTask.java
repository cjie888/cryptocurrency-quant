package com.cjie.cryptocurrency.quant.api.huobi.task;


import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import com.cjie.cryptocurrency.quant.mapper.CurrencyKlineMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyKline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class KLineTask {

    @Autowired
    private CurrencyKlineMapper currencyKlineMapper;

    @Scheduled(cron = "3 * * * * ?")
    public void kline() throws Exception {
        try {
            HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
            HuobiApiRestClient client = factory.newRestClient();
            List<HuobiKLineData> list = client.kline("btcusdt", "1min", 1);
            for (HuobiKLineData data : list) {
                CurrencyKline kline = CurrencyKline.builder().klineTime(new Date(data.getId()*1000))
                        .amount(new BigDecimal(data.getAmount()))
                        .count(data.getCount())
                        .baseCurrency("btc")
                        .quotaCurrency("usdt")
                        .open(new BigDecimal(data.getOpen()))
                        .close(new BigDecimal(data.getOpen()))
                        .high(new BigDecimal(data.getOpen()))
                        .low(new BigDecimal(data.getOpen()))
                        .vol(new BigDecimal(data.getVol()))
                        .site("huobi")
                        .build();
                currencyKlineMapper.insert(kline);

                log.info("btc---" + data);
            }

            List<HuobiKLineData> list2 = client.kline("ethusdt", "1min", 1);
            for (HuobiKLineData data : list2) {
                CurrencyKline kline = CurrencyKline.builder().klineTime(new Date(data.getId()*1000))
                        .amount(new BigDecimal(data.getAmount()))
                        .count(data.getCount())
                        .baseCurrency("eth")
                        .quotaCurrency("usdt")
                        .open(new BigDecimal(data.getOpen()))
                        .close(new BigDecimal(data.getOpen()))
                        .high(new BigDecimal(data.getOpen()))
                        .low(new BigDecimal(data.getOpen()))
                        .vol(new BigDecimal(data.getVol()))
                        .site("huobi")
                        .build();
                currencyKlineMapper.insert(kline);
                log.info("eth---" + data);
            }
        } catch (Exception e) {
            log.error("get kline error", e);
        }
    }
}
