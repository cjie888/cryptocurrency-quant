package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class HuobiBalance {

    private long id;

    private String type;

    private String state;

    @SerializedName("list")
    private List<HuobiBalanceCurrency> currencies;

}
