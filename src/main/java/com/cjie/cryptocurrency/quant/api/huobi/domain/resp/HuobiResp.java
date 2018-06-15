package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class HuobiResp {

    @SerializedName("status")
    private String status;

    @SerializedName("err-code")
    private String errCode;

    @SerializedName("err-msg")
    private String errMsg;

    @SerializedName("ts")
    private long ts;

}
