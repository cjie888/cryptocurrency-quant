package com.cjie.commons.okex.open.api.service.futures;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.futures.CursorPageParams;
import com.cjie.commons.okex.open.api.bean.futures.param.ClosePosition;
import com.cjie.commons.okex.open.api.bean.futures.param.Order;
import com.cjie.commons.okex.open.api.bean.futures.param.Orders;
import com.cjie.commons.okex.open.api.bean.futures.result.OrderResult;

import java.util.List;

/**
 * Futures Trade API Service
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 18:52
 */
public interface FuturesTradeAPIService {

    /**
     * Get all of futures contract position list
     */
    JSONObject getPositions();

    /**
     * Get the futures contract product position
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    JSONObject getProductPosition(String productId);

    /**
     * Get all of futures contract account list
     */
    JSONObject getAccounts();

    /**
     * Get the futures contract currency account
     *
     * @param currency {@link com.okcoin.commons.okex.open.api.enums.FuturesCurrenciesEnum}
     *                 eg: FuturesCurrenciesEnum.BTC.name()
     */
    JSONObject getAccountsByCurrency(String currency);

    /**
     * Get the futures contract currency ledger
     *
     * @param currency {@link com.okcoin.commons.okex.open.api.enums.FuturesCurrenciesEnum}
     *                 eg: FuturesCurrenciesEnum.BTC.name()
     */
    JSONArray getAccountsLedgerByCurrency(String currency);

    /**
     * Get the futures contract product holds
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    JSONObject getAccountsHoldsByProductId(String productId);

    /**
     * Create a new order
     */
    OrderResult order(Order order);

    /**
     * Batch create new order.(Max of 5 orders are allowed per request))
     */
    JSONObject orders(Orders orders);

    /**
     * Cancel the order
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     * @param orderId   the order id provided by okex.com eg: 372238304216064
     */
    JSONObject cancelProductOrder(String productId, long orderId);

    /**
     * Batch Cancel the orders of this product id
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    JSONArray cancelProductOrders(String productId);

    /**
     * close position
     *
     * @param closePositions close position data
     */
    JSONObject closePosition(List<ClosePosition> closePositions);

    /**
     * Get all of futures contract order list
     *
     * @param currency {@link com.okcoin.commons.okex.open.api.enums.FuturesCurrenciesEnum}
     *                 eg: FuturesCurrenciesEnum.BTC.name()
     * @param status   Order status: 0: waiting for transaction 1: 1: part of the deal 2: all transactions.
     * @param before   Request page before (newer) this pagination id.
     * @param after    Request page after (older) this pagination id.
     * @param limit    Number of results per request. Maximum 100. (default 100)
     *                 {@link CursorPageParams}
     * @return
     */
    JSONObject getOrders(String currency, int status, int before, int after, int limit);

    /**
     * Get all of futures contract a order by order id
     *
     * @param orderId the order id provided by okex.com eg: 372238304216064
     */
    JSONObject getOrder(long orderId);

    /**
     * Get all of futures contract transactions.
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     * @param orderId   the order id provided by okex.com eg: 372238304216064
     * @param before    Request page before (newer) this pagination id.
     * @param after     Request page after (older) this pagination id.
     * @param limit     Number of results per request. Maximum 100. (default 100)
     *                  {@link CursorPageParams}
     * @return
     */
    JSONArray getFills(String productId, long orderId, int before, int after, int limit);

    /**
     * Get futures contract account volume
     */
    JSONArray getUsersSelfTrailingVolume();
}
