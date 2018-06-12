package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;


public class HuobiOrderMatchResult {

    @SerializedName("created-at")
    private long createdAt;

    /**
     * 成交数量
     */
    @SerializedName("filled-amount")
    private String filledAmount;

    @SerializedName("filled-fees")
    private String filledFees;

    @SerializedName("id")
    private long id;

    @SerializedName("match-id")
    private long matchId;

    @SerializedName("order-id")
    private long orderId;

    @SerializedName("price")
    private String price;

    @SerializedName("source")
    private String source;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("type")
    private String type;

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFilledAmount() {
        return filledAmount;
    }

    public void setFilledAmount(String filledAmount) {
        this.filledAmount = filledAmount;
    }

    public String getFilledFees() {
        return filledFees;
    }

    public void setFilledFees(String filledFees) {
        this.filledFees = filledFees;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HuobiOrderMatchResult() {
    }
}
