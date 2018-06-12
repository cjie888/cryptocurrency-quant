package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;


import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderMatchResult;
import com.google.gson.annotations.SerializedName;

public class HuobiOrderMatchResultResp extends HuobiResp {

    @SerializedName("data")
    private HuobiOrderMatchResult matchResult;

    public HuobiOrderMatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(HuobiOrderMatchResult matchResult) {
        this.matchResult = matchResult;
    }

    public HuobiOrderMatchResultResp() {
    }
}
