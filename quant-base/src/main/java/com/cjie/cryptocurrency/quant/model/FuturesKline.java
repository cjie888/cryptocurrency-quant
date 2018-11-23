package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FuturesKline {
    private Long id;

    private Date klineTime;

    private String instrumentId;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal low;

    private BigDecimal high;

    private BigDecimal volume;

    private BigDecimal currencyVolume;

    private Date createTime;

    private String suffix = "";

}