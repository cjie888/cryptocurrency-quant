package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuobiCurrencysResp extends HuobiResp {

    @SerializedName("data")
    private List<String> symbols = new ArrayList<>();

}
