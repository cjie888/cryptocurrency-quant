package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyBalance {
    private Long id;

    private String currency;

    private BigDecimal balance;

    private Date createTime;

    private Date modifyTime;

    private String site;

    private BigDecimal available;

    private BigDecimal hold;

}