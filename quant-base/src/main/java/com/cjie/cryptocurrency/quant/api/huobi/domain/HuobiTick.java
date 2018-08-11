package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class HuobiTick {

    private long id;

    private long ts;

    private double close;

    private double open;

    private double high;

    private double low;

    private double amount;

    private int count;

    private double vol;

    /**
     * [price,qty]
     */
    @SerializedName("ask")
    private ArrayList<Double> asks;

    /**
     * [price,qty]
     */
    @SerializedName("bid")
    private ArrayList<Double> bids;

}
