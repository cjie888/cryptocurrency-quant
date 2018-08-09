package com.cjie.commons.okex.open.api.bean.spot.result;

public class Account {

    // 账户id，目前暂时为空
    private Long id;
    // 币种 如：BTC
    private String currency;
    // 账户总资产
    private String balance;
    // 可用余额
    private String available;
    // 账户冻余额
    private String hold;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getHold() {
        return hold;
    }

    public void setHold(String hold) {
        this.hold = hold;
    }
}
