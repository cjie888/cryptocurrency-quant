package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderInfo;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by dev on 1/8/18.
 */
public class HuobiOrdersResp extends HuobiResp {

    @SerializedName("data")
    private List<HuobiOrderInfo> orderInfos;

    public List<HuobiOrderInfo> getOrderInfos() {
        return orderInfos;
    }

    public void setOrderInfos(List<HuobiOrderInfo> orderInfos) {
        this.orderInfos = orderInfos;
    }

    public HuobiOrdersResp() {
    }
}
