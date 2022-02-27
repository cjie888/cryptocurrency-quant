package com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param;

public class PiggyBankPurchaseRedemption {
    private String ccy;
    private String amt;
    private String side;

    private String rate;

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "PiggyBankPurchaseRedemption{" +
                "ccy='" + ccy + '\'' +
                ", amt='" + amt + '\'' +
                ", side='" + side + '\'' +
                '}';
    }
}
