package com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param;

public class AmendOrder {
    private String instId;
    private Boolean cxlOnFail;
    private String ordId;
    private String clOrdId;
    private String reqId;
    private String newSz;
    private String newPx;

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public Boolean getCxlOnFail() {
        return cxlOnFail;
    }

    public void setCxlOnFail(Boolean cxlOnFail) {
        this.cxlOnFail = cxlOnFail;
    }

    public String getOrdId() {
        return ordId;
    }

    public void setOrdId(String ordId) {
        this.ordId = ordId;
    }

    public String getClOrdId() {
        return clOrdId;
    }

    public void setClOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getNewSz() {
        return newSz;
    }

    public void setNewSz(String newSz) {
        this.newSz = newSz;
    }

    public String getNewPx() {
        return newPx;
    }

    public void setNewPx(String newPx) {
        this.newPx = newPx;
    }

    @Override
    public String toString() {
        return "AmendOrder{" +
                "instId='" + instId + '\'' +
                ", cxlOnFail=" + cxlOnFail +
                ", ordId='" + ordId + '\'' +
                ", clOrdId='" + clOrdId + '\'' +
                ", reqId='" + reqId + '\'' +
                ", newSz='" + newSz + '\'' +
                ", newPx='" + newPx + '\'' +
                '}';
    }
}
