package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.result.Book;
import com.cjie.commons.okex.open.api.bean.spot.result.Product;
import com.cjie.commons.okex.open.api.bean.spot.result.Ticker;
import com.cjie.commons.okex.open.api.bean.spot.result.Trade;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.math.BigDecimal;
import java.util.List;

public interface SpotProductAPI {

    @GET("/api/spot/v3/products")
    Call<List<Product>> getProducts();

    @GET("/api/spot/v3/products/{product_id}/book")
    Call<Book> bookProductsByProductId(@Path("product_id") String productId, @Query("size") Integer size, @Query("depth") BigDecimal depth);

    @GET("/api/spot/v3/products/ticker")
    Call<List<Ticker>> getTickers();

    @GET("/api/spot/v3/products/{product_id}/ticker")
    Call<Ticker> getTickerByProductId(@Path("product_id") String productId);


    @GET("/api/spot/v3/products/{product_id}/trades")
    Call<List<Trade>> getTrades(@Path("product_id") String productId, @Query("before") Integer before, @Query("after") Integer after, @Query("limit") Integer limit);

    @GET("/api/spot/v3/products/{product_id}/candles")
    Call<List<String[]>> getCandles(@Path("product_id") String productId, @Query("granularity") Integer granularity, @Query("start") String start, @Query("end") String end);

}
