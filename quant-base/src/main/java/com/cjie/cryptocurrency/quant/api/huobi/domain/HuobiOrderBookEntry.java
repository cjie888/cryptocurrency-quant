package com.cjie.cryptocurrency.quant.api.huobi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class HuobiOrderBookEntry {

    private double price;

    private double qty;



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
