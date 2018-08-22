package com.cjie.cryptocurrency.quant.strategy.okex;

import java.math.BigDecimal;

public class ValuationTicker {

    private BigDecimal changePercent;

    private BigDecimal high;

    private BigDecimal last;

    private BigDecimal low;

    private BigDecimal open;

    private BigDecimal usdCnyRate;

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLast() {
        return last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getUsdCnyRate() {
        return usdCnyRate;
    }

    public void setUsdCnyRate(BigDecimal usdCnyRate) {
        this.usdCnyRate = usdCnyRate;
    }
}
