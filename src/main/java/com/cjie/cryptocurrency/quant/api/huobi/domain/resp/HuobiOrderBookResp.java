package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import java.util.List;

public class HuobiOrderBookResp extends HuobiResp {

    public static class HuobiOrderBookTick {

        public List<List<Double>> bids;

        public List<List<Double>> asks;

    }

    public HuobiOrderBookTick tick;

    public HuobiOrderBookResp() {
    }
}
