package com.cjie.cryptocurrency.quant.api.okex.v5.enums;

public enum CharsetEnum {

    UTF_8("UTF-8"),
    ISO_8859_1("ISO-8859-1"),;


    private String charset;

    CharsetEnum(String charset) {
        this.charset = charset;
    }

    public String charset() {
        return charset;
    }
}
