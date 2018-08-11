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

    JSONObject transfer(Transfer transfer);

    JSONObject withdraw(Withdraw withdraw);

    List<Currency> getCurrencies();

    List<Ledger> getLedger(Integer type, String currency, Integer before, Integer after, int limit);

    List<Wallet> getWallet();

    List<Wallet> getWallet(String currency);

    JSONArray getDepositAddress(String currency);

    List<WithdrawFee> getWithdrawFee(String currency);

}
