package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.param.Order;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.bean.spot.param.Order;
import com.cjie.commons.okex.open.api.bean.spot.result.Fills;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderInfo;
import com.cjie.commons.okex.open.api.bean.spot.result.OrderResult;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.spot.MarginOrderAPIService;

import java.util.List;

public class MarginOrderAPIServiceImpl implements MarginOrderAPIService {
    private final APIClient client;
    private final MarginOrderAPI marginOrderAPI;

    public MarginOrderAPIServiceImpl(final APIConfiguration config) {
        this.client = new APIClient(config);
        this.marginOrderAPI = this.client.createService(MarginOrderAPI.class);
    }

    @Override
    public OrderResult addOrder(final Order order) {
        return this.client.executeSync(this.marginOrderAPI.addOrder(order));
    }

    @Override
    public void cancleOrderByProductIdAndOrderId(final String productId, final Long orderId) {
        this.client.executeSync(this.marginOrderAPI.cancleOrdersByProductIdAndOrderId(orderId, productId));
    }

    @Override
    public void cancleOrdersByProductId(final String productId) {
        this.client.executeSync(this.marginOrderAPI.cancleOrdersByProductId(productId));
    }

    @Override
    public OrderInfo getOrderByProductIdAndOrderId(final String productId, final Long orderId) {
        return this.client.executeSync(this.marginOrderAPI.getOrderByProductIdAndOrderId(orderId, productId));
    }

    @Override
    public List<OrderInfo> getOrders(final String productId, final String status, final Long before, final Long after, final Integer limit) {
        return this.client.executeSync(this.marginOrderAPI.getOrders(productId, status, before, after, limit));
    }

    @Override
    public List<OrderInfo> getPendingOrders(final Long before, final Long after, final Integer limit) {
        return this.client.executeSync(this.marginOrderAPI.getPendingOrders(before, after, limit));
    }

    @Override
    public List<Fills> getFills(final Long orderId, final String productId, final Long before, final Long after, final Integer limit) {
        return this.client.executeSync(this.marginOrderAPI.getFills(orderId, productId, before, after, limit));
    }
}
