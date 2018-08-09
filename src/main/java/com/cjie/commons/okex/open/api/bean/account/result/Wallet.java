package com.cjie.commons.okex.open.api.bean.account.result;

import java.math.BigDecimal;

public class Wallet {

    private String currency;

    private BigDecimal balance;

    private BigDecimal holds;

    private BigDecimal available;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getHolds() {
        return holds;
    }

    public void setHolds(BigDecimal holds) {
        this.holds = holds;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }
}
