package com.cjie.cryptocurrency.quant.api.okex.v5.service.account.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.IncreaseDecreaseMargin;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.SetLeverage;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.SetPositionMode;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.SetTheDisplayTypeOfGreeks;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.PositionInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.BaseServiceImpl;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AccountAPIV5ServiceImpl extends BaseServiceImpl implements AccountAPIV5Service {

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



    //查看账户持仓风险 Get account and position risk
    @Override
    public JSONObject getAccountAndPosition(String site, String instType) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);

        return client.executeSync(api.getAccountAndPosition(instType));
    }

    //查看账户余额 Get Balance
    @Override
    public HttpResult<List<AccountInfo>> getBalance(String site, String ccy) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getBalance(ccy));
    }

    //查看持仓信息 Get Positions
    @Override
    public HttpResult<List<PositionInfo>>  getPositions(String site, String instType, String instId, String posId) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getPositions(instType,instId,posId));
    }

    //账单流水查询（近七天） Get Bills Details (last 7 days)
    @Override
    public JSONObject getBillsDetails7Days(String site, String instType,String ccy,String mgnMode,String ctType,String type,String subType,String after,String before,String limit) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getBillsDetails7Days(instType,ccy,mgnMode,ctType,type,subType,after,before,limit));
    }

    //账单流水查询（近七天） Get Bills Details (last 3 months)
    @Override
    public JSONObject getBillsDetails3Months(String site, String instType, String ccy, String mgnMode,
                                             String ctType, String type, String subType,
                                             String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getBillsDetails3Months(instType,ccy,mgnMode,ctType,type,subType,after,before,limit));
    }

    //查看账户配置 Get Account Configuration
    @Override
    public JSONObject getAccountConfiguration(String site) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getAccountConfiguration());
    }

    //设置持仓模式 Set Position mode
    @Override
    public JSONObject setPositionMode(String site, SetPositionMode setPositionMode) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.setPositionMode(JSONObject.parseObject(JSON.toJSONString(setPositionMode))));
    }

    //设置杠杆倍数 Set Leverage
    @Override
    public JSONObject setLeverage(String site, SetLeverage setLeverage) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.setLeverage(JSONObject.parseObject(JSON.toJSONString(setLeverage))));
    }

    //获取最大可买卖/开仓数量 Get maximum buy/sell amount or open amount
    @Override
    public JSONObject getMaximumTradableSizeForInstrument(String site, String instId, String tdMode, String ccy, String px) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getMaximumTradableSizeForInstrument(instId,tdMode,ccy,px));
    }

    //获取最大可用数量 Get Maximum Available Tradable Amount
    @Override
    public JSONObject getMaximumAvailableTradableAmount(String site, String instId,
                                                        String tdMode, String ccy, String reduceOnly) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getMaximumAvailableTradableAmount(instId,tdMode,ccy,reduceOnly));
    }

    //调整保证金 Increase/Decrease margin
    @Override
    public JSONObject increaseDecreaseMargin(String site, IncreaseDecreaseMargin increaseDecreaseMargin) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.increaseDecreaseMargin(JSONObject.parseObject(JSON.toJSONString(increaseDecreaseMargin))));
    }

    //获取杠杆倍数 Get Leverage
    @Override
    public JSONObject getLeverage(String site, String instId, String mgnMode) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getLeverage(instId,mgnMode));
    }

    //获取币币逐仓杠杆最大可借 Get the maximum loan of instrument
    @Override
    public JSONObject getTheMaximumLoanOfIsolatedMARGIN(String site, String instId,String mgnMode,String mgnCcy) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getTheMaximumLoanOfIsolatedMARGIN(instId,mgnMode,mgnCcy));
    }

    //获取当前账户交易手续费费率 Get Fee Rates
    @Override
    public JSONObject getFeeRates(String site, String instType, String instId, String uly, String category) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getFeeRates(instType,instId,uly,category));
    }


    //获取计息记录 Get interest-accrued
    @Override
    public JSONObject getInterestAccrued(String site, String instId, String ccy, String mgnMode,
                                         String after, String before, String limit) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getInterestAccrued(instId,ccy,mgnMode,after,before,limit));
    }

    //获取用户当前杠杆借币利率 Get interest rate
    @Override
    public JSONObject getInterestRate(String site, String ccy) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getInterestRate(ccy));
    }

    //期权希腊字母PA/BS切换 Set the display type of Greeks
    @Override
    public JSONObject setTheDisplayTypeOfGreeks(String site, SetTheDisplayTypeOfGreeks setTheDisplayTypeOfGreeks) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.setTheDisplayTypeOfGreeks(JSONObject.parseObject(JSON.toJSONString(setTheDisplayTypeOfGreeks))));
    }

    //查看账户最大可转余额 Get Maximum Withdrawals
    @Override
    public JSONObject getMaximumWithdrawals(String site, String ccy) {
        APIClient client = getTradeAPIClient(site);
        AccountAPI api = getAccountApi(site, client);
        return client.executeSync(api.getMaximumWithdrawals(ccy));
    }
}
