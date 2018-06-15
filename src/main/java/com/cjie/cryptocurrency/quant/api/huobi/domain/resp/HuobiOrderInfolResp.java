package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderInfo;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class HuobiOrderInfolResp extends HuobiResp {

    @SerializedName("data")
    private HuobiOrderInfo orderDetail;

}
