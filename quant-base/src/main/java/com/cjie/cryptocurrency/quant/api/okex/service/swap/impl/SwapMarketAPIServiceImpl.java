package com.cjie.cryptocurrency.quant.api.okex.service.swap.impl;

import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class SwapMarketAPIServiceImpl implements SwapMarketAPIService {
    @Autowired
    private APIKeyMapper apiKeyMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, SwapMarketAPI> futuresMarketAPIs = new ConcurrentHashMap<>();

    private SwapMarketAPI getFuturesMarketApi(APIClient apiClient) {
        String site = "okexsub1";
        SwapMarketAPI futuresMarketAPI = futuresMarketAPIs.get(site);
        if (futuresMarketAPI != null) {
            return  futuresMarketAPI;
        }
        futuresMarketAPI = apiClient.createService(SwapMarketAPI.class);
        futuresMarketAPIs.put(site, futuresMarketAPI);
        return futuresMarketAPI;
    }

    private APIClient getFuturesAPIClient() {
        String site = "okexsub1";
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
            config.setPrint(false);
            config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);
            apiClient = new APIClient(config);
            apiClients.put(site, apiClient);
        }
        return apiClient;
    }

    /**
     * 获取可用合约的列表。
     *
     * @return
     */
    @Override
    public String getContractsApi() {

        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getContractsApi());
    }

    /**
     * 获取合约的深度列表。
     *
     * @param instrumentId
     * @param size
     * @return
     */
    @Override
    public String getDepthApi(String instrumentId, String size) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getDepthApi(instrumentId, size));
    }

    /**
     * 获取平台全部合约的最新成交价、买一价、卖一价和24交易量。
     *
     * @return
     */
    @Override
    public String getTickersApi() {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getTickersApi());
    }

    /**
     * 获取合约的最新成交价、买一价、卖一价和24交易量。
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getTickerApi(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getTickerApi(instrumentId));
    }

    /**
     * 获取合约的成交记录。
     *
     * @param instrumentId
     * @param from
     * @param to
     * @param limit
     * @return
     */
    @Override
    public String getTradesApi(String instrumentId, String from, String to, String limit) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getTradesApi(instrumentId, from, to, limit));
    }

    /**
     * 获取合约的K线数据。
     *
     * @param instrumentId
     * @param start
     * @param end
     * @param granularity
     * @return
     */
    @Override
    public String getCandlesApi(String instrumentId, String start, String end, String granularity) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getCandlesApi(instrumentId, start, end, granularity));
    }

    /**
     * 获取币种指数。
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getIndexApi(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getIndexApi(instrumentId));
    }

    /**
     * 获取法币汇率。
     *
     * @return
     */
    @Override
    public String getRateApi() {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getRateApi());
    }

    /**
     * 获取合约整个平台的总持仓量。
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getOpenInterestApi(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getOpenInterestApi(instrumentId));
    }

    /**
     * 获取合约当前开仓的最高买价和最低卖价。
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getPriceLimitApi(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getPriceLimitApi(instrumentId));
    }

    /**
     * 获取合约爆仓单。
     *
     * @param instrumentId
     * @param status
     * @param from
     * @param to
     * @param limit
     * @return
     */
    @Override
    public String getLiquidationApi(String instrumentId, String status, String from, String to, String limit) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getLiquidationApi(instrumentId, status, from, to, limit));
    }

    /**
     * 获取合约下一次的结算时间。
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getFundingTimeApi(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getFundingTimeApi(instrumentId));
    }

    /**
     * 获取合约历史资金费率
     *
     * @param instrumentId
     * @param from
     * @param to
     * @param limit
     * @return
     */
    @Override
    public String getHistoricalFundingRateApi(String instrumentId, String from, String to, String limit) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getHistoricalFundingRateApi(instrumentId, from, to, limit));
    }

    /**
     * 获取合约标记价格
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getMarkPriceApi(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapMarketAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getMarkPriceApi(instrumentId));
    }
}
