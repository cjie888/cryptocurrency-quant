package com.cjie.commons.okex.open.api.service.futures.impl;

import com.alibaba.fastjson.JSONArray;
import com.cjie.commons.okex.open.api.bean.futures.result.Book;
import com.cjie.commons.okex.open.api.bean.futures.result.Liquidation;
import com.cjie.commons.okex.open.api.bean.futures.result.Products;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.service.futures.FuturesMarketAPIService;
import com.cjie.commons.okex.open.api.bean.futures.result.*;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.futures.FuturesMarketAPIService;

import java.util.List;

/**
 * Futures market api
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 16:09
 */
public class FuturesMarketAPIServiceImpl implements FuturesMarketAPIService {

    private APIClient client;
    private FuturesMarketAPI api;

    public FuturesMarketAPIServiceImpl(APIConfiguration config) {
        this.client = new APIClient(config);
        this.api = client.createService(FuturesMarketAPI.class);
    }

    @Override
    public List<Products> getProducts() {
        return this.client.executeSync(this.api.getProducts());
    }

    @Override
    public List<Currencies> getCurrencies() {
        return this.client.executeSync(this.api.getCurrencies());
    }

    @Override
    public Book getProductBook(String productId, Integer depth, Integer conflated) {
        return this.client.executeSync(this.api.getProductBook(productId, depth, conflated));
    }

    @Override
    public Ticker getProductTicker(String productId) {
        return this.client.executeSync(this.api.getProductTicker(productId));
    }

    @Override
    public List<Trades> getProductTrades(String productId) {
        return this.client.executeSync(this.api.getProductTrades(productId));
    }

    @Override
    public JSONArray getProductCandles(String productId, long start, long end, long granularity) {
        return this.client.executeSync(this.api.getProductCandles(productId, String.valueOf(start), String.valueOf(end), String.valueOf(granularity)));
    }

    @Override
    public Index getProductIndex(String productId) {
        return this.client.executeSync(this.api.getProductIndex(productId));
    }

    @Override
    public EstimatedPrice getProductEstimatedPrice(String productId) {
        return this.client.executeSync(this.api.getProductEstimatedPrice(productId));
    }

    @Override
    public Holds getProductHolds(String productId) {
        return this.client.executeSync(this.api.getProductHolds(productId));
    }

    @Override
    public PriceLimit getProductPriceLimit(String productId) {
        return this.client.executeSync(this.api.getProductPriceLimit(productId));
    }

    @Override
    public List<Liquidation> getProductLiquidation(String productId, int status) {
        return this.client.executeSync(this.api.getProductLiquidation(productId, status));
    }

}
