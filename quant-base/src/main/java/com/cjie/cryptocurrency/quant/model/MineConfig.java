package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MineConfig {
    private Long id;

    private String site;

    private String baseCurrency;

    private String quotaCurrency;

    private Integer status;

    private BigDecimal maxPrice;

    private BigDecimal minPrice;

    private Date createTime;

    private Date modifyTime;

}