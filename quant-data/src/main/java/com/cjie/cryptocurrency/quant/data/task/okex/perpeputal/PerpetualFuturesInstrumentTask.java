package com.cjie.cryptocurrency.quant.data.task.okex.perpeputal;


import com.cjie.cryptocurrency.quant.api.okex.bean.perpetual.result.PerputalInstrument;
import com.cjie.cryptocurrency.quant.api.okex.service.perpetual.PerpetualFuturesMarketAPIService;
import com.cjie.cryptocurrency.quant.mapper.PerpetualFuturesInstrumentMapper;
import com.cjie.cryptocurrency.quant.model.PerpetualFuturesInstrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class PerpetualFuturesInstrumentTask {

    @Autowired
    private PerpetualFuturesMarketAPIService futuresMarketAPIService;

    @Autowired
    private PerpetualFuturesInstrumentMapper futuresInstrumentMapper;

//    @Autowired
//    private FuturesKlineMapper futuresKlineMapper;


    @Scheduled(cron = "11 * */1 * * ?")
    public void instruments() throws Exception {
        log.info("get futures instrument  begin");
        List<PerputalInstrument> products = futuresMarketAPIService.getInstruments();
        for (PerputalInstrument symbol : products) {
            if (futuresInstrumentMapper.getFuturesInstrument(symbol.getInstrument_id()) != null) {
                continue;
            }
            PerpetualFuturesInstrument futuresInstrument = PerpetualFuturesInstrument.builder()
                    .instrumentId(symbol.getInstrument_id())
                    .underlyingIndex(symbol.getUnderlying_index())
                    .contractVal(symbol.getContract_val())
                    .listing(symbol.getListing())
                    .delivery(symbol.getDelivery())
                    .tickSize(symbol.getTick_size())
                    .sizeIncrement(symbol.getSize_increment())
                    .coin(symbol.getCoin())
                    .quoteCurrency(symbol.getQuote_currency())
                    .createTime(new Date()).build();
            futuresInstrumentMapper.insert(futuresInstrument);
        }
        log.info("get futures instrument  end");
    }


//    @Scheduled(cron = "3 */1 * * * ?")
//    public void kline() throws Exception {
//        log.info("get okex futures 1min kline begin");
//        getKline("1min", "");
//        log.info("get okex futures 1min kline end");
//    }
//
//    @Scheduled(cron = "7 */5 * * * ?")
//    public void kline5m() throws Exception {
//        log.info("get okex futures 5min kline begin");
//        getKline("5min", "_5m");
//        log.info("get okex futures 5min kline end");
//    }
//
//    @Scheduled(cron = "11 */15 * * * ?")
//    public void kline15m() throws Exception {
//        log.info("get okex futures 15min kline begin");
//        getKline("15min", "_15m");
//        log.info("get okex futures 15min kline end");
//    }
//
//    @Scheduled(cron = "13 */30 * * * ?")
//    public void kline30m() throws Exception {
//        log.info("get okex futures 30min kline begin");
//        getKline("30min", "_30m");
//        log.info("get okex futures 30min kline end");
//    }
//
//    @Scheduled(cron = "17 * */1 * * ?")
//    public void kline60m() throws Exception {
//        log.info("get okex futures 60min kline begin");
//        getKline("60min", "_60m");
//        log.info("get okex futures 60min kline end");
//    }
//
//    @Scheduled(cron = "19 * */23 * * ?")
//    public void kline1d() throws Exception {
//        log.info("get okex futures 1day kline begin");
//        getKline("1day", "_1d");
//        log.info("get okex futures 1day kline end");
//    }
//
//    private void getKline(String type, String suffix) {
//        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//            List<FuturesInstrument> currencies = futuresInstrumentMapper.getAllInstruments();
//            for (FuturesInstrument symbol : currencies) {
////                if (symbol.getStatus() == 0) {
////                    continue;
////                }
//                String instrumentId = symbol.getInstrumentId();
//                try {
//                    String currSuffix = suffix;
//                    if (type.equals("1min")) {
//                        currSuffix = "";
//                    }
//                    List<String[]> klines =  futuresMarketAPIService.getProductCandles(instrumentId, null, null, getGranularity(type));
//                    for (String[] data : klines.subList(1,klines.size()-1)) {
//                        if (futuresKlineMapper.getKLine(new Date(Long.valueOf(data[0])), instrumentId, currSuffix) != null) {
//                            continue;
//                        }
//                        FuturesKline kline = FuturesKline.builder().klineTime(new Date(Long.valueOf(data[0])))
//                                .instrumentId(instrumentId)
//                                .open(new BigDecimal(data[1]))
//                                .high(new BigDecimal(data[2]))
//                                .low(new BigDecimal(data[3]))
//                                .close(new BigDecimal(data[4]))
//                                .volume(new BigDecimal(data[5]))
//                                .currencyVolume(new BigDecimal(data[6]))
//                                .createTime(new Date())
//                                //.site("okex")
//                                .suffix(currSuffix)
//                                .build();
//                        //log.info("{}-{}-{}--,{}", type, baseCurrency, quotaCurrency, JSON.toJSONString(data));
//
//
//                        futuresKlineMapper.insert(kline);
//                    }
//                } catch (Exception e) {
//                    log.error("kline error,{}-{}--",type, instrumentId,e);
//                }
//            }
//
//
//        } catch (Exception e) {
//            log.error("get kline error", e);
//        }
//    }
//
//    private Integer getGranularity(String type) {
//        if ("1min".equals(type)) {
//            return 60;
//        }
//        if ("5min".equals(type)) {
//            return 300;
//        }
//        if ("15min".equals(type)) {
//            return 900;
//        }
//        if ("30min".equals(type)) {
//            return 1800;
//        }
//        if ("60min".equals(type)) {
//            return 3600;
//        }
//        if ("1mon".equals(type)) {
//            return 3600 * 24 * 30;
//        }
//        if ("1day".equals(type)) {
//            return 3600 * 24 ;
//        }
//        if ("1week".equals(type)) {
//            return 3600 * 24 * 7;
//        }
//        if ("1year".equals(type)) {
//            return 3600 * 24 * 360;
//        }
//        return 60;
//    }
}
