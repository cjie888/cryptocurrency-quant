package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiTick;
import lombok.Data;

@Data
public class HuobiTickResp extends HuobiResp {

    private HuobiTick tick;

}
