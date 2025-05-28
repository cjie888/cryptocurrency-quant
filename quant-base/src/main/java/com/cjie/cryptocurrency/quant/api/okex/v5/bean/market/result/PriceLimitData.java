package com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result;

public class PriceLimitData {

    private String instType; //	产品类型 SPOT：币币 MARGIN：杠杆 SWAP：永续合约 FUTURES：交割合约 OPTION：期权 若产品ID支持杠杆交易，则返回MARGIN；否则，返回SPOT。
    private String instId;    //	产品ID ，如 BTC-USDT-SWAP
    private String buyLmt;    //	最高买 当enabled为false时，返回""
    private String sellLmt;    // 	最低卖价 当enabled为false时，返回""
    private String ts;    //	限价数据更新时间 ，Unix时间戳的毫秒数格式，如 1597026383085
    private String enabled; //	限价是否生效 true：限价生效 false：限价不生效

    public String getInstType() {
        return instType;
    }

    public void setInstType(String instType) {
        this.instType = instType;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public String getBuyLmt() {
        return buyLmt;
    }

    public void setBuyLmt(String buyLmt) {
        this.buyLmt = buyLmt;
    }

    public String getSellLmt() {
        return sellLmt;
    }

    public void setSellLmt(String sellLmt) {
        this.sellLmt = sellLmt;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}