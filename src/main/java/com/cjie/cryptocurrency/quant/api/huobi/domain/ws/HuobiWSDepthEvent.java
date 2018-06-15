package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderBookEntry;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuobiWSDepthEvent extends HuobiWSEvent{

    private String symbol;

    private String type;

    private List<HuobiOrderBookEntry> bids = new ArrayList<>();

    private List<HuobiOrderBookEntry> asks = new ArrayList<>();


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
