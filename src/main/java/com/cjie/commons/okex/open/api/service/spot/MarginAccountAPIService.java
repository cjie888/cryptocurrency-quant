package com.cjie.commons.okex.open.api.service.spot;

import com.cjie.commons.okex.open.api.bean.spot.result.*;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 *
 */
public interface MarginAccountAPIService {

    /**
     * 全部杠杆资产
     * @return
     */
    List<MarginAccountDto> getAccounts();

    /**
     * 单个币对杠杆账号资产
     *
     * @param product
     * @return
     */
    List<MarginAccountDetailDto> getAccountsByProductId(@Path("product_id") final String product);

    /**
     * 杠杆账单明细
     * @param product
     * @param type
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<UserMarginBillDto> getLedger(@Path("product_id") final String product,
                                      @Query("type") final Integer type,
                                      @Query("before") final Long before,
                                      @Query("after") final Long after,
                                      @Query("limit") Integer limit);

    /**
     * 全部币对配置
     * @return
     */
    List<BorrowConfigDto> getMarginInfo();

    /**
     * 单个币对配置
     * @param product
     * @return
     */
    List<BorrowConfigDto> getMarginInfoByProductId(@Path("product_id") final String product);

    /**
     * 全部借币历史
     * @param status
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<MarginBorrowOrderDto> getBorrowAccounts(
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
    List<MarginBorrowOrderDto> getBorrowAccountsByProductId(@Path("product_id") final String product,
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
    BorrowResult borrow(@Query("product_id") final String product,
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
    RepaymentResult repayment(@Query("product_id") final String product,
                              @Query("currency") final String currency,
                              @Query("amount") final String amount,
                              @Query("borrow_id") final Long borrow_id);
}
