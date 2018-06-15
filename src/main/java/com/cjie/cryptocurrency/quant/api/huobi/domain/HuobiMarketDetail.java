package com.cjie.cryptocurrency.quant.api.huobi.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class HuobiMarketDetail {

    private double amount;

    private double open;

    private double close;

    private double high;

    private long id;

    private long count;

    private double low;

    private double vol;

    private long version;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("amount",amount)
                .append("open", open)
                .append("close", close)
                .append("high", high)
                .append("id", id)
                .append("count", count)
                .append("low", low)
                .append("vol", vol)
                .append("version",version)
                .toString();
    }
}
