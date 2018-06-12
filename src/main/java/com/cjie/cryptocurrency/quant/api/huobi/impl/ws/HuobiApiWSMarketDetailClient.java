package com.cjie.cryptocurrency.quant.api.huobi.impl.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.resp.HuobiWSMarketDetailResp;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSMarketDetailEvent;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSSub;
import com.cjie.cryptocurrency.quant.api.huobi.impl.HuobiApiWSClientImpl;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;

import java.util.UUID;

public class HuobiApiWSMarketDetailClient extends AbsHuobiApiWSClient<HuobiWSMarketDetailResp> {

    private final String symbol;

    public HuobiApiWSMarketDetailClient(HuobiApiWSClientImpl client, HuobiWSEventHandler handler, String symbol) {
        super(client, handler, HuobiWSMarketDetailResp.class);
        this.symbol = symbol;
    }

    @Override
    protected HuobiWSSub calcSub() {
        String id = UUID.randomUUID().toString();
        return new HuobiWSSub(String.format("market.%s.detail", symbol), id);
    }

    @Override
    protected void doHandler(HuobiWSMarketDetailResp resp) {
        if (this.handler != null && resp != null && resp.tick != null) {
            HuobiWSMarketDetailEvent event = new HuobiWSMarketDetailEvent();
            event.setTs(resp.ts);
            event.setData(resp.tick);
            event.setSymbol(this.symbol);
            this.handler.handleMarketDetail(event);
        }
    }
}
