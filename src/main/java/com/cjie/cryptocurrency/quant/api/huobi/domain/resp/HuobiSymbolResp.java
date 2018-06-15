package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;


import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuobiSymbolResp extends HuobiResp {

    @SerializedName("data")
    private List<HuobiSymbol> symbols = new ArrayList<>();

}
