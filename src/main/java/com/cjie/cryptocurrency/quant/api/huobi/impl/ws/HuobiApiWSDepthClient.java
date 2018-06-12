package com.cjie.cryptocurrency.quant.api.huobi.impl.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.resp.HuobiWSOrderBookResp;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSDepthEvent;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSSub;
import com.cjie.cryptocurrency.quant.api.huobi.impl.HuobiApiWSClientImpl;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.UUID;

public class HuobiApiWSDepthClient extends AbsHuobiApiWSClient<HuobiWSOrderBookResp> {

    private static String[] VALID_TYPES = new String[]{"step0", "step1", "step2", "step3", "step4", "step5"};

    private final String symbol;

    private final String type;

    public HuobiApiWSDepthClient(HuobiApiWSClientImpl client, HuobiWSEventHandler handler, String symbol, String type) {
        super(client, handler, HuobiWSOrderBookResp.class);
        if (org.apache.commons.lang3.StringUtils.isEmpty(symbol) || StringUtils.isEmpty(type) || handler == null) {
            throw new IllegalArgumentException("symbol|type|handler not valid");
        }
        if (Arrays.stream(VALID_TYPES).noneMatch((e) -> e.equals(type))) {
            throw new IllegalArgumentException("type is not valid.");
        }
        this.symbol = symbol;
        this.type = type;
    }

    @Override
    protected HuobiWSSub calcSub() {
        String id = UUID.randomUUID().toString();
        HuobiWSSub sub = new HuobiWSSub(String.format("market.%s.depth.%s", symbol, type), id);
        return sub;
    }


    @Override
    protected void doHandler(HuobiWSOrderBookResp resp) {
        if (this.handler != null && resp != null && resp.tick != null) {
            HuobiWSDepthEvent event = new HuobiWSDepthEvent();
            event.setSymbol(symbol);
            event.setType(type);
            event.setTs(resp.ts);
            event.setAsks(resp.tick.asks);
            event.setBids(resp.tick.bids);
            this.handler.handleDepth(event);
        }
    }

}
