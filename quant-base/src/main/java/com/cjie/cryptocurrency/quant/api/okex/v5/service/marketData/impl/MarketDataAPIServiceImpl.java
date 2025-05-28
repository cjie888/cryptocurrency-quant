package com.cjie.cryptocurrency.quant.api.okex.v5.service.marketData.impl;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.OrderBook;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.v5.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.v5.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.impl.AccountAPI;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.marketData.MarketDataAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MarketDataAPIServiceImpl extends BaseServiceImpl implements MarketDataAPIService {

    private ConcurrentHashMap<String, MarketDataAPI> marketDataAPIs = new ConcurrentHashMap<>();


    public MarketDataAPI getMarketDataApi(String site, APIClient apiClient) {
        MarketDataAPI marketDataAPI = marketDataAPIs.get(site);
        if (marketDataAPI != null) {
            return  marketDataAPI;
        }
        marketDataAPI = apiClient.createService(MarketDataAPI.class);
        marketDataAPIs.put(site, marketDataAPI);
        return marketDataAPI;
    }


    //获取所有产品行情信息 Get Tickers
    @Override
    public JSONObject getTickers(String site, String instType, String uly) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getTickers(instType,uly));
    }

    //获取单个产品行情信息 Get Ticker
    @Override
    public HttpResult<List<Ticker>> getTicker(String site, String instId) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getTicker(instId));
    }

    //获取指数行情数据 Get Index Tickers
    @Override
    public JSONObject getIndexTickers(String site, String quoteCcy, String instId) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getIndexTickers(quoteCcy,instId));
    }

    //获取产品深度 Get Order Book
    @Override
    public HttpResult<List<OrderBook>> getOrderBook(String site, String instId, String sz) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getOrderBook(instId,sz));
    }

    //获取所有交易产品K线数据 Get Candlesticks
    @Override
    public HttpResult<List<String[]>> getCandlesticks(String site, String instId, String after, String before, String bar, String limit) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getCandlesticks(instId,after,before,bar,limit));
    }

    //获取交易产品历史K线数据（仅主流币） Get Candlesticks History（top currencies only）
    @Override
    public JSONObject getCandlesticksHistory(String site, String instId, String after, String before, String bar, String limit) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getCandlesticksHistory(instId,after,before,bar,limit));
    }

    //获取指数K线数据 Get Index Candlesticks
    @Override
    public JSONObject getIndexCandlesticks(String site, String instId, String after, String before, String bar, String limit) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getIndexCandlesticks(instId,after,before,bar,limit));
    }

    //获取标记价格K线数据 Get Mark Price Candlesticks
    @Override
    public JSONObject getMarkPriceCandlesticks(String site, String instId, String after, String before, String bar, String limit) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getMarkPriceCandlesticks(instId, after, before, bar, limit));
    }

    //获取交易产品公共成交数据 Get Trades
    @Override
    public JSONObject getTrades(String site, String instId, String limit) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getTrades(instId,limit));
    }

    //获取平台24小时总成交量 Get total volume
    @Override
    public JSONObject getTotalVolume(String site) {
        APIClient client = getTradeAPIClient(site);
        MarketDataAPI marketDataAPI = getMarketDataApi(site, client);
        return client.executeSync(marketDataAPI.getTotalVolume());
    }

}
