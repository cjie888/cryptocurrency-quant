package com.cjie.commons.okex.open.api.bean.futures.result;

import java.util.List;

/**
 * futures contract product book
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/12 15:14
 */
public class Book {

    /**
     * asks book
     */
    List<List<Double>> asks;
    /**
     * bids book
     */
    List<List<Double>> bids;

    public List<List<Double>> getAsks() {
        return asks;
    }

    public void setAsks(List<List<Double>> asks) {
        this.asks = asks;
    }

    public List<List<Double>> getBids() {
        return bids;
    }

    public void setBids(List<List<Double>> bids) {
        this.bids = bids;
    }
}
