package com.cjie.commons.okex.open.api.bean.account.result;

import java.math.BigDecimal;

public class WithdrawFee {

      private BigDecimal min;

      private BigDecimal max;


      private String currency;

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
