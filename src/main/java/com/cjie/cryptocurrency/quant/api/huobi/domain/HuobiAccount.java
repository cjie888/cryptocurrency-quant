package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;

public class HuobiAccount {

    public static final String ACCOUNT_TYPE_SPOT = "spot";

    public static final String ACCOUNT_TYPE_OTC = "otc";
    private long id;

    private String type;

    private String state;

    @SerializedName("user-id")
    private long userId;

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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public HuobiAccount() {
    }
}
