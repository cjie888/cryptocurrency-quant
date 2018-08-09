package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.result.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * 杠杆账号测试
 */
public interface MarginAccountAPI {
    /**
     * 全部杠杆资产
     *
     * @return
     */
    @GET("/api/margin/v3/accounts")
    Call<List<MarginAccountDto>> getAccounts();

    /**
     * 单个币对杠杆账号资产
     *
     * @param product
     * @return
     */
    @GET("/api/margin/v3/accounts/{product_id}")
    Call<List<MarginAccountDetailDto>> getAccountsByProductId(@Path("product_id") final String product);

    /**
     * 杠杆账单明细
     *
     * @param product
     * @param type
     * @param before
     * @param after
     * @param limit
     * @return
     */
    @GET("/api/margin/v3/accounts/{product_id}/ledger")
    Call<List<UserMarginBillDto>> getLedger(@Path("product_id") final String product,
                                            @Query("type") final Integer type,
                                            @Query("before") final Long before,
                                            @Query("after") final Long after,
                                            @Query("limit") Integer limit);

    /**
     * 全部币对配置
     *
     * @return
     */
    @GET("/api/margin/v3/accounts/margin_info")
    Call<List<BorrowConfigDto>> getMarginInfo();

    /**
     * 单个币对配置
     *
     * @param product
     * @return
     */
    @GET("/api/margin/v3/accounts/{product_id}/margin_info")
    Call<List<BorrowConfigDto>> getMarginInfoByProductId(@Path("product_id") final String product);

    /**
     * 全部借币历史
     * @param status
     * @param before
     * @param after
     * @param limit
     * @return
     */
    @GET("/api/margin/v3/accounts/borrow")
    Call<List<MarginBorrowOrderDto>> getBorrowAccounts(
            @Query("status") final Integer status,
            @Query("before") final Long before,
            @Query("after") final Long after,
            @Query("limit") Integer limit);

    /**
     * 单个币对借币历史
     * @param status
     * @param before
     * @param after
     * @param limit
     * @param product
     * @return
     */
    @GET("/api/margin/v3/accounts/{product_id}/borrow")
    Call<List<MarginBorrowOrderDto>> getBorrowAccountsByProductId(@Path("product_id") final String product,
                                                                  @Query("before") final Long before,
                                                                  @Query("after") final Long after,
                                                                  @Query("limit") final Integer limit,
                                                                  @Query("status") final Integer status);

    /**
     * 借币
     * @param product
     * @param currency
     * @param amount
     * @return
     */
    @POST("/api/margin/v3/accounts/borrow")
    Call<BorrowResult> borrow(@Query("product_id") final String product,
                              @Query("currency") final String currency,
                              @Query("amount") final String amount);

    /**
     * 还币
     * @param product
     * @param currency
     * @param amount
     * @param borrow_id
     * @return
     */
    @POST("/api/margin/v3/accounts/repayment")
    Call<RepaymentResult> repayment(@Query("product_id") final String product,
                                    @Query("currency") final String currency,
                                    @Query("amount") final String amount,
                                    @Query("borrow_id") final Long borrow_id);
}
