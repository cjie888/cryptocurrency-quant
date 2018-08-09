package com.cjie.commons.okex.open.api.service.spot;

import com.cjie.commons.okex.open.api.bean.spot.param.Order;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.bean.spot.param.Order;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderInfo;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderResult;

import java.util.List;

public interface MarginOrderAPIService {
    /**
     * 添加订单
     *
     * @param order
     * @return
     */
    OrderResult addOrder(Order order);

    /**
     * 取消订单
     *
     * @param productId
     * @param orderId
     */
    void cancleOrderByProductIdAndOrderId(String productId, Long orderId);

    /**
     * 批量取消订单
     *
     * @param productId
     */
    void cancleOrdersByProductId(String productId);

    /**
     * 查询订单
     *
     * @param productId
     * @param orderId
     * @return
     */
    OrderInfo getOrderByProductIdAndOrderId(String productId, Long orderId);

    /**
     * 订单列表
     *
     * @param productId
     * @param status    pending、done、archive
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<OrderInfo> getOrders(String productId, String status, Long before, Long after, Integer limit);

    /**
     * /* 订单列表
     *
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<OrderInfo> getPendingOrders(Long before, Long after, Integer limit);

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
    List<Fills> getFills(Long orderId, String productId, Long before, Long after, Integer limit);
}
