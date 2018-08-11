package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;


@Data
public class HuobiOrderResp extends HuobiResp{

    @SerializedName("data")
    private String orderId;

}
