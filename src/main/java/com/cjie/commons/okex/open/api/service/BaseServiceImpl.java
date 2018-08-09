package com.cjie.commons.okex.open.api.service;

import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.enums.I18nEnum;
import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


public abstract class BaseServiceImpl {

    @Autowired
    private APIKeyMapper apiKeyMapper;

    private ConcurrentHashMap<String, APIClient> apiClients = new ConcurrentHashMap<>();

    public APIClient getSpotProductAPIClient(String site) {
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
}
