package com.cjie.cryptocurrency.quant.api.huobi.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuobiOrderBook {

    private long ts;

    private List<HuobiOrderBookEntry> bids = new ArrayList<>();

    private List<HuobiOrderBookEntry> asks = new ArrayList<>();


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("ts",ts)
                .append("bids", bids)
                .append("asks", asks)
                .toString();
    }
}
