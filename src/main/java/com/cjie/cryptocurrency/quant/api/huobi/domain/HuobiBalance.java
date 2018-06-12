package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HuobiBalance {

    private long id;

    private String type;

    private String state;

    @SerializedName("list")
    private List<HuobiBalanceCurrency> currencies;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<HuobiBalanceCurrency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<HuobiBalanceCurrency> currencies) {
        this.currencies = currencies;
    }

    public HuobiBalance() {
    }
}
