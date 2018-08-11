package com.cjie.cryptocurrency.quant.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class CurrencyPrice {
    private Long id;

    private Date tickTime;

    private String baseCurrency;

    private String quotaCurrency;

    private BigDecimal price;

    private String site;
}