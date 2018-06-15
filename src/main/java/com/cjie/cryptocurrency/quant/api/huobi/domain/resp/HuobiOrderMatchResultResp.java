package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;


import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderMatchResult;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class HuobiOrderMatchResultResp extends HuobiResp {

    @SerializedName("data")
    private HuobiOrderMatchResult matchResult;

}
