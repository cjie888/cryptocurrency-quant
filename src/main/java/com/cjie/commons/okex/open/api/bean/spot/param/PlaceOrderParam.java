package com.cjie.commons.okex.open.api.bean.spot.param;

public class PlaceOrderParam {
    /**
     * 客户端下单 标示id 非必填
     */
    private String client_oid;
    /**
     * 币对如 etc_eth
     */
    private String product_id;
    /**
     * 买卖类型 buy/sell
     */
    private String side;
    /**
     * 订单类型 限价单 limit 市价单 market
     */
    private String type;
    /**
     * 交易数量
     */
    private String size;
    /**
     * 限价单使用 价格
     */
    private String price;
    /**
     * 市价单使用 价格
     */
    private String funds;
    /**
     * 来源（web app ios android）
     */
    private Byte source = 0;
    /**
     * 1币币交易 2杠杆交易
     */
    private Byte system_type = 1;

    public String getClient_oid() {
        return this.client_oid;
    }

    public void setClient_oid(final String client_oid) {
        this.client_oid = client_oid;
    }

    public String getProduct_id() {
        return this.product_id;
    }

    public void setProduct_id(final String product_id) {
        this.product_id = product_id;
    }

    public String getSide() {
        return this.side;
    }

    public void setSide(final String side) {
        this.side = side;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(final String size) {
        this.size = size;
    }

    public String getPrice() {
        return this.price;
    }

    public void setPrice(final String price) {
        this.price = price;
    }

    public String getFunds() {
        return this.funds;
    }

    public void setFunds(final String funds) {
        this.funds = funds;
    }

    public Byte getSource() {
        return this.source;
    }

    public void setSource(final Byte source) {
        this.source = source;
    }

    public Byte getSystem_type() {
        return this.system_type;
    }

    public void setSystem_type(final Byte system_type) {
        this.system_type = system_type;
    }


}
