package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiTick;

public class HuobiTickResp extends HuobiResp {

    private HuobiTick tick;

    public HuobiTick getTick() {
        return tick;
    }

    public void setTick(HuobiTick tick) {
        this.tick = tick;
    }

    public HuobiTickResp() {
    }
}
