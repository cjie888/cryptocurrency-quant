package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyRatio {
    private Long id;

    private String baseCurrency;

    private String quotaCurrency;

    private Date createTime;

    private Double ratio;

    private BigDecimal currentPrice;

    private String site;

}