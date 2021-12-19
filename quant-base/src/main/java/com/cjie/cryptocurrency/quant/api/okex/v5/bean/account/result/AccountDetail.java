package com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result;

public class AccountDetail {

    private String availBal;

    private String ccy;


    private String eq;   // 币种总权益

    private String availEq;

    private String cashBal; //币种余额

    public String getAvailBal() {
        return availBal;
    }

    public void setAvailBal(String availBal) {
        this.availBal = availBal;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public String getEq() {
        return eq;
    }

    public void setEq(String eq) {
        this.eq = eq;
    }

    public String getAvailEq() {
        return availEq;
    }

    public void setAvailEq(String availEq) {
        this.availEq = availEq;
    }

    public String getCashBal() {
        return cashBal;
    }

    public void setCashBal(String cashBal) {
        this.cashBal = cashBal;
    }
}
