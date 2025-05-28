package com.cjie.cryptocurrency.quant.api.okex.v5.service.publicData.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.InstrumentInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.OptionMarketData;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.PriceLimitData;
import com.cjie.cryptocurrency.quant.api.okex.v5.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.v5.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.publicData.PublicDataAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class PublicDataAPIServiceImpl  extends BaseServiceImpl implements PublicDataAPIService {

    private ConcurrentHashMap<String, PublicDataAPI> publicDataAPIs = new ConcurrentHashMap<>();

    public PublicDataAPI getPublicDataApi(String site, APIClient apiClient) {
        PublicDataAPI publicDataApi = publicDataAPIs.get(site);
        if (publicDataApi != null) {
            return  publicDataApi;
        }
        publicDataApi = apiClient.createService(PublicDataAPI.class);
        publicDataAPIs.put(site, publicDataApi);
        return publicDataApi;
    }


    //获取交易产品基础信息 Get Instruments
    @Override
    public HttpResult<List<InstrumentInfo>> getInstruments(String site, String instType, String uly) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getInstruments(instType, uly));
    }

    //获取交割和行权记录 Get Delivery/Exercise History
    @Override
    public JSONObject getDeliveryExerciseHistory(String site,String instType, String uly, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getDeliveryExerciseHistory(instType, uly, after,before,limit));
    }

    //获取持仓总量 Get Open Interest
    @Override
    public JSONObject getOpenInterest(String site,String instType, String uly, String instId) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getOpenInterest(instType, uly, instId));
    }

    //获取永续合约当前资金费率 Get Funding Rate
    @Override
    public JSONObject getFundingRate(String site, String instId) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getFundingRate(instId));
    }

    //获取永续合约历史资金费率 Get Funding Rate History
    @Override
    public JSONObject getFundingRateHistory(String site,String instId, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getFundingRateHistory(instId,after,before,limit));
    }

    //获取限价 Get Limit Price
    @Override
    public HttpResult<List<PriceLimitData>>  getLimitPrice(String site, String instId) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getLimitPrice(instId));
    }

    //获取期权定价 Get Option Market Data
    @Override
    public HttpResult<List<OptionMarketData>> getOptionMarketData(String site, String uly, String expTime) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getOptionMarketData(uly,expTime));
    }

    //获取预估交割/行权价格 Get Estimated Delivery/Excercise Price
    @Override
    public JSONObject getEstimatedDeliveryExcercisePrice(String site,String instId) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getEstimatedDeliveryExcercisePrice(instId));
    }

    //获取免息额度和币种折算率 Get Discount Rate And Interest-Free Quota
    @Override
    public JSONObject getDiscountRateAndInterestFreeQuota(String site,String ccy) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getDiscountRateAndInterestFreeQuota(ccy));
    }

    //获取系统时间 Get System Time
    @Override
    public JSONObject getSystemTime(String site) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getSystemTime());
    }

    //获取平台公共爆仓单信息 Get Liquidation Orders
    @Override
    public JSONObject getLiquidationOrders(String site,String instType, String mgnMode, String instId, String ccy, String uly, String alias, String state, String before, String after, String limit) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getLiquidationOrders(instType,mgnMode,instId,ccy,uly,alias,state,before,after,limit));
    }

    //获取标记价格 Get Mark Price
    @Override
    public JSONObject getMarkPrice(String site,String instType, String uly, String instId) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getMarkPrice(instType,uly,instId));
    }

    //获取合约衍生品仓位档位
    @Override
    public JSONObject getTier(String site,String instType, String uly, String instId, String tdMode, String ccy, String tier) {
        APIClient client = getTradeAPIClient(site);
        PublicDataAPI api = getPublicDataApi(site, client);
        return client.executeSync(api.getTier(instType, uly, instId, tdMode, ccy, tier));
    }
}
