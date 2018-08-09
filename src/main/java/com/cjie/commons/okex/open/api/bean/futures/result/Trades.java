package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * Get the latest transaction log information.  <br/>
 * Created by Tony Tian on 2018/2/26 13:30. <br/>
 */
public class Trades {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * Transaction type
     */
    private String type;
    /**
     * Transaction price
     */
    private Double price;
    /**
     * Transaction amount
     */
    private Double qty;
    /**
     * Transaction date
     */
    private String time;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
