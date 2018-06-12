package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class HuobiWSKLineResp extends HuobiWSResp{

    public HuobiKLineData tick;

    public HuobiWSKLineResp() {
    }
}
