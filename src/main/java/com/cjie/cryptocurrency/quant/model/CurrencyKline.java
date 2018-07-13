package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    private String suffix = "";

}