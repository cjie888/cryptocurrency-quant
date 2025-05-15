package com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result;

public class InstrumentInfo {

    private String instId;
    private String baseCcy;
    private String quoteCcy;
    private String minSz;
    private String maxLmtSz;

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public String getBaseCcy() {
        return baseCcy;
    }

    public void setBaseCcy(String baseCcy) {
        this.baseCcy = baseCcy;
    }

    public String getQuoteCcy() {
        return quoteCcy;
    }

    public void setQuoteCcy(String quoteCcy) {
        this.quoteCcy = quoteCcy;
    }

    public String getMinSz() {
        return minSz;
    }

    public void setMinSz(String minSz) {
        this.minSz = minSz;
    }

    public String getMaxLmtSz() {
        return maxLmtSz;
    }

    public void setMaxLmtSz(String maxLmtSz) {
        this.maxLmtSz = maxLmtSz;
    }
}
