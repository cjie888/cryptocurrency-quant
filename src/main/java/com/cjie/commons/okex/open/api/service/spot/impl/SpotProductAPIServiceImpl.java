package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.result.Book;
import com.cjie.commons.okex.open.api.bean.spot.result.Product;
import com.cjie.commons.okex.open.api.bean.spot.result.Ticker;
import com.cjie.commons.okex.open.api.bean.spot.result.Trade;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.service.BaseServiceImpl;
import com.cjie.commons.okex.open.api.bean.spot.result.Book;
import com.cjie.commons.okex.open.api.bean.spot.result.Product;
import com.cjie.commons.okex.open.api.bean.spot.result.Ticker;
import com.cjie.commons.okex.open.api.bean.spot.result.Trade;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.BaseServiceImpl;
import com.cjie.commons.okex.open.api.service.spot.SpotProductAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    public List<String[]> getCandles(String site, final String product_id, final Integer granularity, final String start, final String end) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotProductAPI spotProductAPI = getSpotProductApi(site, apiClient);
        return apiClient.executeSync(spotProductAPI.getCandles(product_id, granularity, start, end));
    }

}
