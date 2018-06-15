package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiTradeDetail;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Data
public class HuobiWSTradeDetailEvent extends HuobiWSEvent {

    private String symbol;

    private List<HuobiTradeDetail> details;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("symbol",symbol)
                .append("ts",getTs())
                .append("details", getDetails())
                .toString();
    }
}
