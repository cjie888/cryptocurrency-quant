package com.cjie.cryptocurrency.quant.api.huobi.domain;

public enum  HuobiOrderState {
    SUBMITED("submitted"),
    PARTIAL_FILLED("partial-filled"),
    PARTIAL_CANCELED("partial-canceled"),
    FILLED("filled"),
    CANCELED("canceled");

    private String name;

    public String getName() {
        return name;
    }

    private HuobiOrderState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
