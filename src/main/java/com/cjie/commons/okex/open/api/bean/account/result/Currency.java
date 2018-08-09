package com.cjie.commons.okex.open.api.bean.account.result;

import java.math.BigDecimal;

public class Currency {
    private String id;

    private String name;

    private Integer withdraw;

    private Integer deposit;

    private BigDecimal withdraw_min;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(Integer withdraw) {
        this.withdraw = withdraw;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public void setDeposit(Integer deposit) {
        this.deposit = deposit;
    }

    public BigDecimal getWithdraw_min() {
        return withdraw_min;
    }

    public void setWithdraw_min(BigDecimal withdraw_min) {
        this.withdraw_min = withdraw_min;
    }
}
