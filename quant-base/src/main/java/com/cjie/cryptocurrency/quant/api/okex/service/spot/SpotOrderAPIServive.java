package com.cjie.cryptocurrency.quant.api.okex.service.spot;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Fills;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Fills;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Fills;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;

import java.util.List;

public interface SpotOrderAPIServive {
    /**
     * 添加订单
     *
     * @param order
     * @return
     */
    OrderResult addOrder(String site, PlaceOrderParam order);

    /**
     * 取消单个订单
     *  @param order
     * @param orderId
     */
    OrderResult cancleOrderByOrderId(String site, final PlaceOrderParam order, Long orderId);

    /**
     * 批量取消订单
     * @param order
     */
    JSONObject cancleOrders(String site, final PlaceOrderParam order);

    /**
     * 单个订单
     * @param productId
     * @param orderId
     * @return
     */
    OrderInfo getOrderByOrderId(String site, String productId, Long orderId);

    /**
     * 订单列表
     *
     * @param productId
     * @param status pending、done、archive
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<OrderInfo> getOrders(String site, String productId, String status, Long before, Long after, Integer limit);

    /**
     * 订单列表
     *
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<OrderInfo> getPendingOrders(String site, Long before, Long after, Integer limit);

    /**
     * 账单列表
     *
     * @param orderId
     * @param productId
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<Fills> getFills(String site, Long orderId, String productId, Long before, Long after, Integer limit);
}
