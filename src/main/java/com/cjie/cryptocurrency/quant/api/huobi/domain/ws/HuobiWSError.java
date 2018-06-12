package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

public class HuobiWSError {

    private String code;

    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public HuobiWSError() {
    }

    public HuobiWSError(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)",this.code,this.msg);
    }
}
