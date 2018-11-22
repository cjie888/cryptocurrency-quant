package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FuturesInstrument {
    private Long id;

    private String instrumentId;

    private String underlyingIndex;

    private String quoteCurrency;

    private BigDecimal contractVal;

    private Date listing;

    private Date delivery;

    private BigDecimal tickSize;

    private BigDecimal tradeIncrement;

    private Date createTime;
}