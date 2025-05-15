package com.cjie.cryptocurrency.quant.api.okex.v5.service.publicData;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.InstrumentInfo;
import retrofit2.http.Query;

import java.util.List;

public interface PublicDataAPIService {

    //获取交易产品基础信息 Get Instruments
    HttpResult<List<InstrumentInfo>> getInstruments(String site, String instType, String uly);

    //获取交割和行权记录 Get Delivery/Exercise History
    JSONObject getDeliveryExerciseHistory(String site,String instType, String uly, String after, String before, String limit);

    //获取持仓总量 Get Open Interest
    JSONObject getOpenInterest(String site,String instType, String uly, String instId);

    //获取永续合约当前资金费率 Get Funding Rate
    JSONObject getFundingRate(String site,String instId);

    //获取永续合约历史资金费率 Get Funding Rate History
    JSONObject getFundingRateHistory(String site,String instId, String after, String before, String limit);

    //获取限价 Get Limit Price
    JSONObject getLimitPrice(String site,String instId);

    //获取期权定价 Get Option Market Data
    JSONObject getOptionMarketData(String site,String uly, String expTime);

    //获取预估交割/行权价格 Get Estimated Delivery/Excercise Price
    JSONObject getEstimatedDeliveryExcercisePrice(String site,String instId);

    //获取免息额度和币种折算率 Get Discount Rate And Interest-Free Quota
    JSONObject getDiscountRateAndInterestFreeQuota(String site,String ccy);

    //获取系统时间 Get System Time
    JSONObject getSystemTime(String site);

    //获取平台公共爆仓单信息 Get Liquidation Orders
    JSONObject getLiquidationOrders(String site,String instType, String mgnMode, String instId, String ccy, String uly, String alias, String state, String before, String after, String limit);

    //获取标记价格 Get Mark Price
    JSONObject getMarkPrice(String site,String instType, String uly, String instId);

    //获取合约衍生品仓位档位
    JSONObject getTier(String site,String instType, String uly, String instId, String tdMode, String ccy, String tier);
}
