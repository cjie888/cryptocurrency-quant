package com.cjie.cryptocurrency.quant.api.okex.v5.service.trade;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.*;
import com.cjie.cryptocurrency.quant.model.OptionsOrder;

import java.util.List;

public interface TradeAPIService {

    //下单 Place Order
    JSONObject placeOrder(String site, PlaceOrder placeOrder);

    //下单 Place Order
    JSONObject placeSwapOrder(String site, PlaceOrder placeOrder, String strategy);

    String placeOptionsOrder(String site, PlaceOrder placeOrder, OptionsOrder optionsOrder);

    //批量下单 Place Multiple Orders
    JSONObject placeMultipleOrders(String site, List<PlaceOrder> placeOrders, String strategy);

    //撤单 Cancel Order
    JSONObject cancelOrder(String site, CancelOrder cancelOrder);

    //批量撤单 Cancel Multiple Orders
    JSONObject cancelMultipleOrders(String site, List<CancelOrder> cancelOrders);

    //修改订单 Amend Order
    JSONObject amendOrder(String site, AmendOrder amendOrder);

    //批量修改订单 Amend Multiple Orders
    JSONObject amendMultipleOrders(String site, List<AmendOrder> amendOrders);

    //市价仓位全平 Close Positions
    JSONObject closePositions(String site, ClosePositions closePositions);

    //获取订单信息 Get Order Details
    JSONObject getOrderDetails(String site, String instId, String ordId, String clOrdId);

    //获取未成交订单列表 Get Order List
    JSONObject getOrderList(String site, String instType, String uly, String instId, String ordType, String state, String after, String before, String limit);

    //获取历史订单记录（近七天） Get Order History (last 7 days）
    JSONObject getOrderHistory7days(String site, String instType, String uly, String instId, String ordType, String state, String after, String before, String limit);

    //获取历史订单记录（近三个月） Get Order History (last 3 months)
    JSONObject getOrderHistory3months(String site, String instType, String uly, String instId, String ordType, String state, String after, String before, String limit);

    //获取成交明细 Get Transaction Details
    JSONObject getTransactionDetails(String site, String instType, String uly, String instId, String ordId, String after, String before, String limit);

    //委托策略下单 Place Algo Order
    JSONObject placeAlgoOrder(String site, PlaceAlgoOrder placeAlgoOrder);

    //撤销策略委托订单 Cancel Algo Order
    JSONObject cancelAlgoOrder(String site, List<CancelAlgoOrder> cancelAlgoOrder);

    //获取未完成策略委托单列表 Get Algo Order List
    JSONObject getAlgoOrderList(String site, String algoId, String instType, String instId, String ordType, String after, String before, String limit);

    //获取历史策略委托单列表 Get Algo Order History
    JSONObject getAlgoOrderHistory(String site, String state, String algoId, String instType, String instId, String ordType, String after, String before, String limit);


}
