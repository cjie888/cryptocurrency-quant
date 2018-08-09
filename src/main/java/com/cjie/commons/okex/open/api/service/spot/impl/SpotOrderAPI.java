package com.cjie.commons.okex.open.api.service.spot.impl;

import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.spot.param.PlaceOrderParam;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderInfo;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderResult;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface SpotOrderAPI {

    @POST("api/spot/v3/orders")
    Call<OrderResult> addOrder(@Body PlaceOrderParam order);

    @HTTP(method = "DELETE", path = "api/spot/v3/orders/{order_id}", hasBody = true)
    Call<OrderResult> cancleOrderByOrderId(@Path("order_id") Long orderId, @Body PlaceOrderParam order);

    @HTTP(method = "DELETE", path = "api/spot/v3/orders", hasBody = true)
    Call<JSONObject> cancleOrders(@Body PlaceOrderParam order);

    @GET("api/spot/v3/orders/{order_id}")
    Call<OrderInfo> getOrderByOrderId(@Path("order_id") Long orderId, @Query("product_id") String productId);

    @GET("api/spot/v3/orders")
    Call<List<OrderInfo>> getOrders(@Query("product_id") String productId, @Query("status") String status, @Query("before") Long before, @Query("after") Long after, @Query("limit") Integer limit);

    @GET("api/spot/v3/orders_pending")
    Call<List<OrderInfo>> getPendingOrders(@Query("before") Long before, @Query("after") Long after, @Query("limit") Integer limit);

    @GET("api/spot/v3/fills")
    Call<List<Fills>> getFills(@Query("order_id") Long order_id, @Query("product_id") String product_id, @Query("before") Long before, @Query("after") Long after, @Query("limit") Integer limit);

}
