package com.cjie.cryptocurrency.quant.api.huobi.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class HuobiOrderBook {

    private long ts;

    private List<HuobiOrderBookEntry> bids = new ArrayList<>();

    private List<HuobiOrderBookEntry> asks = new ArrayList<>();

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
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

    public HuobiOrderBook() {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("ts",ts)
                .append("bids", bids)
                .append("asks", asks)
                .toString();
    }
}
