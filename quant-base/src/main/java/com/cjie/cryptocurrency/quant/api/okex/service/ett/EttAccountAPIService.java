package com.cjie.cryptocurrency.quant.api.okex.service.ett;

import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.CursorPager;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.CursorPager;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttAccount;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttLedger;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.CursorPager;

import java.util.List;

/**
 * @author chuping.cui
 * @date 2018/7/4
 */
public interface EttAccountAPIService {

    /**
     * Get all ett account list
     *
     * @return account info list
     */
    List<EttAccount> getAccount();

    /**
     * Get ett currency account
     *
     * @param currency currency code
     * @return account info
     */
    EttAccount getAccount(String currency);

    /**
     * Get ett account ledger list
     *
     * @param currency currency code
     * @param before   before and after cursors are available via response headers OK-BEFORE and OK-AFTER. Your requests should use these cursor values when making requests for pages after the initial
     *                 request. {@link CursorPager}
     * @param after    before and after cursors are available via response headers OK-BEFORE and OK-AFTER. Your requests should use these cursor values when making requests for pages after the initial
     *                 request. {@link CursorPager}
     * @param limit    number of results per request.
     * @return
     */
    CursorPager<EttLedger> getLedger(String currency, String before, String after, int limit);

}
