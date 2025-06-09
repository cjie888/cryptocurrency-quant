package com.cjie.cryptocurrency.quant.api.okex.v5.service.account;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.IncreaseDecreaseMargin;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.SetLeverage;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.SetPositionMode;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param.SetTheDisplayTypeOfGreeks;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.PositionInfo;
import io.netty.handler.codec.http.HttpObject;
import retrofit2.http.Query;

import java.util.List;


public interface AccountAPIV5Service {

    //查看账户持仓风险 Get account and position risk
    JSONObject getAccountAndPosition(String site, String instType);

    //查看账户余额 Get Balance
    HttpResult<List<AccountInfo>> getBalance(String site, String ccy);

    //查看持仓信息 Get Positions
    HttpResult<List<PositionInfo>> getPositions(String site, String instType, String instId, String posId);

    HttpResult<List<PositionInfo>> getHistoryPostions(String site, String instType, String instId, String ccy, String mgnMode, String ctType, String type, String subType, String after, String before, String limit);


    //账单流水查询（近七天） Get Bills Details (last 7 days)
    JSONObject getBillsDetails7Days(String site, String instType, String instId, String ccy, String mgnMode, String ctType, String type, String subType, String after, String before, String limit);

    //账单流水查询（近七天） Get Bills Details (last 3 months)
    JSONObject getBillsDetails3Months(String site, String instType, String ccy, String mgnMode, String ctType, String type, String subType, String after, String before, String limit);

    //查看账户配置 Get Account Configuration
    JSONObject getAccountConfiguration(String site);

    //设置持仓模式 Set Position mode
    JSONObject setPositionMode(String site, SetPositionMode setPositionMode);

    //设置杠杆倍数 Set Leverage
    JSONObject setLeverage(String site, SetLeverage setLeverage);

    //获取最大可买卖/开仓数量 Get maximum buy/sell amount or open amount
    JSONObject getMaximumTradableSizeForInstrument(String site, String instId, String tdMode, String ccy, String px);

    //获取最大可用数量 Get Maximum Tradable Size For Instrument
    JSONObject getMaximumAvailableTradableAmount(String site, String instId, String tdMode, String ccy, String reduceOnly);

    //调整保证金 Increase/Decrease margin
     JSONObject increaseDecreaseMargin(String site, IncreaseDecreaseMargin increaseDecreaseMargin);

    //获取杠杆倍数 Get Leverage
    JSONObject getLeverage(String site, String instId, String mgnMode);

    //获取交易产品最大可借 Get the maximum loan of instrument
    JSONObject getTheMaximumLoanOfIsolatedMARGIN(String site, String instId, String mgnMode, String mgnCcy);

    //获取当前账户交易手续费费率 Get Fee Rates
    JSONObject getFeeRates(String site, String instType, String instId, String uly, String category);

    //获取计息记录 Get interest-accrued
    JSONObject getInterestAccrued(String site, String instId, String ccy, String mgnMode, String after, String before, String limit);

    //获取用户当前杠杆借币利率 Get interest rate
    JSONObject getInterestRate(String site, String ccy);
    //期权希腊字母PA/BS切换 Set the display type of Greeks
    JSONObject setTheDisplayTypeOfGreeks(String site, SetTheDisplayTypeOfGreeks setTheDisplayTypeOfGreeks);

    //查看账户最大可转余额 Get Maximum Withdrawals
    JSONObject getMaximumWithdrawals(String site, String ccy);
}
