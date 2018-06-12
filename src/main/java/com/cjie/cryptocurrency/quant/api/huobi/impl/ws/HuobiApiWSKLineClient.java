package com.cjie.cryptocurrency.quant.api.huobi.impl.ws;


import com.cjie.cryptocurrency.quant.api.huobi.domain.resp.HuobiWSKLineResp;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSKLineEvent;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSSub;
import com.cjie.cryptocurrency.quant.api.huobi.impl.HuobiApiWSClientImpl;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.UUID;

public class HuobiApiWSKLineClient extends AbsHuobiApiWSClient<HuobiWSKLineResp> {

    private static String[] VALID_PERIODS = new String[]{"1min", "5min", "15min", "30min", "60min", "1day", "1mon", "1week", "1year"};

    private final String symbol;

    private final String period;

    public HuobiApiWSKLineClient(HuobiApiWSClientImpl client, HuobiWSEventHandler handler, String symbol, String period) {
        super(client, handler, HuobiWSKLineResp.class);
        if (StringUtils.isEmpty(symbol) || StringUtils.isEmpty(period) || handler == null) {
            throw new IllegalArgumentException("symbol|period|handler not valid");
        }
        if (Arrays.stream(VALID_PERIODS).noneMatch((e) -> e.equals(period))) {
            throw new IllegalArgumentException("type is not valid.");
        }
        this.symbol = symbol;
        this.period = period;
    }

    @Override
    protected HuobiWSSub calcSub() {
        String id = UUID.randomUUID().toString();
        HuobiWSSub sub = new HuobiWSSub(String.format("market.%s.kline.%s", symbol, period), id);
        return sub;
    }

    @Override
    protected void doHandler(HuobiWSKLineResp resp) {
        if (this.handler != null && resp != null && resp.tick != null) {
            HuobiWSKLineEvent event = new HuobiWSKLineEvent();
            event.setSymbol(symbol);
            event.setPeriod(period);
            event.setTs(resp.ts);
            event.setData(resp.tick);
            this.handler.handleKLine(event);
        }
    }
}
