package com.cjie.cryptocurrency.quant.api.okex.v5.service.trade.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.ApiOrderVO;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.*;
import com.cjie.cryptocurrency.quant.api.okex.v5.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.v5.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.trade.TradeAPIService;
import com.cjie.cryptocurrency.quant.mapper.OptionsOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.OptionsOrder;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class TradeAPIServiceImpl extends BaseServiceImpl implements TradeAPIService {


    private ConcurrentHashMap<String, TradeAPI> tradeAPIs = new ConcurrentHashMap<>();

    @Autowired
    private SwapOrderMapper swapOrderMapper;


    @Autowired
    private OptionsOrderMapper optionsOrderMapper;

    public TradeAPI getTradeApi(String site, APIClient apiClient) {
        TradeAPI tradeAPI = tradeAPIs.get(site);
        if (tradeAPI != null) {
            return  tradeAPI;
        }
        tradeAPI = apiClient.createService(TradeAPI.class);
        tradeAPIs.put(site, tradeAPI);
        return tradeAPI;
    }


    //下单 Place Order
    @Override
    public JSONObject placeOrder(String site, PlaceOrder placeOrder) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.placeOrder(JSONObject.parseObject(JSON.toJSONString(placeOrder))));
    }

    //下单 Place Order
    @Override
    public JSONObject placeSwapOrder(String site, PlaceOrder placeOrder, String strategy) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        JSONObject orderResult =  client.executeSync(tradeAPI.placeOrder(JSONObject.parseObject(JSON.toJSONString(placeOrder))));


        log.info("order result:{}", JSONObject.toJSONString(orderResult));
        if (orderResult != null && orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {
            SwapOrder swapOrder = new SwapOrder();
            swapOrder.setInstrumentId(placeOrder.getInstId());
            swapOrder.setCreateTime(new Date());
            swapOrder.setStrategy(strategy);
            swapOrder.setIsMock(Byte.valueOf("0"));
            swapOrder.setType(Byte.valueOf(placeOrder.getType()));
            swapOrder.setPrice(new BigDecimal(placeOrder.getPx()));
            swapOrder.setSize(new BigDecimal(placeOrder.getSz()));
            swapOrder.setOrderId(String.valueOf(((JSONObject)orderResult.getJSONArray("data").get(0)).getString("ordId")));

            swapOrder.setStatus(99);
            swapOrderMapper.insert(swapOrder);
        }
        return orderResult;
    }

    @Override
    public String placeOptionsOrder(String site, PlaceOrder placeOrder, OptionsOrder optionsOrder) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        JSONObject orderResult =  client.executeSync(tradeAPI.placeOrder(JSONObject.parseObject(JSON.toJSONString(placeOrder))));


        log.info("order result:{}", JSONObject.toJSONString(orderResult));
        if (orderResult != null && orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {
            String orderId = String.valueOf(((JSONObject)orderResult.getJSONArray("data").get(0)).getString("ordId"));
            optionsOrder.setOrderId(orderId);

            optionsOrder.setStatus(99);
            optionsOrderMapper.insert(optionsOrder);
            return orderId;
        }
        return null;
    }

    //批量下单 Place Multiple Orders
    @Override
    public JSONObject placeMultipleOrders(String site, List<PlaceOrder> placeOrders) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        JSONObject orderResult =   client.executeSync(tradeAPI.placeMultipleOrders(placeOrders));

        log.info("order result:{}", JSONObject.toJSONString(orderResult));
        if (orderResult != null && orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {
            for (PlaceOrder placeOrder : placeOrders) {
                SwapOrder swapOrder = new SwapOrder();
                swapOrder.setInstrumentId(placeOrder.getInstId());
                swapOrder.setCreateTime(new Date());
                swapOrder.setStrategy("netGrid");
                swapOrder.setIsMock(Byte.valueOf("0"));
                swapOrder.setType(Byte.valueOf(placeOrder.getType()));
                swapOrder.setPrice(new BigDecimal(placeOrder.getPx()));
                swapOrder.setSize(new BigDecimal(placeOrder.getSz()));
                swapOrder.setOrderId(String.valueOf(((JSONObject) orderResult.getJSONArray("data").get(0)).getString("ordId")));

                swapOrder.setStatus(99);
                swapOrderMapper.insert(swapOrder);
            }
        }
        return orderResult;
    }

    //撤单 Cancel Order
    @Override
    public JSONObject cancelOrder(String site, CancelOrder cancelOrder) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.cancelOrder(cancelOrder));
    }

    //批量撤单 Cancel Multiple Orders
    @Override
    public JSONObject cancelMultipleOrders(String site, List<CancelOrder> cancelOrders) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.cancelMultipleOrders(cancelOrders));
    }

    //修改订单 Amend Order
    @Override
    public JSONObject amendOrder(String site, AmendOrder amendOrder) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.amendOrder(amendOrder));
    }

    //批量修改订单 Amend Multiple Orders
    @Override
    public JSONObject amendMultipleOrders(String site, List<AmendOrder> amendOrders) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.amendMultipleOrders(amendOrders));
    }

    //市价仓位全平 Close Positions
    @Override
    public JSONObject closePositions(String site, ClosePositions closePositions) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.closePositions(closePositions));
    }

    //获取订单信息 Get Order Details
    @Override
    public JSONObject getOrderDetails(String site, String instId, String ordId, String clOrdId) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getOrderDetails(instId, ordId, clOrdId));
    }

    //获取未成交订单列表 Get Order List
    @Override
    public JSONObject getOrderList(String site, String instType,String uly,String instId,String ordType,
                                   String state,String after,String before,String limit) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getOrderList(instType, uly, instId, ordType, state, after, before, limit));
    }

    //获取历史订单记录（近七天） Get Order History (last 7 days）
    @Override
    public JSONObject getOrderHistory7days(String site, String instType, String uly,
                                           String instId, String ordType, String state, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getOrderHistory7days(instType, uly, instId, ordType, state, after, before, limit));
    }

    ////获取历史订单记录（近三个月） Get Order History (last 3 months)
    @Override
    public JSONObject getOrderHistory3months(String site, String instType, String uly,
                                             String instId, String ordType, String state, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getOrderHistory3months(instType, uly, instId, ordType, state, after, before, limit));
    }

    //获取成交明细 Get Transaction Details
    @Override
    public JSONObject getTransactionDetails(String site, String instType, String uly,
                                            String instId, String ordId, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getTransactionDetails(instType, uly, instId, ordId, after, before, limit));
    }

    //委托策略下单 Place Algo Order
    @Override
    public JSONObject placeAlgoOrder(String site, PlaceAlgoOrder placeAlgoOrder) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.placeAlgoOrder(JSONObject.parseObject(JSON.toJSONString(placeAlgoOrder))));
    }

    //撤销策略委托订单 Cancel Algo Order
    @Override
    public JSONObject cancelAlgoOrder(String site, List<CancelAlgoOrder> cancelAlgoOrder) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.cancelAlgoOrders(cancelAlgoOrder));
    }

    //获取未完成策略委托单列表 Get Algo Order List
    @Override
    public JSONObject getAlgoOrderList(String site, String algoId, String instType,
                                       String instId, String ordType, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getAlgoOrderList(algoId, instType, instId, ordType, after, before, limit));
    }

    //获取历史策略委托单列表 Get Algo Order History
    @Override
    public JSONObject getAlgoOrderHistory(String site, String state, String algoId, String instType,
                                          String instId, String ordType, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        TradeAPI tradeAPI = getTradeApi(site, client);
        return client.executeSync(tradeAPI.getAlgoOrderHistory(state, algoId, instType, instId, ordType, after, before, limit));
    }
}
