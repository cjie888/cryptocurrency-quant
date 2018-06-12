package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

public class HuobiWSSub {

    private String sub;

    private String id;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HuobiWSSub() {
    }

    public HuobiWSSub(String sub, String id) {
        this.sub = sub;
        this.id = id;
    }
}
