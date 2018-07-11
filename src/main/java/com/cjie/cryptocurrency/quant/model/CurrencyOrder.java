package com.cjie.cryptocurrency.quant.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
public class CurrencyOrder {
    private Integer id;

    private String orderId;

    private String baseCurrency;

    private String quotaCurrency;

    private BigDecimal orderPrice;

    private BigDecimal markePrice;

    private BigDecimal amount;

    private String site;

    private Date createTime;

    private Integer type;

}