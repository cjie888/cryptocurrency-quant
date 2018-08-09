package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.result.Account;
import com.cjie.commons.okex.open.api.bean.spot.result.Ledger;
import com.cjie.commons.okex.open.api.bean.spot.result.ServerTimeDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface SpotAccountAPI {

    @GET("api/spot/v3/time")
    Call<ServerTimeDto> time();

    @GET("api/spot/v3/accounts")
    Call<List<Account>> getAccounts();

    @GET("api/spot/v3/accounts/{currency}")
    Call<Account> getAccountByCurrency(@Path("currency") String currency);

    @GET("api/spot/v3/accounts/{currency}/ledger")
    Call<List<Ledger>> getLedgersByCurrency(@Path("currency") String currency,
                                            @Query("before") Long before,
                                            @Query("after") Long after,
                                            @Query("limit") Integer limit);

}
