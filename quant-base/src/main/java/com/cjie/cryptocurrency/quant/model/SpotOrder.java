package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SpotOrder {
    private Long id;

    private String symbol;

    private Date createTime;

    private Byte type;

    private BigDecimal size;

    private BigDecimal price;

    private Byte isMock;

    private String orderId;

    private String strategy;

    private Integer status;

    private String referSymbol;
}