package com.cjie.commons.okex.open.api.service.account.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.commons.okex.open.api.bean.account.result.Currency;
import com.cjie.commons.okex.open.api.bean.account.result.Ledger;
import com.cjie.commons.okex.open.api.bean.account.result.Wallet;
import com.cjie.commons.okex.open.api.bean.account.result.WithdrawFee;
import com.cjie.commons.okex.open.api.bean.account.result.Currency;
import com.cjie.commons.okex.open.api.bean.account.result.Ledger;
import com.cjie.commons.okex.open.api.bean.account.result.Wallet;
import com.cjie.commons.okex.open.api.bean.account.result.WithdrawFee;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Account api
 *
 * @author hucj
 * @version 1.0.0
 * @date 2018/07/04 20:51
 */
public interface AccountAPI {


    @POST("/api/account/v3/transfer")
    Call<JSONObject> transfer(@Body JSONObject jsonObject);


    @POST("/api/account/v3/withdrawals")
    Call<JSONObject> withdraw(@Body JSONObject jsonObject);

    @GET("/api/account/v3/currencies")
    Call<List<Currency>> getCurrencies();

    @GET("/api/account/v3/ledger")
    Call<List<Ledger>> getLedger(@Query("type") Integer type, @Query("currency") String currency,
                                 @Query("before") Integer before, @Query("after") Integer after, @Query("limit") int limit);

    @GET("/api/account/v3/wallet")
    Call<List<Wallet>> getWallet();

    @GET("/api/account/v3/wallet/{currency}")
    Call<List<Wallet>> getWallet(@Path("currency") String currency);

    @GET("/api/account/v3/deposit/address")
    Call<JSONArray> getDepositAddress(@Query("currency") String currency);

    @GET("/api/account/v3/withdrawals/fee")
    Call<List<WithdrawFee>> getWithdrawFee(@Query("currency") String currency);


}
