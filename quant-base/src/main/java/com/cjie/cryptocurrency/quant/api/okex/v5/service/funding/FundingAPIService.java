package com.cjie.cryptocurrency.quant.api.okex.v5.service.funding;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.FundsTransfer;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.PiggyBankPurchaseRedemption;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.Withdrawal;

public interface FundingAPIService {

    //获取充值地址信息 Get Deposit Address
    JSONObject getDepositAddress(String site,String ccy);

    //获取资金账户余额信息
    JSONObject getBalance(String site,String ccy);

    //资金划转  Funds Transfer
    JSONObject fundsTransfer(String site,FundsTransfer fundsTransfer);

    //提币 Withdrawal
    JSONObject Withdrawal(String site,Withdrawal withdrawal);

    //充值记录 Get Deposit History
    JSONObject getDepositHistory(String site,String ccy, String state, String after, String before, String limit);

    //提币记录 Get Withdrawal History
    JSONObject getWithdrawalHistory(String site,String ccy, String state, String after, String before, String limit);

    //获取币种列表 Get Currencies
    JSONObject getCurrencies(String site);

    //余币宝申购/赎回 PiggyBank Purchase/Redemption
    JSONObject piggyBankPurchaseRedemption(String site,PiggyBankPurchaseRedemption piggyBankPurchaseRedemption);

    //资金流水查询 Asset Bills Details
    JSONObject assetBillsDetails(String site,String ccy, String type, String after, String before, String limit);
}
