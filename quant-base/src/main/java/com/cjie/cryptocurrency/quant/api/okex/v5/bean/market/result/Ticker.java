package com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result;

public class Ticker {

    private String instType; //SWAP

    private String instId; //BTC-USD-SWAP

    private String last;   //56956.1
//            "lastSz": "3",
//            "askPx": "56959.1",
//            "askSz": "10582",
//            "bidPx": "56959",
//            "bidSz": "4552",
//            "open24h": "55926",
//            "high24h": "57641.1",
//            "low24h": "54570.1",
//            "volCcy24h": "81137.755",
//            "vol24h": "46258703",
//            "ts": "1620289117764",
//            "sodUtc0": "55926",
//            "sodUtc8": "55926"


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

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }
}
