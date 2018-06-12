package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiMarketDetail;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class HuobiWSMarketDetailEvent extends HuobiWSEvent {

    private String symbol;

    private HuobiMarketDetail data;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public HuobiMarketDetail getData() {
        return data;
    }

    public void setData(HuobiMarketDetail data) {
        this.data = data;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("symbol",getSymbol())
                .append("ts", getTs())
                .append("data", getData())
                .toString();
    }
}
