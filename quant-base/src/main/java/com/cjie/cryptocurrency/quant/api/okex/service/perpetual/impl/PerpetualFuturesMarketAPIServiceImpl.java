package com.cjie.cryptocurrency.quant.api.okex.service.perpetual.impl;

import com.cjie.cryptocurrency.quant.api.okex.bean.perpetual.result.PerputalInstrument;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.api.okex.service.perpetual.PerpetualFuturesMarketAPIService;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class PerpetualFuturesMarketAPIServiceImpl implements PerpetualFuturesMarketAPIService {

    @Autowired
    private APIKeyMapper apiKeyMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, PerpetualFuturesMarketAPI> futuresMarketAPIs = new ConcurrentHashMap<>();

    private PerpetualFuturesMarketAPI getFuturesMarketApi(APIClient apiClient) {
        String site = "okex";
        PerpetualFuturesMarketAPI futuresMarketAPI = futuresMarketAPIs.get(site);
        if (futuresMarketAPI != null) {
            return  futuresMarketAPI;
        }
        futuresMarketAPI = apiClient.createService(PerpetualFuturesMarketAPI.class);
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
            config.setPrint(false);
            config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);
            apiClient = new APIClient(config);
            apiClients.put(site, apiClient);
        }
        return apiClient;
    }

    @Override
    public List<PerputalInstrument> getInstruments() {
        APIClient client = getFuturesMarketAPIClient();
        PerpetualFuturesMarketAPI futuresMarketAPI = getFuturesMarketApi(client);
        return client.executeSync(futuresMarketAPI.getInstruments());
    }
}
