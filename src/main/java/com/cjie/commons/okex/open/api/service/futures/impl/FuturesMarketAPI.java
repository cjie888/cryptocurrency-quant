package com.cjie.commons.okex.open.api.service.futures.impl;

import com.alibaba.fastjson.JSONArray;
import com.cjie.commons.okex.open.api.bean.futures.result.*;
import com.cjie.commons.okex.open.api.bean.futures.result.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * Futures market api
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/8 20:51
 */
interface FuturesMarketAPI {

    @GET("/api/futures/v3/time")
    Call<ServerTime> getServerTime();

    @GET("/api/futures/v3/exchange_rate")
    Call<ExchangeRate> getExchangeRate();

    @GET("/api/futures/v3/products")
    Call<List<Products>> getProducts();

    @GET("/api/futures/v3/products/currencies")
    Call<List<Currencies>> getCurrencies();

    @GET("/api/futures/v3/products/{product_id}/book")
    Call<Book> getProductBook(@Path("product_id") String productId, @Query("depth") Integer depth, @Query("conflated") Integer conflated);

    @GET("/api/futures/v3/products/{product_id}/ticker")
    Call<Ticker> getProductTicker(@Path("product_id") String productId);

    @GET("/api/futures/v3/products/{product_id}/trades")
    Call<List<Trades>> getProductTrades(@Path("product_id") String productId);

    @GET("/api/futures/v3/products/{product_id}/candles")
    Call<JSONArray> getProductCandles(@Path("product_id") String productId, @Query("start") String start, @Query("end") String end, @Query("granularity") String granularity);

    @GET("/api/futures/v3/products/{product_id}/index")
    Call<Index> getProductIndex(@Path("product_id") String productId);

    @GET("/api/futures/v3/products/{product_id}/estimated_price")
    Call<EstimatedPrice> getProductEstimatedPrice(@Path("product_id") String productId);

    @GET("/api/futures/v3/products/{product_id}/holds")
    Call<Holds> getProductHolds(@Path("product_id") String productId);

    @GET("/api/futures/v3/products/{product_id}/price_limit")
    Call<PriceLimit> getProductPriceLimit(@Path("product_id") String productId);

    @GET("/api/futures/v3/products/{product_id}/liquidation")
    Call<List<Liquidation>> getProductLiquidation(@Path("product_id") String productId, @Query("status") int status);
}
