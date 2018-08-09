package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * all of contract liquidation <br/>
 * Created by Tony Tian on 2018/2/26 16:36. <br/>
 */
public class Liquidation {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * order price
     */
    private Double price;
    /**
     * order quantity(unit: contract)
     */
    private Double order_Qty;
    /**
     * The execution type {@link com.okcoin.commons.okex.open.api.enums.FuturesTransactionTypeEnum}
     */
    private Integer type;
    /**
     * user loss due to forced liquidation
     */
    private Double loss;
    /**
     * create date
     */
    private String create_at;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Double getOrder_Qty() {
        return order_Qty;
    }

    public void setOrder_Qty(Double order_Qty) {
        this.order_Qty = order_Qty;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }
}
