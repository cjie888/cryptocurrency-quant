package com.cjie.cryptocurrency.quant.api.huobi.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class HuobiKLineData {

    private long id;

    private double amount;

    private int count;

    private double open;

    private double close;

    private double low;

    private double high;

    private double vol;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id",id)
                .append("amount", amount)
                .append("count", count)
                .append("open", open)
                .append("close", close)
                .append("low", low)
                .append("high", high)
                .append("vol", vol)
                .toString();
    }
}
