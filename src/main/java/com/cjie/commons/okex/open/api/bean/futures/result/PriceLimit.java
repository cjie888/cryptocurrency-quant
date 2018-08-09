package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * The current limit of the contract.  <br/>
 * Created by Tony Tian on 2018/2/26 16:21. <br/>
 */
public class PriceLimit {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * Highest price
     */
    private Double high;
    /**
     * Lowest price
     */
    private Double low;

    private String timestamp;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
