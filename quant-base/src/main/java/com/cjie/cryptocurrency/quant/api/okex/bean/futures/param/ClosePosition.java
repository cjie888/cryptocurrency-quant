package com.cjie.cryptocurrency.quant.api.okex.bean.futures.param;

import com.cjie.cryptocurrency.quant.api.okex.enums.FuturesTransactionTypeEnum;
import com.cjie.cryptocurrency.quant.api.okex.enums.FuturesTransactionTypeEnum;
import com.cjie.cryptocurrency.quant.api.okex.enums.FuturesTransactionTypeEnum;

/**
 * Close Position
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/9 15:38
 */
public class ClosePosition {
    /**
     * The id of the futures, eg: BTC_USD_0331
     */
    private String product_id;
    /**
     * The execution type {@link FuturesTransactionTypeEnum}
     */
    private Integer type;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
