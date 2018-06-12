package com.cjie.cryptocurrency.quant.api.huobi.domain;

public class HuobiBalanceCurrency {

    public static final String TYPE_TRADE = "trade";

    public static final String TYPE_FROZEN = "frozen";

    private String currency;

    private String type;

    private String balance;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public HuobiBalanceCurrency() {
    }
}
