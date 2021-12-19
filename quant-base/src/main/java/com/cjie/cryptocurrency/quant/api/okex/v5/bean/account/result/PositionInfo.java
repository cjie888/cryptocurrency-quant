package com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result;

public class PositionInfo {

    private String instType; //	String	产品类型
    private String mgnMode;//	String	保证金模式

    private String posId;	//String	持仓ID

    private String posSide;//	String	持仓方向

    private String pos;

    private String availPos;

    private String margin;

    private String mgnRatio;

    private String ccy;

    public String getInstType() {
        return instType;
    }

    public void setInstType(String instType) {
        this.instType = instType;
    }

    public String getMgnMode() {
        return mgnMode;
    }

    public void setMgnMode(String mgnMode) {
        this.mgnMode = mgnMode;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getPosSide() {
        return posSide;
    }

    public void setPosSide(String posSide) {
        this.posSide = posSide;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getAvailPos() {
        return availPos;
    }

    public void setAvailPos(String availPos) {
        this.availPos = availPos;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public String getMgnRatio() {
        return mgnRatio;
    }

    public void setMgnRatio(String mgnRatio) {
        this.mgnRatio = mgnRatio;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }
}
