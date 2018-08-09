package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.result.Account;
import com.cjie.commons.okex.open.api.bean.spot.result.Ledger;
import com.cjie.commons.okex.open.api.bean.spot.result.ServerTimeDto;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.BaseServiceImpl;
import com.cjie.commons.okex.open.api.service.spot.SpotAccountAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SpotAccountAPIServiceImpl extends BaseServiceImpl implements SpotAccountAPIService {

    private ConcurrentHashMap<String, SpotAccountAPI> spotAccountAPIs = new ConcurrentHashMap<>();


    public SpotAccountAPI getSpotAccountApi(String site, APIClient apiClient) {
        SpotAccountAPI spotProductAPI = spotAccountAPIs.get(site);
        if (spotProductAPI != null) {
            return  spotProductAPI;
        }
        spotProductAPI = apiClient.createService(SpotAccountAPI.class);
        spotAccountAPIs.put(site, spotProductAPI);
        return spotProductAPI;
    }

    @Override
    public ServerTimeDto time(String site) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotAccountAPI api = getSpotAccountApi(site, apiClient);
        return apiClient.executeSync(api.time());
    }

    @Override
    public List<Account> getAccounts(String site) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotAccountAPI api = getSpotAccountApi(site, apiClient);
        return apiClient.executeSync(api.getAccounts());
    }

    @Override
    public List<Ledger> getLedgersByCurrency(String site, final String currency, final Long before, final Long after, final Integer limit) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotAccountAPI api = getSpotAccountApi(site, apiClient);
        return apiClient.executeSync(api.getLedgersByCurrency(currency, before, after, limit));
    }

    @Override
    public Account getAccountByCurrency(String site, final String currency) {
        APIClient apiClient = getSpotProductAPIClient(site);
        SpotAccountAPI api = getSpotAccountApi(site, apiClient);
        return apiClient.executeSync(api.getAccountByCurrency(currency));
    }
}
