package com.cjie.cryptocurrency.quant.api.okex.v5.bean.other;

import java.math.BigDecimal;

public interface OrderBookItem<T> {
    String getPrice();

    T getSize();
}
