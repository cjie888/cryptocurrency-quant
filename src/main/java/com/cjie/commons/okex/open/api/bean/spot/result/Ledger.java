package com.cjie.commons.okex.open.api.bean.spot.result;

public class Ledger {

    private Long ledger_id;
    private String create_at;
    private String amount;
    private String balance;
    private String type;
    private Details details;

    public Long getLedger_id() {
        return this.ledger_id;
    }

    public void setLedger_id(final Long ledger_id) {
        this.ledger_id = ledger_id;
    }

    public String getCreate_at() {
        return this.create_at;
    }

    public void setCreate_at(final String create_at) {
        this.create_at = create_at;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(final String balance) {
        this.balance = balance;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Details getDetails() {
        return this.details;
    }

    public void setDetails(final Details details) {
        this.details = details;
    }

    public static class Details {
        private Long order_id;
        private String product_id;

        public Long getOrder_id() {
            return this.order_id;
        }

        public void setOrder_id(final Long order_id) {
            this.order_id = order_id;
        }

        public String getProduct_id() {
            return this.product_id;
        }

        public void setProduct_id(final String product_id) {
            this.product_id = product_id;
        }
    }
}
