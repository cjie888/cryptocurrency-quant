package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
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

}
