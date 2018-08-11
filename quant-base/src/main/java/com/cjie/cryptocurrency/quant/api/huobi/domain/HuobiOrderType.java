package com.cjie.cryptocurrency.quant.api.huobi.domain;


public enum HuobiOrderType {

    BUY_MARKET("buy-market"),
    SELL_MARKET("sell-market"),
    BUY_LIMIT("buy-limit"),
    SELL_LIMIT("sell-limit");

    private String name;

    public String getName() {
        return name;
    }

    private HuobiOrderType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
