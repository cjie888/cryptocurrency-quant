package com.cjie.cryptocurrency.quant.api.okex.service.perpetual;

import com.cjie.cryptocurrency.quant.api.okex.bean.perpetual.result.PerputalInstrument;

import java.util.List;

/**
 * @author hucj
 * @version 1.0.0
 * @date 2018/12/3 16:06
 */
public interface PerpetualFuturesMarketAPIService {

    /**
     * Get all of futures contract list
     */
    List<PerputalInstrument> getInstruments();


}
