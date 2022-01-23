package com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding;

public class AssetBalance {

    private String availBal;

    private String bal;

    private String ccy;

    private String frozenBal;


    public String getAvailBal() {
        return availBal;
    }

    public void setAvailBal(String availBal) {
        this.availBal = availBal;
    }

    public String getBal() {
        return bal;
    }

    public void setBal(String bal) {
        this.bal = bal;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public String getFrozenBal() {
        return frozenBal;
    }

    public void setFrozenBal(String frozenBal) {
        this.frozenBal = frozenBal;
    }
}
