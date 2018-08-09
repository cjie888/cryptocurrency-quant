package com.cjie.commons.okex.open.api.service.futures.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.futures.result.OrderResult;
import com.cjie.commons.okex.open.api.bean.futures.result.OrderResult;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Futures trade api
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 19:20
 */
interface FuturesTradeAPI {

    @GET("/api/futures/v3/position")
    Call<JSONObject> getPositions();

    @GET("/api/futures/v3/{product_id}/position")
    Call<JSONObject> getProductPosition(@Path("product_id") String productId);

    @GET("/api/futures/v3/accounts")
    Call<JSONObject> getAccounts();

    @GET("/api/futures/v3/accounts/{currency}")
    Call<JSONObject> getAccountsByCurrency(@Path("currency") String currency);

    @GET("/api/futures/v3/accounts/{currency}/ledger")
    Call<JSONArray> getAccountsLedgerByCurrency(@Path("currency") String currency);

    @GET("/api/futures/v3/accounts/{product_id}/holds")
    Call<JSONObject> getAccountsHoldsByProductId(@Path("product_id") String productId);

    @POST("/api/futures/v3/order")
    Call<OrderResult> order(@Body JSONObject order);

    @POST("/api/futures/v3/orders")
    Call<JSONObject> orders(@Body JSONObject orders);

    @DELETE("/api/futures/v3/orders/{product_id}/{order_id}")
    Call<JSONObject> cancelProductOrder(@Path("product_id") String productId, @Path("order_id") String orderId);

    @DELETE("/api/futures/v3/orders/{product_id}")
    Call<JSONArray> cancelProductOrders(@Path("product_id") String productId);

    @DELETE("/api/futures/v3/close_position")
    Call<JSONObject> closePosition(@Body String close_position_data);

    @GET("/api/futures/v3/orders")
    Call<JSONObject> getOrders(@Query("currency") String currency, @Query("status") int status,
                               @Query("before") int before, @Query("after") int after, @Query("limit") int limit);

    @GET("/api/futures/v3/orders/{order_id}")
    Call<JSONObject> getOrder(@Path("order_id") String orderId);

    @GET("/api/futures/v3/fills")
    Call<JSONArray> getFills(@Query("product_id") String productId, @Query("order_id") String orderId,
                             @Query("before") int before, @Query("after") int after, @Query("limit") int limit);

    @GET("/api/futures/v3/users/self/trailing_volume")
    Call<JSONArray> getUsersSelfTrailingVolume();
}
