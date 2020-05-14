package com.cjie.cryptocurrency.quant.api.okex.service.swap.impl;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpCancelOrderVO;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrder;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrders;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.ApiOrderVO;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapTradeAPIService;
import com.cjie.cryptocurrency.quant.api.okex.utils.JsonUtils;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SwapTradeAPIServiceImpl implements SwapTradeAPIService {
    @Autowired
    private APIKeyMapper apiKeyMapper;

    @Autowired
    private SwapOrderMapper swapOrderMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, SwapTradeAPI> futuresMarketAPIs = new ConcurrentHashMap<>();

    private SwapTradeAPI getFuturesMarketApi(APIClient apiClient) {
        String site = "okexsub1";
        SwapTradeAPI futuresMarketAPI = futuresMarketAPIs.get(site);
        if (futuresMarketAPI != null) {
            return  futuresMarketAPI;
        }
        futuresMarketAPI = apiClient.createService(SwapTradeAPI.class);
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
            //config.setPrint(true);
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
    public String order(PpOrder ppOrder, String strategy)  {
        APIClient client = getFuturesAPIClient();
        SwapTradeAPI api = getFuturesMarketApi(client);
        log.info("下单参数：：：：：：{}", JsonUtils.convertObject(ppOrder, PpOrder.class));
        String result =  client.executeSync(api.order(JsonUtils.convertObject(ppOrder, PpOrder.class)));

        log.info("order result:{}", result);
        ApiOrderVO apiOrderVO = JSON.parseObject(result, ApiOrderVO.class);
        if (apiOrderVO.getError_code().equals("0")) {
            SwapOrder swapOrder = new SwapOrder();
            swapOrder.setInstrumentId(ppOrder.getInstrument_id());
            swapOrder.setCreateTime(new Date());
            swapOrder.setStrategy(strategy);
            swapOrder.setIsMock(Byte.valueOf("0"));
            swapOrder.setType(Byte.valueOf(ppOrder.getType()));
            swapOrder.setPrice(new BigDecimal(ppOrder.getPrice()));
            swapOrder.setSize(new BigDecimal(ppOrder.getSize()));
            swapOrder.setOrderId(apiOrderVO.getOrder_id());
            swapOrder.setStatus(99);
            swapOrderMapper.insert(swapOrder);
        }
        return result;
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
