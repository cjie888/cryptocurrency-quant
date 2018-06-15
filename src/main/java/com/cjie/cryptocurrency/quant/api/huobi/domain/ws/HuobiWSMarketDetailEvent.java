package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiMarketDetail;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class HuobiWSMarketDetailEvent extends HuobiWSEvent {

    private String symbol;

    private HuobiMarketDetail data;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("symbol",getSymbol())
                .append("ts", getTs())
                .append("data", getData())
                .toString();
    }
}
