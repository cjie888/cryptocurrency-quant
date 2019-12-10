package com.cjie.cryptocurrency.quant.api.okex.service.swap.impl;

import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpCancelOrderVO;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrder;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrders;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapTradeAPIService;
import com.cjie.cryptocurrency.quant.api.okex.utils.JsonUtils;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SwapTradeAPIServiceImpl implements SwapTradeAPIService {
    @Autowired
    private APIKeyMapper apiKeyMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, SwapTradeAPI> futuresMarketAPIs = new ConcurrentHashMap<>();

    private SwapTradeAPI getFuturesMarketApi(APIClient apiClient) {
        String site = "okex";
        SwapTradeAPI futuresMarketAPI = futuresMarketAPIs.get(site);
        if (futuresMarketAPI != null) {
            return  futuresMarketAPI;
        }
        futuresMarketAPI = apiClient.createService(SwapTradeAPI.class);
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
     * 下单
     *
     * @param ppOrder
     * @return
     */
    @Override
    public String order(PpOrder ppOrder)  {
        APIClient client = getFuturesAPIClient();
        SwapTradeAPI api = getFuturesMarketApi(client);
        System.out.println("下单参数：：：：：：");
        System.out.println(JsonUtils.convertObject(ppOrder, PpOrder.class));
        return client.executeSync(api.order(JsonUtils.convertObject(ppOrder, PpOrder.class)));
    }

    /**
     * 批量下单
     *
     * @param ppOrders
     * @return
     */
    @Override
    public String orders(PpOrders ppOrders) {
        APIClient client = getFuturesAPIClient();
        SwapTradeAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.orders(JsonUtils.convertObject(ppOrders, PpOrders.class)));
    }

    /**
     * 撤单
     *
     * @param instrumentId
     * @param orderId
     * @return
     */
    @Override
    public String cancelOrder(String instrumentId, String orderId) {
        APIClient client = getFuturesAPIClient();
        SwapTradeAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.cancelOrder(instrumentId,orderId));
    }

    /**
     * 批量撤单
     *
     * @param instrumentId
     * @param ppCancelOrderVO
     * @return
     */
    @Override
    public String cancelOrders(String instrumentId, PpCancelOrderVO ppCancelOrderVO) {
        APIClient client = getFuturesAPIClient();
        SwapTradeAPI api = getFuturesMarketApi(client);
        return client.executeSync(api.cancelOrders(instrumentId,JsonUtils.convertObject(ppCancelOrderVO, PpCancelOrderVO.class)));
    }
}