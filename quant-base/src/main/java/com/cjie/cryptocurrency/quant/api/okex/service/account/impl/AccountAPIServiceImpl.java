package com.cjie.cryptocurrency.quant.api.okex.service.account.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Withdraw;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Currency;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Ledger;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.WithdrawFee;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Withdraw;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Ledger;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Withdraw;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Currency;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Ledger;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.WithdrawFee;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Withdraw;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Currency;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Ledger;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.WithdrawFee;
import com.cjie.cryptocurrency.quant.api.okex.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.impl.SpotAccountAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AccountAPIServiceImpl extends BaseServiceImpl implements AccountAPIService {

    private ConcurrentHashMap<String, AccountAPI> accountAPIs = new ConcurrentHashMap<>();


    public AccountAPI getAccountApi(String site, APIClient apiClient) {
        AccountAPI accountAPI = accountAPIs.get(site);
        if (accountAPI != null) {
            return  accountAPI;
        }
        accountAPI = apiClient.createService(AccountAPI.class);
        accountAPIs.put(site, accountAPI);
        return accountAPI;
    }

    @Override
    public JSONObject transfer(String site, Transfer transfer) {
        log.info("transfer site-{}, {}", site, JSON.toJSONString(transfer));
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.transfer(JSONObject.parseObject(JSON.toJSONString(transfer))));
    }

    @Override
    public JSONObject withdraw(String site, Withdraw withdraw) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.withdraw(JSONObject.parseObject(JSON.toJSONString(withdraw))));
    }

    @Override
    public List<Currency> getCurrencies(String site) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getCurrencies());
    }

    @Override
    public List<Ledger> getLedger(String site, Integer type, String currency, Integer before, Integer after, int limit) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getLedger(type, currency, before, after, limit));
    }

    @Override
    public List<Wallet> getWallet(String site) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getWallet());
    }

    @Override
    public List<Wallet> getWallet(String site, String currency) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getWallet(currency));
    }

    @Override
    public JSONArray getDepositAddress(String site, String currency) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getDepositAddress(currency));
    }

    @Override
    public List<WithdrawFee> getWithdrawFee(String site, String currency) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getWithdrawFee(currency));
    }

    @Override
    public JSONObject purchaseRedempt(String site, String currency, String amount, String side) {
        APIClient client = getSpotProductAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.purchaseRedempt(currency, amount, side));
    }
}
