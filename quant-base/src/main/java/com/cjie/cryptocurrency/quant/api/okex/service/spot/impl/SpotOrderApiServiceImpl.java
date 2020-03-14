package com.cjie.cryptocurrency.quant.api.okex.service.spot.impl;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Fills;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Fills;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotOrderAPIServive;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Fills;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * order api
 *
 * @author
 * @create 2018-04-18 下午5:03
 **/
@Component
@Slf4j
public class SpotOrderApiServiceImpl extends BaseServiceImpl implements SpotOrderAPIServive {
    private ConcurrentHashMap<String, SpotOrderAPI> spotOrderAPIs = new ConcurrentHashMap<>();


    public SpotOrderAPI getSpotOrderApi(String site, APIClient apiClient) {
        SpotOrderAPI spotProductAPI = spotOrderAPIs.get(site);
        if (spotProductAPI != null) {
            return  spotProductAPI;
        }
        spotProductAPI = apiClient.createService(SpotOrderAPI.class);
        spotOrderAPIs.put(site, spotProductAPI);
        return spotProductAPI;
    }

    @Override
    public OrderResult addOrder(String site, final PlaceOrderParam order) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        return apiClient.executeSync(spotOrderAPI.addOrder(order));
    }

    @Override
    public OrderResult cancleOrderByOrderId(String site, final String instrumentId, final Long orderId) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        PlaceOrderParam placeOrderParam = new PlaceOrderParam();
        placeOrderParam.setInstrument_id(instrumentId);
        return apiClient.executeSync(spotOrderAPI.cancleOrderByOrderId(orderId, placeOrderParam));
    }

    @Override
    public JSONObject cancleOrders(String site, final PlaceOrderParam order) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        return apiClient.executeSync(spotOrderAPI.cancleOrders(order));
    }

    @Override
    public OrderInfo getOrderByOrderId(String site, final String productId, final Long orderId) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        return apiClient.executeSync(spotOrderAPI.getOrderByOrderId(orderId, productId));
    }

    @Override
    public List<OrderInfo> getOrders(String site, final String productId, final String status, final Long before, final Long after, final Integer limit) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        return apiClient.executeSync(spotOrderAPI.getOrders(productId, status, before, after, limit));
    }

    @Override
    public List<OrderInfo> getPendingOrders(String site, final Long before, final Long after, final Integer limit) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        return apiClient.executeSync(spotOrderAPI.getPendingOrders(before, after, limit));
    }

    @Override
    public List<Fills> getFills(String site, final Long orderId, final String productId, final Long before, final Long after, final Integer limit) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotOrderAPI spotOrderAPI = getSpotOrderApi(site, apiClient);
        return apiClient.executeSync(spotOrderAPI.getFills(orderId, productId, before, after, limit));
    }

}
