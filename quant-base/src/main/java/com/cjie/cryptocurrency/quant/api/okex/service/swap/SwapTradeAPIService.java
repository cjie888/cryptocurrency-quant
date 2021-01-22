package com.cjie.cryptocurrency.quant.api.okex.service.swap;

import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpCancelOrderVO;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrder;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrders;


public interface SwapTradeAPIService {
    /**
     * 下单
     * @param ppOrder
     * @return
     */
    String order(String site, PpOrder ppOrder, String strategy);

    /**
     * 批量下单
     * @param ppOrders
     * @return
     */
    String orders(String site, PpOrders ppOrders);

    /**
     * 撤单
     * @param instrumentId
     * @param orderId
     * @return
     */
    String cancelOrder(String site, String instrumentId, String orderId);

    /**
     * 批量撤单
     * @param instrumentId
     * @param ppCancelOrderVO
     * @return
     */
    String cancelOrders(String site, String instrumentId, PpCancelOrderVO ppCancelOrderVO);
}
