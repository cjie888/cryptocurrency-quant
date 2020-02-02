package com.cjie.cryptocurrency.quant.data.task.okex;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.mapper.SwapKlineMapper;
import com.cjie.cryptocurrency.quant.model.SwapKline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j(topic = "time")
public class OkexSwapKLineTask {

    @Autowired
    private SwapMarketAPIService swapMarketAPIService;
    @Autowired
    private SwapKlineMapper swapKlineMapper;

    @Scheduled(cron = "22 */10 * * * ?")
    public void kline() throws Exception {
        log.info("get okex swap 1min kline begin");
        getKline("1min", "");
        log.info("get okex swap 1min kline end");
    }

    @Scheduled(cron = "23 */20 * * * ?")
    public void kline5m() throws Exception {
        log.info("get okex swap 5min kline begin");
        getKline("5min", "_5m");
        log.info("get okex swap 5min kline end");

    }
    @Scheduled(cron = "16 */30 * * * ?")
    public void kline15m() throws Exception {
        log.info("get okex swap 15min kline begin");
        getKline("15min", "_15m");
        log.info("get okex swap 15min kline end");

    }

    @Scheduled(cron = "26 */21 * * * ?")
    public void kline30m() throws Exception {
        log.info("get okex swap 30min kline begin");
        getKline("30min", "_30m");
        log.info("get okex swap 30min kline end");

    }

    @Scheduled(cron = "18 */42 * * * ?")
    public void kline60m() throws Exception {
        log.info("get okex swap 60min kline begin");
        getKline("60min", "_60m");
        log.info("get okex swap 60min kline end");

    }
    @Scheduled(cron = "38 7 */9 * * ?")
    public void kline1month() throws Exception {
        log.info("get okex swap 1month kline begin");
        getKline("1mon", "_1m");
        log.info("get okex 1mon kline end");

    }

    @Scheduled(cron = "18 13 */2 * * ?")
    public void kline1day() throws Exception {
        log.info("get okex swap 1day kline begin");
        getKline("1day", "_1d");
        log.info("get okex swap 1day kline end");

    }

    @Scheduled(cron = "18 17 */6 * * ?")
    public void kline1week() throws Exception {
        log.info("get okex swap 1week kline begin");
        getKline("1week", "_1w");
        log.info("get okex swap 1week kline end");

    }
    @Scheduled(cron = "18 22 4 */5 * ?")
    public void kline1eya() throws Exception {
        log.info("get okex swap 1yeay kline begin");
        getKline("1year", "_1y");
        log.info("get okex swap 1year kline end");

    }
    private void getKline(String type, String suffix) {
        List<String> instrumentIds = new ArrayList<>();
        instrumentIds.add("BTC-USD-SWAP");
        instrumentIds.add("ETH-USD-SWAP");
        instrumentIds.add("EOS-USD-SWAP");
        instrumentIds.add("LTC-USD-SWAP");
        instrumentIds.add("XRP-USD-SWAP");
        instrumentIds.add("BCH-USD-SWAP");
        instrumentIds.add("BSV-USD-SWAP");
        instrumentIds.add("ETC-USD-SWAP");

        instrumentIds.add("BTC-USDT-SWAP");
        instrumentIds.add("ETH-USDT-SWAP");
        instrumentIds.add("EOS-USDT-SWAP");
        instrumentIds.add("LTC-USDT-SWAP");
        instrumentIds.add("XRP-USDT-SWAP");
        instrumentIds.add("BCH-USDT-SWAP");
        instrumentIds.add("BSV-USDT-SWAP");
        instrumentIds.add("ETC-USDT-SWAP");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
              for (String instrumentId : instrumentIds) {

                try {
                    String currSuffix = suffix;
                    int count = 0;
                    String klineS = swapMarketAPIService.getCandlesApi(instrumentId, null, null, String.valueOf(getGranularity(type)));
                    List<String[]> apiKlineVOs = JSON.parseObject(klineS, new TypeReference<List<String[]>>(){});
                    for (String[] apiKlineVO : apiKlineVOs) {
                        String time = apiKlineVO[0];
                        if (count++ < 2) {
                            continue;
                        }
                        if (swapKlineMapper.getKLine(dateFormat.parse(time),
                                instrumentId, currSuffix) != null) {
                            continue;
                        }
                        SwapKline kline = SwapKline.builder().klineTime(dateFormat.parse(time))
                                //.amount(BigDecimal.ZERO)
                                //.count(0)
                                //.baseCurrency(baseCurrency)
                                //.quotaCurrency(quotaCurrency)
                                .instrumentId(instrumentId)
                                .open(new BigDecimal(apiKlineVO[1]))
                                .close(new BigDecimal(apiKlineVO[4]))
                                .high(new BigDecimal(apiKlineVO[2]))
                                .low(new BigDecimal(apiKlineVO[3]))
                                .volume(new BigDecimal(apiKlineVO[5]))
                                .currencyVolume(new BigDecimal(apiKlineVO[6]))
                                //.site("okex")
                                .suffix(currSuffix)
                                .build();
                        //log.info("{}-{}-{}--,{}", type, baseCurrency, quotaCurrency, JSON.toJSONString(data));


                        swapKlineMapper.insert(kline);
                    }
                } catch (Exception e) {
                    log.error("swap kline error,{}-{}--",type,instrumentId,e);
                }
            }


        } catch (Exception e) {
            log.error("get swap kline error", e);
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
