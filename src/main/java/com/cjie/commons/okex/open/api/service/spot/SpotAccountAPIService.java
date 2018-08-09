package com.cjie.commons.okex.open.api.service.spot;

import com.cjie.commons.okex.open.api.bean.spot.result.Account;
import com.cjie.commons.okex.open.api.bean.spot.result.Ledger;
import com.cjie.commons.okex.open.api.bean.spot.result.ServerTimeDto;

import java.util.List;

/**
 * @author liwei.li
 * @version 1.0.0
 * @date 2018/3/14 13:06
 */
public interface SpotAccountAPIService {

    ServerTimeDto time(String site);

    /**
     * 账户资产列表
     *
     * @return
     */
    List<Account> getAccounts(String site);

    /**
     * 账单列表
     *
     * @param currency
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<Ledger> getLedgersByCurrency(String site, String currency, Long before, Long after, Integer limit);

    /**
     * 单币资产
     *
     * @param currency
     * @return
     */
    Account getAccountByCurrency(String site, final String currency);
}
