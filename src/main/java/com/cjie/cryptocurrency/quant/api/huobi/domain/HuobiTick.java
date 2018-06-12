package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public ArrayList<Double> getAsks() {
        return asks;
    }

    public void setAsks(ArrayList<Double> asks) {
        this.asks = asks;
    }

    public ArrayList<Double> getBids() {
        return bids;
    }

    public void setBids(ArrayList<Double> bids) {
        this.bids = bids;
    }

    public HuobiTick() {
    }
}
