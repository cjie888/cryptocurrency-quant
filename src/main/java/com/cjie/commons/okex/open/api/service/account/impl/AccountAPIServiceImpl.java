package com.cjie.commons.okex.open.api.service.account.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.account.param.Transfer;
import com.cjie.commons.okex.open.api.bean.account.param.Withdraw;
import com.cjie.commons.okex.open.api.bean.account.result.Ledger;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.service.account.AccountAPIService;
import com.cjie.commons.okex.open.api.bean.account.param.Transfer;
import com.cjie.commons.okex.open.api.bean.account.param.Withdraw;
import com.cjie.commons.okex.open.api.bean.account.result.Currency;
import com.cjie.commons.okex.open.api.bean.account.result.Ledger;
import com.cjie.commons.okex.open.api.bean.account.result.Wallet;
import com.cjie.commons.okex.open.api.bean.account.result.WithdrawFee;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.account.AccountAPIService;

import java.util.List;

public class AccountAPIServiceImpl implements AccountAPIService {

    private APIClient client;
    private AccountAPI api;

    public AccountAPIServiceImpl(APIConfiguration config) {
        this.client = new APIClient(config);
        this.api = client.createService(AccountAPI.class);
    }

    @Override
    public JSONObject transfer(Transfer transfer) {
        return this.client.executeSync(this.api.transfer(JSONObject.parseObject(JSON.toJSONString(transfer))));
    }

    @Override
    public JSONObject withdraw(Withdraw withdraw) {
        return this.client.executeSync(this.api.withdraw(JSONObject.parseObject(JSON.toJSONString(withdraw))));
    }

    @Override
    public List<Currency> getCurrencies() {
        return this.client.executeSync(this.api.getCurrencies());
    }

    @Override
    public List<Ledger> getLedger(Integer type, String currency, Integer before, Integer after, int limit) {
        return this.client.executeSync(this.api.getLedger(type, currency, before, after, limit));
    }

    @Override
    public List<Wallet> getWallet() {
        return this.client.executeSync(this.api.getWallet());
    }

    @Override
    public List<Wallet> getWallet(String currency) {
        return this.client.executeSync(this.api.getWallet(currency));
    }

    @Override
    public JSONArray getDepositAddress(String currency) {
        return this.client.executeSync(this.api.getDepositAddress(currency));
    }

    @Override
    public List<WithdrawFee> getWithdrawFee(String currency) {
        return this.client.executeSync(this.api.getWithdrawFee(currency));
    }
}
