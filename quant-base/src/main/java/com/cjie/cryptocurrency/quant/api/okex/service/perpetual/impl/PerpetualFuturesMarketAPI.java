package com.cjie.cryptocurrency.quant.api.okex.service.perpetual.impl;

import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Instrument;
import com.cjie.cryptocurrency.quant.api.okex.bean.perpetual.result.PerputalInstrument;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface PerpetualFuturesMarketAPI {

    @GET("/api/swap/v3/instruments")
    Call<List<PerputalInstrument>> getInstruments();
}
