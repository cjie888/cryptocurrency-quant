package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import lombok.Data;

import java.util.List;

@Data
public class HuobiKLineResp extends HuobiResp {

    private List<HuobiKLineData> data;

}
