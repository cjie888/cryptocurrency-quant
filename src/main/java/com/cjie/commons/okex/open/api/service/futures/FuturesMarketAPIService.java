package com.cjie.commons.okex.open.api.service.futures;

import com.alibaba.fastjson.JSONArray;
import com.cjie.commons.okex.open.api.bean.futures.result.*;

import java.util.List;

/**
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 16:06
 */
public interface FuturesMarketAPIService {

    /**
     * Get all of futures contract list
     */
    List<Products> getProducts();

    /**
     * Get the futures contract currencies
     */
    List<Currencies> getCurrencies();

    /**
     * Get the futures contract product book
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     * @param depth     value：1-200
     * @param conflated 1(merge depth)
     * @return
     */
    Book getProductBook(String productId, Integer depth, Integer conflated);

    /**
     * Get the futures contract product ticker
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    Ticker getProductTicker(String productId);

    /**
     * Get the futures contract product trades
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    List<Trades> getProductTrades(String productId);

    /**
     * Get the futures contract product candles
     *
     * @param productId   The id of the futures contract eg: BTC-USD-0331"
     * @param start       start timestamp of candles, (eg:1530676775258)
     * @param end         start timestamp of candles, (eg:1530676841895)
     * @param granularity Time granularity measured in seconds. data after the timestamp will be returned
     *                    60     ->  1min
     *                    180    ->  3min
     *                    300    ->  5min
     *                    900    ->  15min
     *                    1800   ->  30min
     *                    3600   ->  1hour
     *                    7200   ->  2hour
     *                    14400  ->  4hour
     *                    21600  ->  6hour
     *                    43200  ->  12hour
     *                    86400  ->  1day
     *                    604800 ->  1week
     */
    JSONArray getProductCandles(String productId, long start, long end, long granularity);

    /**
     * Get the futures contract product index
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    Index getProductIndex(String productId);

    /**
     * Get the futures contract product estimated price
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    EstimatedPrice getProductEstimatedPrice(String productId);

    /**
     * Get the futures contract product holds
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    Holds getProductHolds(String productId);

    /**
     * Get the futures contract product limit price
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     */
    PriceLimit getProductPriceLimit(String productId);

    /**
     * Get the futures contract liquidation
     *
     * @param productId The id of the futures contract eg: BTC-USD-0331"
     * @param status    0:Last 7 days: Open 1:Last 7 days: Filled
     */
    List<Liquidation> getProductLiquidation(String productId, int status);

}
