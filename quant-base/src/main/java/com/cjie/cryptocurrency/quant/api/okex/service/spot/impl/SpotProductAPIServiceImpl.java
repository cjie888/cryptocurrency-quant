package com.cjie.cryptocurrency.quant.api.okex.service.spot.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Book;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Product;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Book;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Product;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.CurrencyKlineDTO;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.api.okex.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Product api
 *
 * @author
 * @create 2018-04-18 下午5:18
 **/
@Component
@Slf4j
public class SpotProductAPIServiceImpl extends BaseServiceImpl implements SpotProductAPIService {

    private ConcurrentHashMap<String, SpotProductAPI> spotProductAPIs = new ConcurrentHashMap<>();


    public SpotProductAPI getSpotProductApi(String site, APIClient apiClient) {
        SpotProductAPI spotProductAPI = spotProductAPIs.get(site);
        if (spotProductAPI != null) {
            return  spotProductAPI;
        }
        spotProductAPI = apiClient.createService(SpotProductAPI.class);
        spotProductAPIs.put(site, spotProductAPI);
        return spotProductAPI;
    }

    @Override
    public Ticker getTickerByProductId(String site, final String productId) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotProductAPI spotProductAPI = getSpotProductApi(site, apiClient);
        return apiClient.executeSync(spotProductAPI.getTickerByProductId(productId));
    }

    @Override
    public List<Ticker> getTickers(String site) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotProductAPI spotProductAPI = getSpotProductApi(site, apiClient);
        return apiClient.executeSync(spotProductAPI.getTickers());
    }

    @Override
    public Book bookProductsByProductId(String site, final String productId, final Integer size, final BigDecimal depth) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotProductAPI spotProductAPI = getSpotProductApi(site, apiClient);
        return apiClient.executeSync(spotProductAPI.bookProductsByProductId(productId, size, depth));
    }

    @Override
    public List<Product> getProducts(String site) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotProductAPI spotProductAPI = getSpotProductApi(site, apiClient);
        return apiClient.executeSync(spotProductAPI.getProducts());
    }

    @Override
    public List<Trade> getTrades(String site, final String productId, final Integer before, final Integer after, final Integer limit) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotProductAPI spotProductAPI = getSpotProductApi(site, apiClient);
        return apiClient.executeSync(spotProductAPI.getTrades(productId, before, after, limit));
    }

    @Override
    public List<CurrencyKlineDTO> getCandles(String site, final String product_id, final Integer granularity, final String start, final String end) throws Exception {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Referer", "https://www.coinall.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url = "https://www.okex.com/api/spot/v3/products/" + product_id + "/candles?granularity=" + granularity;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        List<List<String>> klines =  JSON.parseObject(body,new TypeReference<List<List<String>>>(){});
        List<CurrencyKlineDTO> currencyKlineDTOS = new ArrayList<>();
        for(List<String> kline : klines) {
            CurrencyKlineDTO currencyKlineDTO = new CurrencyKlineDTO();
            currencyKlineDTO.setTime(String.valueOf(DateUtils.parseUTCTime(kline.get(0)).getTime()));
            currencyKlineDTO.setOpen(kline.get(1));
            currencyKlineDTO.setHigh(kline.get(2));
            currencyKlineDTO.setLow(kline.get(3));
            currencyKlineDTO.setClose(kline.get(4));
            currencyKlineDTOS.add(currencyKlineDTO);

        }
        return currencyKlineDTOS;
    }

}
