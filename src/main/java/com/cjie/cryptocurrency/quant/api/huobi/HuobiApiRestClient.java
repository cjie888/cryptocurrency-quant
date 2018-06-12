package com.cjie.cryptocurrency.quant.api.huobi;


import com.cjie.cryptocurrency.quant.api.huobi.domain.*;

import java.util.List;

public interface HuobiApiRestClient {

    List<HuobiKLineData> kline(String symbol, String period, int size) throws HuobiApiException;

    HuobiTick tick(String symbol) throws HuobiApiException;

    List<String> currencys() throws HuobiApiException;

    List<HuobiSymbol> symbols() throws HuobiApiException;

    HuobiOrderBook depth(String symbol, String type) throws HuobiApiException;

    List<HuobiAccount> accounts() throws HuobiApiException;

    HuobiBalance balance(long accountId) throws HuobiApiException;

    /**
     * @see HuobiAccount#ACCOUNT_TYPE_SPOT
     * @see HuobiAccount#ACCOUNT_TYPE_OTC
     */
    HuobiBalance balance(String type) throws HuobiApiException;

    /**
     *
     * @param symbol
     * @param price
     * @param amount
     * @param type buy-market：市价买, sell-market：市价卖, buy-limit：限价买, sell-limit：限价卖
     * @return order id from huobi.pro
     * @throws HuobiApiException
     */
    String sendOrder(String symbol, String price, String amount, HuobiOrderType type) throws HuobiApiException;

    String cancelOrder(String orderId) throws HuobiApiException;

    HuobiOrderInfo orderInfo(String orderId) throws HuobiApiException;

    HuobiOrderMatchResult matchResult(String orderId) throws HuobiApiException;

    List<HuobiOrderInfo> orders(String symbol,
                                List<HuobiOrderType> types,
                                String startDate,
                                String endDate,
                                List<HuobiOrderState> states,
                                String fromOrderId,
                                int size) throws HuobiApiException;

}
