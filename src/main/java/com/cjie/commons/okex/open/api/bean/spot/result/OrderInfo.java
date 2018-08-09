package com.cjie.commons.okex.open.api.bean.spot.result;

public class OrderInfo {

    private Long order_id;

    private String price;

    private String funds;

    private String size;

    private String created_at;

    private String filled_size;

    private String exectued_value;

    private String status;

    private String side;

    private String type;

    private String product_id;

    public Long getOrder_id() {
        return this.order_id;
    }

    public void setOrder_id(final Long order_id) {
        this.order_id = order_id;
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

    public String getSize() {
        return this.size;
    }

    public void setSize(final String size) {
        this.size = size;
    }

    public String getCreated_at() {
        return this.created_at;
    }

    public void setCreated_at(final String created_at) {
        this.created_at = created_at;
    }

    public String getFilled_size() {
        return this.filled_size;
    }

    public void setFilled_size(final String filled_size) {
        this.filled_size = filled_size;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = status;
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

    public String getProduct_id() {
        return this.product_id;
    }

    public void setProduct_id(final String product_id) {
        this.product_id = product_id;
    }

    public String getExectued_value() {
        return this.exectued_value;
    }

    public void setExectued_value(final String exectued_value) {
        this.exectued_value = exectued_value;
    }
}
