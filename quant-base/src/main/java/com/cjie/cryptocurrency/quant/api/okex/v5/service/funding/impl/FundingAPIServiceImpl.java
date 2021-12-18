package com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.FundsTransfer;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.PiggyBankPurchaseRedemption;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.Withdrawal;
import com.cjie.cryptocurrency.quant.api.okex.v5.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.FundingAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FundingAPIServiceImpl extends BaseServiceImpl implements FundingAPIService {

    private ConcurrentHashMap<String, FundingAPI> fundingAPIs = new ConcurrentHashMap<>();


    public FundingAPI getFundingApi(String site, APIClient apiClient) {
        FundingAPI fundingAPI = fundingAPIs.get(site);
        if (fundingAPI != null) {
            return  fundingAPI;
        }
        fundingAPI = apiClient.createService(FundingAPI.class);
        fundingAPIs.put(site, fundingAPI);
        return fundingAPI;
    }


    //获取充值地址信息 Get Deposit Address
    @Override
    public JSONObject getDepositAddress(String site,String ccy) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.getDepositAddress(ccy));
    }

    //获取资金账户余额信息 Get Balance
    @Override
    public JSONObject getBalance(String site,String ccy) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.getBalance(ccy));
    }

    //资金划转  Funds Transfer
    @Override
    public JSONObject fundsTransfer(String site,FundsTransfer fundsTransfer) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.fundsTransfer(JSONObject.parseObject(JSON.toJSONString(fundsTransfer))));
    }

    //提币 Withdrawal
    @Override
    public JSONObject Withdrawal(String site,Withdrawal withdrawal) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.Withdrawal(JSONObject.parseObject(JSON.toJSONString(withdrawal))));
    }

    //充值记录 Get Deposit History
    @Override
    public JSONObject getDepositHistory(String site, String ccy, String state, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.getDepositHistory(ccy,state,after,before,limit));
    }

    //提币记录 Get Withdrawal History

    @Override
    public JSONObject getWithdrawalHistory(String site, String ccy, String state, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.getWithdrawalHistory(ccy,state,after,before,limit));
    }

    //获取币种列表 Get Currencies
    @Override
    public JSONObject getCurrencies(String site) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.getCurrencies());
    }

    //余币宝申购/赎回 PiggyBank Purchase/Redemption
    @Override
    public JSONObject piggyBankPurchaseRedemption(String site, PiggyBankPurchaseRedemption piggyBankPurchaseRedemption) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.piggyBankPurchaseRedemption(JSONObject.parseObject(JSON.toJSONString(piggyBankPurchaseRedemption))));
    }

    //资金流水查询 Asset Bills Details
    @Override
    public JSONObject assetBillsDetails(String site, String ccy, String type, String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        FundingAPI api = getFundingApi(site, client);
        return client.executeSync(api.assetBillsDetails(ccy,type,after,before,limit));
    }
}
