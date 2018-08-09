package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * Contract price estimate for delivery.  <br/>
 * Created by Tony Tian on 2018/2/26 15:52. <br/>
 */
public class EstimatedPrice {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * Estimated price
     */
    private Double estimated_price;
    private String timestamp;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Double getEstimated_price() {
        return estimated_price;
    }

    public void setEstimated_price(Double estimated_price) {
        this.estimated_price = estimated_price;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
