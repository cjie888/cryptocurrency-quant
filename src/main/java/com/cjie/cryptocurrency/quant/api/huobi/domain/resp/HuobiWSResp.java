package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class HuobiWSResp {

    public static final String STATUES_OK = "ok";

    public String ch;

    public long ts;

    public String id;

    public String status;

    public String subbed;

    @SerializedName("err-code")
    @JsonProperty("err-code")
    public String errCode;


    @SerializedName("err-msg")
    @JsonProperty("err-msg")
    public String errMsg;
}
