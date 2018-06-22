package com.cjie.cryptocurrency.quant.api.huobi.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class HuobiTradeDetail {

    private double amount;

    private long ts;

    private String id;

    private double price;

    private String direction;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id",id)
                .append("amount",amount)
                .append("ts", ts)
                .append("price", price)
                .append("direction", direction)
                .toString();
    }
}
