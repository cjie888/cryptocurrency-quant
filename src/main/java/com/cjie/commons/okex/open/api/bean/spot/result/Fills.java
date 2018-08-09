package com.cjie.commons.okex.open.api.bean.spot.result;

public class Fills {

    // 账单 id
    private Long ledger_id;
    // 币种 id
    private String product_id;
    // 价格
    private String price;
    // 数量
    private String size;
    // 订单 id
    private Long order_id;
    // 创建时间
    private String create_at;
    // 流动方向
    private String liquidity;
    // 手续费
    private String fee;
    // buy、sell
    private String side;

    public Long getLedger_id() {
        return ledger_id;
    }

    public void setLedger_id(Long ledger_id) {
        this.ledger_id = ledger_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(String liquidity) {
        this.liquidity = liquidity;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }
}
