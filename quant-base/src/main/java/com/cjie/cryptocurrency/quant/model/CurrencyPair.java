package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyPair {
    private Long id;

    private Date createTime;

    private String baseCurrency;

    private String quotaCurrency;

    private String site;

}