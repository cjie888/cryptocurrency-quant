package com.cjie.cryptocurrency.quant.api.huobi.impl.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.resp.HuobiWSTradeDetailResp;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSSub;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSTradeDetailEvent;
import com.cjie.cryptocurrency.quant.api.huobi.impl.HuobiApiWSClientImpl;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;


import java.util.UUID;

public class HuobiApiWSTradeDetailClient extends AbsHuobiApiWSClient<HuobiWSTradeDetailResp> {

    private final String symbol;


    public HuobiApiWSTradeDetailClient(HuobiApiWSClientImpl client, HuobiWSEventHandler handler, String symbol) {
        super(client, handler, HuobiWSTradeDetailResp.class);
        this.symbol = symbol;
    }

    @Override
    protected HuobiWSSub calcSub() {
        String id = UUID.randomUUID().toString();
        HuobiWSSub sub = new HuobiWSSub(String.format("market.%s.trade.detail", symbol), id);
        return sub;
    }

    @Override
    protected void doHandler(HuobiWSTradeDetailResp resp) {

        if(resp.tick != null && resp.tick.data != null){
            HuobiWSTradeDetailEvent event = new HuobiWSTradeDetailEvent();
            event.setSymbol(this.symbol);
            event.setTs( resp.ts );
            event.setDetails( resp.tick.data );
            if(this.handler != null){
                this.handler.handleTradeDetail(event);
            }
        }

    }
}
