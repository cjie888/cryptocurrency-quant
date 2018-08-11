package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiTradeDetail;

import java.util.List;

public class HuobiWSTradeDetailResp extends HuobiWSResp {

    public static class HuobiWSTradeDetailTick {

        public long id;

        public long ts;

        public List<HuobiTradeDetail> data;

        public HuobiWSTradeDetailTick() {
        }
    }

    public HuobiWSTradeDetailTick tick;

    public HuobiWSTradeDetailResp() {
    }
}
