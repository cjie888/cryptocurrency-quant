package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderBookEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties
public class HuobiWSOrderBookResp extends HuobiWSResp {

    @JsonIgnoreProperties
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
