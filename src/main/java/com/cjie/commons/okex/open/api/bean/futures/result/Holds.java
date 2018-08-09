package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * All of contract position  <br/>
 * Created by Tony Tian on 2018/2/26 16:14. <br/>
 */
public class Holds {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * all of position
     */
    private Integer amount;

    private String timestamp;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
