package com.cjie.commons.okex.open.api.bean.spot.result;

public class OrderResult {

    private boolean result;
    private Long order_id;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }
}
