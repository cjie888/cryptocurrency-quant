package com.cjie.commons.okex.open.api.bean.spot.result;

import java.math.BigDecimal;
import java.util.Date;

public class MarginBorrowOrderDto {
    private Long borrow_id;
    private String product_id;
    private String currency;
    private Date created_at;
    private Date last_interest_time;
    private BigDecimal amount;
    private BigDecimal interest;
    private BigDecimal repay_amount;
    private BigDecimal repay_interest;

    public Long getBorrow_id() {
        return this.borrow_id;
    }

    public void setBorrow_id(final Long borrow_id) {
        this.borrow_id = borrow_id;
    }

    public String getProduct_id() {
        return this.product_id;
    }

    public void setProduct_id(final String product_id) {
        this.product_id = product_id;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public Date getCreated_at() {
        return this.created_at;
    }

    public void setCreated_at(final Date created_at) {
        this.created_at = created_at;
    }

    public Date getLast_interest_time() {
        return this.last_interest_time;
    }

    public void setLast_interest_time(final Date last_interest_time) {
        this.last_interest_time = last_interest_time;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public void setInterest(final BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getRepay_amount() {
        return this.repay_amount;
    }

    public void setRepay_amount(final BigDecimal repay_amount) {
        this.repay_amount = repay_amount;
    }

    public BigDecimal getRepay_interest() {
        return this.repay_interest;
    }

    public void setRepay_interest(final BigDecimal repay_interest) {
        this.repay_interest = repay_interest;
    }
}
