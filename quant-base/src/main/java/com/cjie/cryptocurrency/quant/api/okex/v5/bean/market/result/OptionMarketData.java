package com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result;

public class OptionMarketData {

    private String instType; // 产品类型 OPTION：期权
    private String instId; //	产品ID，如 BTC-USD-200103-5500-C
    private String uly;	//标的指数
    private String  delta;	//期权价格对uly价格的敏感度
    private String gamma;	//delta对uly价格的敏感度
    private String vega;  //期权价格对隐含波动率的敏感度
    private String theta; //期权价格对剩余期限的敏感度
    private String deltaBS; 	//BS模式下期权价格对uly价格的敏感度
    private String gammaBS; //BS模式下delta对uly价格的敏感度
    private String vegaBS;	//BS模式下期权价格对隐含波动率的敏感度
    private String thetaBS; //BS模式下期权价格对剩余期限的敏感度

    private String lever; //	杠杆倍数
    private String markVol;	//	标记波动率
    private String bidVol;	//	bid波动率
    private String askVol;	//	ask波动率
    private String realVol;	//	已实现波动率（目前该字段暂未启用）
    private String volLv;	//	价平期权的隐含波动率
    private String fwdPx;	//远期价格
    private String ts;	//	数据更新时间，Unix时间戳的毫秒数格式，如 1597026383085

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

    public String getUly() {
        return uly;
    }

    public void setUly(String uly) {
        this.uly = uly;
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public String getGamma() {
        return gamma;
    }

    public void setGamma(String gamma) {
        this.gamma = gamma;
    }

    public String getVega() {
        return vega;
    }

    public void setVega(String vega) {
        this.vega = vega;
    }

    public String getTheta() {
        return theta;
    }

    public void setTheta(String theta) {
        this.theta = theta;
    }

    public String getDeltaBS() {
        return deltaBS;
    }

    public void setDeltaBS(String deltaBS) {
        this.deltaBS = deltaBS;
    }

    public String getGammaBS() {
        return gammaBS;
    }

    public void setGammaBS(String gammaBS) {
        this.gammaBS = gammaBS;
    }

    public String getVegaBS() {
        return vegaBS;
    }

    public void setVegaBS(String vegaBS) {
        this.vegaBS = vegaBS;
    }

    public String getThetaBS() {
        return thetaBS;
    }

    public void setThetaBS(String thetaBS) {
        this.thetaBS = thetaBS;
    }

    public String getLever() {
        return lever;
    }

    public void setLever(String lever) {
        this.lever = lever;
    }

    public String getMarkVol() {
        return markVol;
    }

    public void setMarkVol(String markVol) {
        this.markVol = markVol;
    }

    public String getBidVol() {
        return bidVol;
    }

    public void setBidVol(String bidVol) {
        this.bidVol = bidVol;
    }

    public String getAskVol() {
        return askVol;
    }

    public void setAskVol(String askVol) {
        this.askVol = askVol;
    }

    public String getRealVol() {
        return realVol;
    }

    public void setRealVol(String realVol) {
        this.realVol = realVol;
    }

    public String getVolLv() {
        return volLv;
    }

    public void setVolLv(String volLv) {
        this.volLv = volLv;
    }

    public String getFwdPx() {
        return fwdPx;
    }

    public void setFwdPx(String fwdPx) {
        this.fwdPx = fwdPx;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}
