package com.cjie.commons.okex.open.api.service.spot.impl;

import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.spot.param.Order;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.bean.spot.param.Order;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderInfo;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderResult;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface MarginOrderAPI {
    @POST("api/margin/v3/orders")
    Call<OrderResult> addOrder(@Body Order order);

    /**
     * Cancle a order
     *
     * @param productId
     * @param orderId
     */
    @DELETE("api/margin/v3/orders/{order_id}")
    Call<JSONObject> cancleOrdersByProductIdAndOrderId(@Path("order_id") Long orderId, @Query("product_id") String productId);

    /**
     * Batch cancle order
     *
     * @param productId
     */
    @DELETE("api/margin/v3/orders")
    Call<JSONObject> cancleOrdersByProductId(@Query("product_id") String productId);

    /**
     * get a order
     *
     * @param productId
     * @param orderId
     * @return
     */
    @GET("api/margin/v3/orders/{order_id}")
    Call<OrderInfo> getOrderByProductIdAndOrderId(@Path("order_id") Long orderId, @Query("product_id") String productId);

    /**
     * get order list
     *
     * @param productId
     * @param status    pending、done、archive
     * @param before
     * @param after
     * @param limit
     * @return
     */
    @GET("api/margin/v3/orders")
    Call<List<OrderInfo>> getOrders(@Query("product_id") String productId, @Query("status") String status, @Query("before") Long before, @Query("after") Long after, @Query("limit") Integer limit);

    /**
     * get pending order list
     *
     * @param before
     * @param after
     * @param limit
     * @return
     */
    @GET("api/margin/v3/orders_pending")
    Call<List<OrderInfo>> getPendingOrders(@Query("before") Long before, @Query("after") Long after, @Query("limit") Integer limit);


    @GET("api/margin/v3/fills")
    Call<List<Fills>> getFills(@Query("order_id") Long orderId, @Query("product_id") String productId, @Query("before") Long before, @Query("after") Long after, @Query("limit") Integer limit);
}
