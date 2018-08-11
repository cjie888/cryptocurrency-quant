package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class HuobiAccount {

    public static final String ACCOUNT_TYPE_SPOT = "spot";

    public static final String ACCOUNT_TYPE_OTC = "otc";
    private long id;

    private String type;

    private String state;

    @SerializedName("user-id")
    private long userId;

}
