package com.cjie.cryptocurrency.quant.api.huobi.domain;


import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public int getPricePrecision() {
        return pricePrecision;
    }

    public void setPricePrecision(int pricePrecision) {
        this.pricePrecision = pricePrecision;
    }

    public int getAmountPrecision() {
        return amountPrecision;
    }

    public void setAmountPrecision(int amountPrecision) {
        this.amountPrecision = amountPrecision;
    }

    public String getSymbolPartition() {
        return symbolPartition;
    }

    public void setSymbolPartition(String symbolPartition) {
        this.symbolPartition = symbolPartition;
    }

    public HuobiSymbol() {

    }

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
