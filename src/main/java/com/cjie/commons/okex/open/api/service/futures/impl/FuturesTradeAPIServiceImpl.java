package com.cjie.commons.okex.open.api.service.futures.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.futures.param.ClosePosition;
import com.cjie.commons.okex.open.api.bean.futures.param.Order;
import com.cjie.commons.okex.open.api.bean.futures.param.Orders;
import com.cjie.commons.okex.open.api.bean.futures.param.OrdersItem;
import com.cjie.commons.okex.open.api.bean.futures.result.OrderResult;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.futures.FuturesTradeAPIService;
import com.cjie.commons.okex.open.api.utils.JsonUtils;
import com.cjie.commons.okex.open.api.utils.NumberUtils;

import java.util.List;


/**
 * Futures trade api
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 18:52
 */
public class FuturesTradeAPIServiceImpl implements FuturesTradeAPIService {

    private APIClient client;
    private FuturesTradeAPI api;

    public FuturesTradeAPIServiceImpl(APIConfiguration config) {
        this.client = new APIClient(config);
        this.api = client.createService(FuturesTradeAPI.class);
    }

    @Override
    public JSONObject getPositions() {

        return this.client.executeSync(this.api.getPositions());
    }

    @Override
    public JSONObject getProductPosition(String productId) {
        return this.client.executeSync(this.api.getProductPosition(productId));
    }

    @Override
    public JSONObject getAccounts() {
        return this.client.executeSync(this.api.getAccounts());
    }

    @Override
    public JSONObject getAccountsByCurrency(String currency) {
        return this.client.executeSync(this.api.getAccountsByCurrency(currency));
    }

    @Override
    public JSONArray getAccountsLedgerByCurrency(String currency) {
        return this.client.executeSync(this.api.getAccountsLedgerByCurrency(currency));
    }

    @Override
    public JSONObject getAccountsHoldsByProductId(String productId) {
        return this.client.executeSync(this.api.getAccountsHoldsByProductId(productId));
    }

    @Override
    public OrderResult order(Order order) {
        return this.client.executeSync(this.api.order(JsonUtils.convertObject(order, Order.class)));
    }

    @Override
    public JSONObject orders(Orders orders) {
        JSONObject params = new JSONObject();
        params.put("product_id", orders.getProduct_id());
        params.put("lever_rate", NumberUtils.doubleToString(orders.getLever_rate()));
        params.put("orders_data", JsonUtils.convertList(orders.getOrders_data(), OrdersItem.class));
        return this.client.executeSync(this.api.orders(params));
    }

    @Override
    public JSONObject cancelProductOrder(String productId, long orderId) {
        return this.client.executeSync(this.api.cancelProductOrder(productId, String.valueOf(orderId)));
    }

    @Override
    public JSONArray cancelProductOrders(String productId) {
        return this.client.executeSync(this.api.cancelProductOrders(productId));
    }

    @Override
    public JSONObject closePosition(List<ClosePosition> closePositions) {
        JSONArray close_position_data = JsonUtils.convertList(closePositions, ClosePosition.class);
        JSONObject params = new JSONObject();
        params.put("close_position_data", close_position_data.toJSONString());
        String responseString = this.client.getApiHttp().delete("/api/futures/v3/close_position", params);
        return JSONObject.parseObject(responseString, JSONObject.class);
    }

    @Override
    public JSONObject getOrders(String currency, int status, int before, int after, int limit) {
        return this.client.executeSync(this.api.getOrders(currency, status, before, after, limit));
    }

    @Override
    public JSONObject getOrder(long orderId) {
        return this.client.executeSync(this.api.getOrder(String.valueOf(orderId)));
    }

    @Override
    public JSONArray getFills(String productId, long orderId, int before, int after, int limit) {
        return this.client.executeSync(this.api.getFills(productId, String.valueOf(orderId), before, after, limit));
    }

    @Override
    public JSONArray getUsersSelfTrailingVolume() {
        return this.client.executeSync(this.api.getUsersSelfTrailingVolume());
    }
}
