package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;


import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HuobiSymbolResp extends HuobiResp {

    @SerializedName("data")
    private List<HuobiSymbol> symbols = new ArrayList<>();

    public List<HuobiSymbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<HuobiSymbol> symbols) {
        this.symbols = symbols;
    }

    public HuobiSymbolResp() {
    }
}
