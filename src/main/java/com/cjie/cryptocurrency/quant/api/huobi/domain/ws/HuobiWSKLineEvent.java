package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class HuobiWSKLineEvent extends HuobiWSEvent {

    private String symbol;

    private String period;

    private HuobiKLineData data;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("symbol",symbol)
                .append("period",period)
                .append("ts", new SimpleDateFormat().format(new Date(getTs())))
                .append("data", getData())
                .toString();
    }
}
