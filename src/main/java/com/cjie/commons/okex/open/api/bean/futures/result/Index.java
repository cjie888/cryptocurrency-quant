package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * The index of futures contract product information  <br/>
 * Created by Tony Tian on 2018/3/1 18:36. <br/>
 */
public class Index {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * index
     */
    private Double index = 0.00D;
    /**
     * time
     */
    private String timestamp;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Double getIndex() {
        return index;
    }

    public void setIndex(Double index) {
        this.index = index;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
