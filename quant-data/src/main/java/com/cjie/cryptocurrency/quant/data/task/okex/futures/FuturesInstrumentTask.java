package com.cjie.cryptocurrency.quant.data.task.okex.futures;


import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Instrument;
import com.cjie.cryptocurrency.quant.api.okex.service.futures.FuturesMarketAPIService;
import com.cjie.cryptocurrency.quant.mapper.FuturesInstrumentMapper;
import com.cjie.cryptocurrency.quant.model.FuturesInstrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FuturesInstrumentTask {

    @Autowired
    private FuturesMarketAPIService futuresMarketAPIService;

    @Autowired
    private FuturesInstrumentMapper futuresInstrumentMapper;


    @Scheduled(cron = "7 * */1 * * ?")
    public void instruments() throws Exception {
        log.info("get futures instrument  begin");
        List<Instrument> products = futuresMarketAPIService.getProducts();
        for (Instrument symbol : products) {
            if (futuresInstrumentMapper.getFuturesInstrument(symbol.getInstrument_id()) != null) {
                continue;
            }
            FuturesInstrument futuresInstrument = FuturesInstrument.builder()
                    .instrumentId(symbol.getInstrument_id())
                    .underlyingIndex(symbol.getUnderlying_index())
                    .contractVal(symbol.getContract_val())
                    .listing(symbol.getListing())
                    .delivery(symbol.getDelivery())
                    .tickSize(symbol.getTick_size())
                    .tradeIncrement(symbol.getTrade_increment())
                    .quoteCurrency(symbol.getQuote_currency())
                    .createTime(new Date()).build();
            futuresInstrumentMapper.insert(futuresInstrument);
        }
        log.info("get futures instrument  end");
    }
}
