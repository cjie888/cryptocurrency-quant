package com.cjie.commons.okex.open.api.bean.futures.param;

import java.util.List;

/**
 * New Order
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 15:38
 */
public class Orders {
    /**
     * The id of the futures, eg: BTC-USD-180629
     */
    protected String product_id;
    /**
     * lever, default 10.
     */
    protected Double lever_rate;

    /**
     * batch new order sub element
     */
    List<OrdersItem> orders_data;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Double getLever_rate() {
        return lever_rate;
    }

    public void setLever_rate(Double lever_rate) {
        this.lever_rate = lever_rate;
    }

    public List<OrdersItem> getOrders_data() {
        return orders_data;
    }

    public void setOrders_data(List<OrdersItem> orders_data) {
        this.orders_data = orders_data;
    }
}
