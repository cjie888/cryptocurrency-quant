package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderBookEntry;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class HuobiWSDepthEvent extends HuobiWSEvent{

    private String symbol;

    private String type;

    private List<HuobiOrderBookEntry> bids = new ArrayList<>();

    private List<HuobiOrderBookEntry> asks = new ArrayList<>();

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<HuobiOrderBookEntry> getBids() {
        return bids;
    }

    public void setBids(List<HuobiOrderBookEntry> bids) {
        this.bids = bids;
    }

    public List<HuobiOrderBookEntry> getAsks() {
        return asks;
    }

    public void setAsks(List<HuobiOrderBookEntry> asks) {
        this.asks = asks;
    }

    public HuobiWSDepthEvent() {
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("symbol",symbol)
                .append("type",type)
                .append("ts",getTs())
                .append("bids", getBids())
                .append("asks", getAsks())
                .toString();
    }
}
