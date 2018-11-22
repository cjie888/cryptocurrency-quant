package com.cjie.cryptocurrency.quant.api.okex.service.futures.impl;

import com.alibaba.fastjson.JSONArray;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Index;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Trades;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.*;
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

    @GET("/api/futures/v3/instruments")
    Call<List<Instrument>> getProducts();

    @GET("/api/futures/v3/instruments/currencies")
    Call<List<Currencies>> getCurrencies();

    @GET("/api/futures/v3/instruments/{instrument_id}/book")
    Call<Book> getProductBook(@Path("instrument_id") String productId, @Query("depth") Integer depth, @Query("conflated") Integer conflated);

    @GET("/api/futures/v3/instruments/{instrument_id}/ticker")
    Call<Ticker> getProductTicker(@Path("instrument_id") String productId);

    @GET("/api/futures/v3/instruments/{instrument_id}/trades")
    Call<List<Trades>> getProductTrades(@Path("product_id") String productId);

    @GET("/api/futures/v3/instruments/{instrument_id}/candles")
    Call<JSONArray> getProductCandles(@Path("instrument_id") String productId, @Query("start") String start, @Query("end") String end, @Query("granularity") String granularity);

    @GET("/api/futures/v3/instruments/{instrument_id}/index")
    Call<Index> getProductIndex(@Path("instrument_id") String productId);

    @GET("/api/futures/v3/instruments/{instrument_id}/estimated_price")
    Call<EstimatedPrice> getProductEstimatedPrice(@Path("instrument_id") String productId);

    @GET("/api/futures/v3/instruments/{instrument_id}/holds")
    Call<Holds> getProductHolds(@Path("instrument_id") String productId);

    @GET("/api/futures/v3/instruments/{instrument_id}/price_limit")
    Call<PriceLimit> getProductPriceLimit(@Path("instrument_id") String productId);

    @GET("/api/futures/v3/instruments/{instrument_id}/liquidation")
    Call<List<Liquidation>> getProductLiquidation(@Path("instrument_id") String productId, @Query("status") int status);
}
