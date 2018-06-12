package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;


import com.google.gson.annotations.SerializedName;

public class HuobiCancelOrderResp extends HuobiResp {

    @SerializedName("data")
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public HuobiCancelOrderResp() {
    }
}
