package com.cjie.cryptocurrency.quant.api.huobi.domain;


import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class HuobiSymbol {

    // private String symbol;

    @SerializedName("base-currency")
    private String baseCurrency;

    @SerializedName("quote-currency")
    private String quoteCurrency;

    @SerializedName("price-precision")
    private int pricePrecision;

    @SerializedName("amount-precision")
    private int amountPrecision;

    @SerializedName("symbol-partition")
    private String symbolPartition;

    public String getSymbol(){
        return String.format("%s%s",baseCurrency, quoteCurrency);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("base-currency",baseCurrency)
                .append("quote-currency", quoteCurrency)
                .append("price-precision", pricePrecision)
                .append("amount-precision",amountPrecision)
                .append("symbol-partition",symbolPartition)
                .toString();
    }
}
