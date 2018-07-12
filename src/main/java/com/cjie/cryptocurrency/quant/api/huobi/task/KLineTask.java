package com.cjie.cryptocurrency.quant.api.huobi.task;


import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
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

    @Scheduled(cron = "5 * * * * ?")
    public void kline() throws Exception {
        try {
            HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
            HuobiApiRestClient client = factory.newRestClient();
            List<HuobiSymbol> symbols  = client.symbols();
            for (HuobiSymbol symbol : symbols) {
                String baseCurrency = symbol.getBaseCurrency();
                String quotaCurrency = symbol.getQuoteCurrency();
                try {
                    List<HuobiKLineData> list = client.kline(baseCurrency + quotaCurrency, "1min", 10);
                    for (HuobiKLineData data : list) {
                        if (currencyKlineMapper.getCurrencyLine(new Date(data.getId() * 1000),
                                baseCurrency, quotaCurrency, "huobi") != null) {
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
                                .build();
                        log.info("{}-{}--,{}", baseCurrency, quotaCurrency, data);


                        currencyKlineMapper.insert(kline);
                    }
                } catch (Exception e) {
                    log.error("kline error,{}-{}--",baseCurrency,quotaCurrency,e);
                }
            }


        } catch (Exception e) {
            log.error("get kline error", e);
        }
    }
}
