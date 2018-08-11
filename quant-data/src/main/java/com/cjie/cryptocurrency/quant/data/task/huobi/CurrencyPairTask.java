package com.cjie.cryptocurrency.quant.data.task.huobi;


import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPairMapper;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPairMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyPair;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPairMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class CurrencyPairTask {

    @Autowired
    private CurrencyPairMapper currencyPairMapper;

    @Scheduled(cron = "5 26 */2 * * ?")
    //@Scheduled(cron = "1 * * * * ?")
    public void currencyPair() throws Exception {
        log.info("get huobi currency pair begin");
        HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
        HuobiApiRestClient client = factory.newRestClient();
        List<HuobiSymbol> symbols  = client.symbols();
        for (HuobiSymbol symbol : symbols) {
            String baseCurrency = symbol.getBaseCurrency();
            String quotaCurrency = symbol.getQuoteCurrency();
            if (currencyPairMapper.getCurrencyPair(baseCurrency, quotaCurrency, "huobi") != null) {
                continue;
            }
            CurrencyPair currencyPair = CurrencyPair.builder()
                    .baseCurrency(baseCurrency).quotaCurrency(quotaCurrency)
                    .createTime(new Date()).site("huobi").build();

            currencyPairMapper.insert(currencyPair);
        }

        log.info("get huobi currency pair end");
    }
}
