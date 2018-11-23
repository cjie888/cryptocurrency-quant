package com.cjie.cryptocurrency.quant.api.okex.service.futures.impl;

import com.alibaba.fastjson.JSONArray;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Book;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Liquidation;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.Instrument;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.api.okex.service.futures.FuturesMarketAPIService;
import com.cjie.cryptocurrency.quant.api.okex.bean.futures.result.*;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Futures market api
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 16:09
 */
@Component
@Slf4j
public class FuturesMarketAPIServiceImpl implements FuturesMarketAPIService {

    @Autowired
    private APIKeyMapper apiKeyMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, FuturesMarketAPI> futuresMarketAPIs = new ConcurrentHashMap<>();



    @Override
    public List<Instrument> getProducts() {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProducts());
    }

    private FuturesMarketAPI getFuturesMarketApi(APIClient apiClient) {
        String site = "okex";
        FuturesMarketAPI futuresMarketAPI = futuresMarketAPIs.get(site);
        if (futuresMarketAPI != null) {
            return  futuresMarketAPI;
        }
        futuresMarketAPI = apiClient.createService(FuturesMarketAPI.class);
        futuresMarketAPIs.put(site, futuresMarketAPI);
        return futuresMarketAPI;
    }

    private APIClient getFuturesMarketAPIClient() {
        String site = "okex";
        APIClient apiClient = apiClients.get(site);
        if (apiClient != null) {
            return apiClient;
        }
        APIKey apiKey = apiKeyMapper.selectBySite(site);
        if (apiKey != null) {
            APIConfiguration config = new APIConfiguration();
            config.setEndpoint(apiKey.getDomain());
            config.setApiKey(apiKey.getApiKey());
            config.setSecretKey(apiKey.getApiSecret());
            config.setPassphrase(apiKey.getApiPassphrase());
            config.setPrint(true);
            config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);
            apiClient = new APIClient(config);
            apiClients.put(site, apiClient);
        }
        return apiClient;
    }

    @Override
    public List<Currencies> getCurrencies() {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getCurrencies());
    }

    @Override
    public Book getProductBook(String productId, Integer depth, Integer conflated) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductBook(productId, depth, conflated));
    }

    @Override
    public Ticker getProductTicker(String productId) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductTicker(productId));
    }

    @Override
    public List<Trades> getProductTrades(String productId) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductTrades(productId));
    }

    @Override
    public List<String[]> getProductCandles(String productId, Long start, Long end, long granularity) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductCandles(productId, String.valueOf(start), String.valueOf(end), String.valueOf(granularity)));
    }

    @Override
    public Index getProductIndex(String productId) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductIndex(productId));
    }

    @Override
    public EstimatedPrice getProductEstimatedPrice(String productId) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductEstimatedPrice(productId));
    }

    @Override
    public Holds getProductHolds(String productId) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductHolds(productId));
    }

    @Override
    public PriceLimit getProductPriceLimit(String productId) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductPriceLimit(productId));
    }

    @Override
    public List<Liquidation> getProductLiquidation(String productId, int status) {
        APIClient client = getFuturesMarketAPIClient();
        FuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getProductLiquidation(productId, status));
    }

}
