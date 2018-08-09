package com.cjie.commons.okex.open.api.bean.account.param;

import java.math.BigDecimal;

public class Withdraw {
    private BigDecimal amount;

    private String currency;

    private Integer target;

    private String withdraw_address;

    private String trade_pwd;

    private BigDecimal fee;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public String getWithdraw_address() {
        return withdraw_address;
    }

    public void setWithdraw_address(String withdraw_address) {
        this.withdraw_address = withdraw_address;
    }

    public String getTrade_pwd() {
        return trade_pwd;
    }

    public void setTrade_pwd(String trade_pwd) {
        this.trade_pwd = trade_pwd;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
}
