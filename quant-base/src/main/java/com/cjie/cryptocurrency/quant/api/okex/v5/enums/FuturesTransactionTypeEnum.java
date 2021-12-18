package com.cjie.cryptocurrency.quant.api.okex.v5.enums;

public enum FuturesTransactionTypeEnum {

    OPEN_LONG(1), OPEN_SHORT(2), CLOSE_LONG(3), CLOSE_SHORT(4),;

    private int code;

    FuturesTransactionTypeEnum(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
