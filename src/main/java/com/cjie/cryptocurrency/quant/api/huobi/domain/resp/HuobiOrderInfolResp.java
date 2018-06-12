package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderInfo;
import com.google.gson.annotations.SerializedName;

public class HuobiOrderInfolResp extends HuobiResp {

    @SerializedName("data")
    private HuobiOrderInfo orderDetail;

    public HuobiOrderInfo getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(HuobiOrderInfo orderDetail) {
        this.orderDetail = orderDetail;
    }

    public HuobiOrderInfolResp() {
    }
}
