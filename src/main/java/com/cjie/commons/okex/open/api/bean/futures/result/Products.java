package com.cjie.commons.okex.open.api.bean.futures.result;

/**
 * futures contract products <br/>
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/2/26 10:49
 */
public class Products {
    /**
     * The id of the futures contract
     */
    private String product_id;
    /**
     * Currency
     */
    private String base_currency;
    /**
     * Quote currency
     */
    private String quote_currency;
    /**
     * Minimum amount: $
     */
    private Double quote_increment;
    /**
     * Unit price per contract
     */
    private Double contract_val;
    /**
     * Effect of time
     */
    private String listing;
    /**
     * Settlement price
     */
    private String delivery;
    /**
     * Minimum amount: cont
     */
    private Double trade_increment;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getBase_currency() {
        return base_currency;
    }

    public void setBase_currency(String base_currency) {
        this.base_currency = base_currency;
    }

    public String getQuote_currency() {
        return quote_currency;
    }

    public void setQuote_currency(String quote_currency) {
        this.quote_currency = quote_currency;
    }

    public Double getQuote_increment() {
        return quote_increment;
    }

    public void setQuote_increment(Double quote_increment) {
        this.quote_increment = quote_increment;
    }

    public Double getContract_val() {
        return contract_val;
    }

    public void setContract_val(Double contract_val) {
        this.contract_val = contract_val;
    }

    public String getListing() {
        return listing;
    }

    public void setListing(String listing) {
        this.listing = listing;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public Double getTrade_increment() {
        return trade_increment;
    }

    public void setTrade_increment(Double trade_increment) {
        this.trade_increment = trade_increment;
    }
}
