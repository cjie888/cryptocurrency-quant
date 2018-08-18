package com.cjie.cryptocurrency.quant.api.okex.service.account;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Currency;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.WithdrawFee;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Withdraw;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Currency;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Ledger;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.WithdrawFee;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Currency;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.WithdrawFee;

import java.util.List;


public interface AccountAPIService {

    JSONObject transfer(String site, Transfer transfer);

    JSONObject withdraw(String site, Withdraw withdraw);

    List<Currency> getCurrencies(String site);

    List<Ledger> getLedger(String site, Integer type, String currency, Integer before, Integer after, int limit);

    List<Wallet> getWallet(String site);

    List<Wallet> getWallet(String site, String currency);

    JSONArray getDepositAddress(String site, String currency);

    List<WithdrawFee> getWithdrawFee(String site, String currency);

}
