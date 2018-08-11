package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderBookEntry;

import java.util.List;

public class HuobiWSOrderBookResp extends HuobiWSResp {

    public static class HuobiOrderBookTick {

        public List<HuobiOrderBookEntry> bids;

        public List<HuobiOrderBookEntry> asks;

        public long ts;

        public long version;

    }

    public HuobiOrderBookTick tick;

    public HuobiWSOrderBookResp() {

    }

}
