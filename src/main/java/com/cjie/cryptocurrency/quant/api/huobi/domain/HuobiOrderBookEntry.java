package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(using = HuobiOrderBookEntryDeserializer.class)
public class HuobiOrderBookEntry {

    private double price;

    private double qty;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public HuobiOrderBookEntry() {
    }

    public HuobiOrderBookEntry(double price, double qty) {
        this.price = price;
        this.qty = qty;
    }

    public static HuobiOrderBookEntry parseOne(List<Double> list){
        return new HuobiOrderBookEntry(list.get(0),list.get(1));
    }

    public static List<HuobiOrderBookEntry> parseMany(List<List<Double>> list){
        return list.stream().map( (e)-> parseOne(e)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", String.valueOf(price), String.valueOf(qty));
    }
}
