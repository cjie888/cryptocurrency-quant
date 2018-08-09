package com.cjie.cryptocurrency.quant.service.impl;

import com.cjie.cryptocurrency.quant.mapper.APIKeyMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.service.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private ConcurrentHashMap<String, APIKey> apiKeys = new ConcurrentHashMap<>();

    @Autowired
    private APIKeyMapper apiKeyMapper;


    @Override
    public APIKey getApiKey(String site) {
        APIKey apiKey = apiKeys.get(site);
        if (apiKey == null) {
            apiKey = apiKeyMapper.selectBySite(site);
            if (apiKey != null) {
                apiKeys.put(site, apiKey);
            }
        }
        return apiKey;
    }
}
