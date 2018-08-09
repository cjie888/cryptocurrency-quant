package com.cjie.commons.okex.open.api.service.spot.impl;

import com.cjie.commons.okex.open.api.bean.spot.result.*;
import com.cjie.commons.okex.open.api.client.APIClient;
import com.cjie.commons.okex.open.api.config.APIConfiguration;
import com.cjie.commons.okex.open.api.service.spot.MarginAccountAPIService;

import java.util.List;

/**
 *
 */
public class MarginAccountAPIServiceImpl implements MarginAccountAPIService {

    private final APIClient client;
    private final MarginAccountAPI api;

    public MarginAccountAPIServiceImpl(final APIConfiguration config) {
        this.client = new APIClient(config);
        this.api = this.client.createService(MarginAccountAPI.class);
    }


    @Override
    public BorrowResult borrow(final String product, final String currency, final String amount) {
        return this.client.executeSync(this.api.borrow(product, currency, amount));
    }

    @Override
    public List<MarginAccountDto> getAccounts() {
        return this.client.executeSync(this.api.getAccounts());
    }

    @Override
    public List<MarginAccountDetailDto> getAccountsByProductId(final String product) {
        return this.client.executeSync(this.api.getAccountsByProductId(product));
    }

    @Override
    public List<UserMarginBillDto> getLedger(final String product, final Integer type, final Long before, final Long after, final Integer limit) {
        return this.client.executeSync(this.api.getLedger(product, type, before, after, limit));
    }

    @Override
    public List<BorrowConfigDto> getMarginInfo() {
        return this.client.executeSync(this.api.getMarginInfo());
    }

    @Override
    public List<BorrowConfigDto> getMarginInfoByProductId(final String product) {
        return this.client.executeSync(this.api.getMarginInfoByProductId(product));
    }

    @Override
    public List<MarginBorrowOrderDto> getBorrowAccounts(final Integer status, final Long before, final Long after, final Integer limit) {
        return this.client.executeSync(this.api.getBorrowAccounts(status, before, after, limit));
    }

    @Override
    public List<MarginBorrowOrderDto> getBorrowAccountsByProductId(final String product, final Long before, final Long after, final Integer limit, final Integer status) {
        return this.client.executeSync(this.api.getBorrowAccountsByProductId(product, before, after, limit, status));
    }

    @Override
    public RepaymentResult repayment(final String product, final String currency, final String amount, final Long borrow_id) {
        return this.client.executeSync(this.api.repayment(product, currency, amount, borrow_id));
    }
}
