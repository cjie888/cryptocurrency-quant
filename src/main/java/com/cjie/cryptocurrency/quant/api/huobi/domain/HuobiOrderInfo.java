package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class HuobiOrderInfo {

    public static final String STATE_SUBMITTED = "submitted";

    public static final String STATE_PARTIAL_FILLED = "partial-filled";

    public static final String STATE_PARTIAL_CANCELED = "partial-canceled";

    public static final String STATE_FILLED = "filled";

    public static final String SATE_CANCELED = "canceled";

    @SerializedName("account-id")
    private long accountId;

    @SerializedName("amount")
    private String amount;

    @SerializedName("canceled-at")
    private long canceledAt;

    @SerializedName("created-at")
    private long createdAt;

    @SerializedName("field-amount")
    private String fieldAmount;

    @SerializedName("field-cash-amount")
    private String fieldCashAmount;

    @SerializedName("field-fees")
    private String fieldFees;

    @SerializedName("finished-at")
    private long finishedAt;

    @SerializedName("id")
    private long id;

    @SerializedName("price")
    private String price;

    @SerializedName("source")
    private String source;

    /**
     * <pre>
     *     pre-submitted
     *     submitting
     *     partial-filled
     *     partial-canceled
     *     filled
     *     canceled
     * </pre>
     */
    @SerializedName("state")
    private String state;

    @SerializedName("symbol")
    private String symbol;

    /**
     * <pre>
     *     buy-market：市价买,
     *     sell-market：市价卖,
     *     buy-limit：限价买,
     *     sell-limit：限价卖
     * </pre>
     */
    @SerializedName("type")
    private String type;
}
