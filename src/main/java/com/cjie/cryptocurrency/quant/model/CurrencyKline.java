package com.cjie.cryptocurrency.quant.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class CurrencyKline {
    private Long id;

    private Date klineTime;

    private String baseCurrency;

    private String quotaCurrency;

    private BigDecimal amount;

    private Integer count;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal low;

    private BigDecimal high;

    private BigDecimal vol;

    private String site;

}