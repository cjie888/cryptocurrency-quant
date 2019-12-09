package com.cjie.cryptocurrency.quant.api.okex.service.swap.impl;

import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.LevelRateParam;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapUserAPIServive;
import com.cjie.cryptocurrency.quant.api.okex.utils.JsonUtils;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SwapUserAPIServiceImpl implements SwapUserAPIServive {
    @Autowired
    private APIKeyMapper apiKeyMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, SwapUserAPI> futuresMarketAPIs = new ConcurrentHashMap<>();

    private SwapUserAPI getFuturesMarketApi(APIClient apiClient) {
        String site = "okex";
        SwapUserAPI futuresMarketAPI = futuresMarketAPIs.get(site);
        if (futuresMarketAPI != null) {
            return  futuresMarketAPI;
        }
        futuresMarketAPI = apiClient.createService(SwapUserAPI.class);
        futuresMarketAPIs.put(site, futuresMarketAPI);
        return futuresMarketAPI;
    }

    private APIClient getFuturesAPIClient() {
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
            config.setPrint(false);
            config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);
            apiClient = new APIClient(config);
            apiClients.put(site, apiClient);
        }
        return apiClient;
    }

    /**
     * 获取单个合约持仓信息
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getPosition(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getPosition(instrumentId));
    }

    /**
     * 获取所有币种合约的账户信息
     *
     * @return
     */
    @Override
    public String getAccounts() {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getAccounts());
    }

    /**
     * 获取某个币种合约的账户信息
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String selectAccount(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.selectAccount(instrumentId));
    }

    /**
     * 获取某个合约的用户配置
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String selectContractSettings(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.selectContractSettings(instrumentId));
    }

    /**
     * 设定某个合约的杠杆
     *
     * @param instrumentId
     * @param levelRateParam
     * @return
     */
    @Override
    public String updateLevelRate(String instrumentId, LevelRateParam levelRateParam) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.updateLevelRate(instrumentId, JsonUtils.convertObject(levelRateParam, LevelRateParam.class)));
    }

    /**
     * 获取所有订单列表
     *
     * @param instrumentId
     * @param status
     * @param from
     * @param to
     * @param limit
     * @return
     */
    @Override
    public String selectOrders(String instrumentId, String status, String from, String to, String limit) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.selectOrders(instrumentId, status, from, to, limit));
    }

    /**
     * 通过订单id获取单个订单信息
     *
     * @param instrumentId
     * @param orderId
     * @return
     */
    @Override
    public String selectOrder(String instrumentId, String orderId) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.selectOrder(instrumentId, orderId));
    }

    /**
     * 获取最近的成交明细列表
     *
     * @param instrumentId
     * @param orderId
     * @param from
     * @param to
     * @param limit
     * @return
     */
    @Override
    public String selectDealDetail(String instrumentId, String orderId, String from, String to, String limit) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.selectDealDetail(instrumentId, orderId, from, to, limit));
    }

    /**
     * 列出账户资产流水，账户资产流水是指导致账户余额增加或减少的行为。
     * 流水会分页，每页100条数据，并且按照时间倒序排序和存储，最新的排在最前面。
     *
     * @param instrumentId
     * @param from
     * @param to
     * @param limit
     * @return
     */
    @Override
    public String getLedger(String instrumentId, String from, String to, String limit) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getLedger(instrumentId, from, to, limit));
    }

    /**
     * 获取合约挂单冻结数量
     *
     * @param instrumentId
     * @return
     */
    @Override
    public String getHolds(String instrumentId) {
        APIClient client = getFuturesAPIClient();
        SwapUserAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.getHolds(instrumentId));
    }
}
