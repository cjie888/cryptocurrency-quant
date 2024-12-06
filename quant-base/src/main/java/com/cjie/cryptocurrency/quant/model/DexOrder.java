package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DexOrder {
    private Long id;

    private String chainId;

    private String fromAddress;

    private String toAddress;

    private Date createTime;

    private BigDecimal size;

    private BigDecimal price;

    private Byte isMock;

    private String txId;

    private Integer status;
}