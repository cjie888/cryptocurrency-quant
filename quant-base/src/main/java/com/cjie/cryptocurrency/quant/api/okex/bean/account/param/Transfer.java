package com.cjie.cryptocurrency.quant.api.okex.bean.account.param;

import java.math.BigDecimal;

public class Transfer {

    private String currency;

    private BigDecimal amount;

    private Integer from;

    private Integer to;

    private String sub_account;

    private Integer product_id;

    private String instrument_id;

    private String to_instrument_id;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public String getSub_account() {
        return sub_account;
    }

    public void setSub_account(String sub_account) {
        this.sub_account = sub_account;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public String getInstrument_id() {
        return instrument_id;
    }

    public void setInstrument_id(String instrument_id) {
        this.instrument_id = instrument_id;
    }

    public String getTo_instrument_id() {
        return to_instrument_id;
    }

    public void setTo_instrument_id(String to_instrument_id) {
        this.to_instrument_id = to_instrument_id;
    }
}
