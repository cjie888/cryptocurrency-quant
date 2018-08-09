package com.cjie.cryptocurrency.quant.service;

import com.cjie.cryptocurrency.quant.model.APIKey;

public interface ApiKeyService {
    APIKey getApiKey(String site);
}
