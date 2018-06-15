package com.cjie.cryptocurrency.quant.api.huobi.domain;

import lombok.Data;

@Data
public class HuobiBalanceCurrency {

    public static final String TYPE_TRADE = "trade";

    public static final String TYPE_FROZEN = "frozen";

    private String currency;

    private String type;

    private String balance;
}
