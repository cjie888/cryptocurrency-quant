package com.cjie.commons.okex.open.api.task;


import com.cjie.commons.okex.open.api.bean.spot.result.Product;
import com.cjie.commons.okex.open.api.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPairMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OkexCurrencyPairTask {

    @Autowired
    private SpotProductAPIService spotProductAPIService;

    @Autowired
    private CurrencyPairMapper currencyPairMapper;

    @Scheduled(cron = "17 25 */2 * * ?")
    //@Scheduled(cron = "1 * * * * ?")
    public void currencyPair() throws Exception {
        log.info("get okex currency pair begin");
        List<Product> products = spotProductAPIService.getProducts("okex");
        for (Product symbol : products) {
            String baseCurrency = symbol.getBase_currency();
            String quotaCurrency = symbol.getQuote_currency();
            if (currencyPairMapper.getCurrencyPair(baseCurrency, quotaCurrency, "okex") != null) {
                continue;
            }
            CurrencyPair currencyPair = CurrencyPair.builder()
                    .baseCurrency(baseCurrency).quotaCurrency(quotaCurrency)
                    .createTime(new Date()).site("okex").build();
            currencyPairMapper.insert(currencyPair);
        }

        log.info("get okex currency pair end");
    }
}
