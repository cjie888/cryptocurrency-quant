package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderInfo;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class HuobiOrdersResp extends HuobiResp {

    @SerializedName("data")
    private List<HuobiOrderInfo> orderInfos;

}
