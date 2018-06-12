package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;

import java.util.List;

public class HuobiKLineResp extends HuobiResp {

    private List<HuobiKLineData> data;

    public List<HuobiKLineData> getData() {
        return data;
    }

    public void setData(List<HuobiKLineData> data) {
        this.data = data;
    }

    public HuobiKLineResp() {
    }
}
