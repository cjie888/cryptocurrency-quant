package com.cjie.cryptocurrency.quant.data.task.okex.perpeputal;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.mapper.SwapKlineMapper;
import com.cjie.cryptocurrency.quant.model.SwapKline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j(topic = "time")
public class OkexSwapHistoryKLineTask {

    @Autowired
    private SwapMarketAPIService swapMarketAPIService;
    @Autowired
    private SwapKlineMapper swapKlineMapper;

    @Scheduled(cron = "52 */10 * * * ?")
    public void kline() throws Exception {
        log.info("get okex swap history 1min kline begin");
        getKline("1min", "");
        log.info("get okex swap history  1min kline end");
    }

    @Scheduled(cron = "33 */20 * * * ?")
    public void kline5m() throws Exception {
        log.info("get okex swap history 5min kline begin");
        getKline("5min", "_5m");
        log.info("get okex swap history 5min kline end");

    }

    @Scheduled(cron = "46 */30 * * * ?")
    public void kline15m() throws Exception {
        log.info("get okex swap history 15min kline begin");
        getKline("15min", "_15m");
        log.info("get okex swap history 15min kline end");

    }

    @Scheduled(cron = "16 */21 * * * ?")
    public void kline30m() throws Exception {
        log.info("get okex swap  history 30min kline begin");
        getKline("30min", "_30m");
        log.info("get okex swap history 30min kline end");

    }

    @Scheduled(cron = "18 */42 * * * ?")
    public void kline60m() throws Exception {
        log.info("get okex swap history 60min kline begin");
        getKline("60min", "_60m");
        log.info("get okex swap history 60min kline end");

    }

    @Scheduled(cron = "38 7 */9 * * ?")
    public void kline1month() throws Exception {
        log.info("get okex swap history 1month kline begin");
        getKline("1mon", "_1m");
        log.info("get okex 1mon history kline end");

    }

    @Scheduled(cron = "18 13 */2 * * ?")
    public void kline1day() throws Exception {
        log.info("get okex swap  history 1day kline begin");
        getKline("1day", "_1d");
        log.info("get okex swap history 1day kline end");

    }

    @Scheduled(cron = "18 17 */6 * * ?")
    public void kline1week() throws Exception {
        log.info("get okex swap history 1week kline begin");
        getKline("1week", "_1w");
        log.info("get okex swap history 1week kline end");

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
                    SwapKline minKline = swapKlineMapper.getMinKLine(
                            instrumentId, currSuffix);
                    if (minKline == null) {
                        continue;
                    }
                    int count = 0;
                    Date endTime =  new Date(minKline.getKlineTime().getTime() + 60L * 1000 * 10 * getMintutes(type));
                    String klineS = getCandlesApi(instrumentId,
                           endTime, String.valueOf(getGranularity(type)));
                    List<String[]> apiKlineVOs = JSON.parseObject(klineS, new TypeReference<List<String[]>>() {
                    });
                    if (currSuffix.equals("")) {
                        log.info("get swap history {} -{}-{}", instrumentId, endTime, klineS);
                    }
                    for (String[] apiKlineVO : apiKlineVOs) {
                        String time = apiKlineVO[0];
                        if (count++ < 1) {
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
                    log.error("swap history kline error,{}-{}--", type, instrumentId, e);
                }
            }


        } catch (Exception e) {
            log.error("get swap kline error", e);
        }
    }

    private Integer getMintutes(String type) {
        if ("1min".equals(type)) {
            return 1;
        }
        if ("5min".equals(type)) {
            return 5;
        }
        if ("15min".equals(type)) {
            return 15;
        }
        if ("30min".equals(type)) {
            return 30;
        }
        if ("60min".equals(type)) {
            return 60;
        }
        if ("1mon".equals(type)) {
            return 60 * 24 * 30;
        }
        if ("1day".equals(type)) {
            return 60 * 24;
        }
        if ("1week".equals(type)) {
            return 60 * 24 * 7;
        }
        if ("1year".equals(type)) {
            return 60 * 24 * 360;
        }
        return 60;
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
            return 3600 * 24;
        }
        if ("1week".equals(type)) {
            return 3600 * 24 * 7;
        }
        if ("1year".equals(type)) {
            return 3600 * 24 * 360;
        }
        return 60;
    }


    public String getCandlesApi(String instrument,Date endDate, String type) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Referer", "https://www.okex.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url = "https://www.okex.com/v2/perpetual/pc/public/instruments/" + instrument + "/candles?granularity=" + type + "&size=1000&t=" + endDate.getTime();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
        JSONObject result =  JSON.parseObject(body);
        return result.getString("data");


        //return spotProductAPIService.getTickerByProductId(baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase());
    }
}
