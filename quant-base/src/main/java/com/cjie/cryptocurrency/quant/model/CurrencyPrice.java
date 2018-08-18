package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyPrice {
    private Long id;

    private Date tickTime;

    private String baseCurrency;

    private String quotaCurrency;

    private BigDecimal price;

    private String site;
}